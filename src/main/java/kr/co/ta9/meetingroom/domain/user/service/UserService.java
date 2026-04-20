package kr.co.ta9.meetingroom.domain.user.service;

import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.file.entity.File;
import kr.co.ta9.meetingroom.domain.file.enums.FileType;
import kr.co.ta9.meetingroom.domain.file.repository.FileRepository;
import kr.co.ta9.meetingroom.domain.auth.enums.EmailVerificationPurpose;
import kr.co.ta9.meetingroom.domain.user.dto.UserCreateRequestDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserProfileDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserUpdateRequestDto;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.exception.UserException;
import kr.co.ta9.meetingroom.domain.user.mapper.UserMapper;
import kr.co.ta9.meetingroom.domain.user.repository.UserRepository;
import kr.co.ta9.meetingroom.global.error.code.UserErrorCode;
import kr.co.ta9.meetingroom.infra.s3.service.AmazonS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final AmazonS3Service  amazonS3Service;

    // 회원가입
    @Transactional
    public UserDto join(
            UserCreateRequestDto userCreateRequestDto,
            MultipartFile employmentCertificateFile
    ) {
        userRepository.findByLoginId(userCreateRequestDto.getLoginId()).ifPresent(user -> {
            throw new UserException(UserErrorCode.DUPLICATE_USER);
        });

        String verificationKey = EmailVerificationPurpose.SIGNUP.verifiedKey(userCreateRequestDto.getVerificationId());
        String verifiedEmail = stringRedisTemplate.opsForValue().get(verificationKey);
        if (verifiedEmail == null
                || !verifiedEmail.equalsIgnoreCase(userCreateRequestDto.getEmail())) {
            throw new UserException(UserErrorCode.INVALID_EMAIL_VERIFICATION);
        }

        stringRedisTemplate.delete(verificationKey);

        User user = User.createUser(
                userCreateRequestDto.getLoginId(),
                passwordEncoder.encode(userCreateRequestDto.getPassword()),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getNickname(),
                userCreateRequestDto.getEmail()
        );

        User savedUser = userRepository.save(user);

        String employmentCertificateFilePublicUrl = amazonS3Service.uploadFile(employmentCertificateFile);

        File employmentCertificate = File.createFile(
                employmentCertificateFile.getOriginalFilename(),
                employmentCertificateFile.getSize(),
                StringUtils.getFilenameExtension(employmentCertificateFile.getOriginalFilename()),
                employmentCertificateFilePublicUrl,
                FileType.EMPLOYMENT_CERTIFICATE,
                savedUser.getId()
        );

        fileRepository.save(employmentCertificate);

        Optional<CompanyMember> companyMember = companyMemberRepository.findWithCompanyByUser_Id(savedUser.getId());
        return userMapper.toDto(
                savedUser,
                companyMember.map(CompanyMember::getCompany).orElse(null),
                companyMember.map(CompanyMember::getRole).orElse(null)
        );
    }

    // 사용자 정보 조회
    public UserDto getUserInfo(User currentUser) {
        Optional<CompanyMember> companyMember = companyMemberRepository.findWithCompanyByUser_Id(currentUser.getId());
        return userMapper.toDto(
                currentUser,
                companyMember.map(CompanyMember::getCompany).orElse(null),
                companyMember.map(CompanyMember::getRole).orElse(null)
        );
    }

    // 사용자 프로필 조회
    public UserProfileDto getUserProfile(User currentUser) {
        Optional<File> existingProfile = fileRepository.findByTypeAndTargetId(
                FileType.PROFILE,
                currentUser.getId()
        );

        String previousUrl = existingProfile.map(File::getUrl).orElse(null);

        return UserProfileDto.builder()
                .nickname(currentUser.getNickname())
                .profileImageUrl(previousUrl)
                .build();
    }

    // 사용자 정보 수정
    @Transactional
    public UserDto updateUserInfo(User currentUser, UserUpdateRequestDto userUpdateRequestDto) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String newPassword = userUpdateRequestDto.getNewPassword();
        if (StringUtils.hasText(newPassword)) {
            String currentPassword = userUpdateRequestDto.getCurrentPassword();
            if (!StringUtils.hasText(currentPassword)) {
                throw new UserException(UserErrorCode.CURRENT_PASSWORD_REQUIRED);
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new UserException(UserErrorCode.INVALID_CURRENT_PASSWORD);
            }
        }

        String encodedNewPassword = StringUtils.hasText(newPassword)
                ? passwordEncoder.encode(newPassword)
                : null;

        user.updateInfo(encodedNewPassword, userUpdateRequestDto.getName());

        Optional<CompanyMember> companyMember = companyMemberRepository.findWithCompanyByUser_Id(user.getId());

        return userMapper.toDto(
                user,
                companyMember.map(CompanyMember::getCompany).orElse(null),
                companyMember.map(CompanyMember::getRole).orElse(null)
        );
    }

    // 사용자 프로필 수정
    @Transactional
    public UserProfileDto updateUserProfile(
            User currentUser,
            UserProfileUpdateRequestDto userProfileUpdateRequestDto,
            MultipartFile profileImageFile
    ) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 새로운 닉네임이 현재 닉네임과 다르고, 이미 존재하는 닉네임인 경우 예외 처리
        if(!user.getNickname().equals(userProfileUpdateRequestDto.getNickname())) {
            if(userRepository.existsByNickname(userProfileUpdateRequestDto.getNickname())) {
                throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
            }
        }

        user.updateProfile(userProfileUpdateRequestDto.getNickname());

        String newProfileUrl = null;

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            String originalName = profileImageFile.getOriginalFilename();
            String name = StringUtils.hasText(originalName) ? originalName : "profile";

            String extension = Optional.ofNullable(StringUtils.getFilenameExtension(originalName))
                    .orElse("")
                    .toLowerCase();

            boolean validExtension = extension.equals("jpg")
                    || extension.equals("jpeg")
                    || extension.equals("png")
                    || extension.equals("webp");
            if (!validExtension) {
                throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_INVALID);
            }

            String contentType = profileImageFile.getContentType();
            if (contentType != null && !contentType.isBlank()) {
                String normalizedContentType = contentType.toLowerCase();
                boolean validMimeType = ((extension.equals("jpg") || extension.equals("jpeg"))
                        && normalizedContentType.equals("image/jpeg"))
                        || (extension.equals("png")
                        && normalizedContentType.equals("image/png"))
                        || (extension.equals("webp")
                        && normalizedContentType.equals("image/webp"));

                if (!validMimeType) {
                    throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_INVALID);
                }
            }

            newProfileUrl = amazonS3Service.uploadFile(profileImageFile);
            long size = profileImageFile.getSize();

            Optional<File> existingProfile = fileRepository.findByTypeAndTargetId(
                    FileType.PROFILE,
                    user.getId()
            );
            String previousUrl = existingProfile.map(File::getUrl).orElse(null);

            if (existingProfile.isPresent()) {
                File profile = existingProfile.get();
                profile.updateImage(name, size, extension, newProfileUrl);
            } else {
                fileRepository.save(File.createFile(
                        name,
                        size,
                        extension,
                        newProfileUrl,
                        FileType.PROFILE,
                        user.getId()
                ));
            }

            if (previousUrl != null) {
                amazonS3Service.deleteFile(previousUrl);
                fileRepository.delete(existingProfile.get());
            }
        }

        return UserProfileDto.builder()
                .nickname(user.getNickname())
                .profileImageUrl(newProfileUrl)
                .build();
    }
}
