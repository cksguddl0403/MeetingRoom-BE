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
import kr.co.ta9.meetingroom.domain.user.dto.UserProfileUpdateResponseDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserInfoUpdateResponseDto;
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

import java.io.IOException;
import java.util.Objects;
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
        // 로그인 아이디 중복 확인
        if(userRepository.existsByLoginId(userCreateRequestDto.getLoginId())) {
            throw new UserException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 닉네임 중복 확인
        if(userRepository.existsByNickname(userCreateRequestDto.getNickname())) {
            throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        // 이메일 중복 확인
        if(userRepository.existsByEmail(userCreateRequestDto.getEmail())) {
            throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
        }

        String verificationKey = EmailVerificationPurpose.SIGNUP.verifiedKey(userCreateRequestDto.getVerificationId());

        // Redis에서 검증된 이메일 조회
        String verifiedEmail = stringRedisTemplate.opsForValue().get(verificationKey);

        // 검증된 이메일과 일치하는지 확인
        if (verifiedEmail == null
                || !verifiedEmail.equalsIgnoreCase(userCreateRequestDto.getEmail())) {
            throw new UserException(UserErrorCode.INVALID_EMAIL_VERIFICATION);
        }

        // 검증된 이메일 삭제
        stringRedisTemplate.delete(verificationKey);

        // 사용자 생성
        User user = User.createUser(
                userCreateRequestDto.getLoginId(),
                passwordEncoder.encode(userCreateRequestDto.getPassword()),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getNickname(),
                userCreateRequestDto.getEmail()
        );

        // 사용자 저장
        User savedUser = userRepository.save(user);

        // 재직 증명서 파일명 길이, 확장자, ContentType 검증
        validateEmploymentCertificateFile(employmentCertificateFile);

        // 재직 증명서 파일 AWS S3 업로드
        String employmentCertificateFilePublicUrl = amazonS3Service.uploadFile(employmentCertificateFile);

        // 파일 생성
        File employmentCertificate = File.createFile(
                employmentCertificateFile.getOriginalFilename(),
                employmentCertificateFile.getSize(),
                StringUtils.getFilenameExtension(employmentCertificateFile.getOriginalFilename()),
                employmentCertificateFilePublicUrl,
                FileType.EMPLOYMENT_CERTIFICATE,
                savedUser.getId()
        );

        File defaultProfile = File.createFile(
                "default.png",
                20602L,
                "png",
                "https://tanineojt.s3.ap-northeast-2.amazonaws.com/default.png",
                FileType.PROFILE,
                savedUser.getId()
        );

        // 파일 저장
        fileRepository.save(employmentCertificate);
        fileRepository.save(defaultProfile);

        // 회사 멤버 정보와 회사 정보 조회
        Optional<CompanyMember> companyMember = companyMemberRepository.findWithCompanyByUser_Id(savedUser.getId());

        return userMapper.toDto(
                savedUser,
                companyMember.map(CompanyMember::getCompany).orElse(null),
                companyMember.map(CompanyMember::getRole).orElse(null)
        );
    }

    // 사용자 정보 조회
    public UserDto getUserInfo(User currentUser) {
        // 회사 멤버 정보와 회사 정보 조회
        Optional<CompanyMember> companyMember = companyMemberRepository.findWithCompanyByUser_Id(currentUser.getId());

        return userMapper.toDto(
                currentUser,
                companyMember.map(CompanyMember::getCompany).orElse(null),
                companyMember.map(CompanyMember::getRole).orElse(null)
        );
    }

    // 사용자 프로필 조회
    public UserProfileDto getUserProfile(User currentUser) {
        // 프로필 이미지 조회
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
    public UserInfoUpdateResponseDto updateUserInfo(User currentUser, UserUpdateRequestDto userUpdateRequestDto) {
        // 사용자 조회
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String newPassword = userUpdateRequestDto.getNewPassword();
        String encodedNewPassword = null;
        boolean passwordChanged = false;
        // 새 비밀번호가 입력된 경우
        if (StringUtils.hasText(newPassword)) {
           // 기존 비밀번호를 입력하지 않는 경우 예외 발생
            String currentPassword = userUpdateRequestDto.getCurrentPassword();
            if (!StringUtils.hasText(currentPassword)) {
                throw new UserException(UserErrorCode.CURRENT_PASSWORD_REQUIRED);
            }

            // 기존 비밀번호가 일치하지 않는 경우 예외 발생
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new UserException(UserErrorCode.INVALID_CURRENT_PASSWORD);
            }

            // 새 비밀번호가 기존 비밀번호와 같은 경우 예외 발생
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                throw new UserException(UserErrorCode.SAME_AS_CURRENT_PASSWORD);
            }

            // 새 비밀번호 암호화
            encodedNewPassword = passwordEncoder.encode(newPassword);
            passwordChanged = true;
        }

        boolean nameChanged = !Objects.equals(user.getName(), userUpdateRequestDto.getName());

        // 사용자 정보 업데이트
        user.updateInfo(encodedNewPassword, userUpdateRequestDto.getName());

        // 회사 멤버 정보와 회사 정보 조회
        Optional<CompanyMember> companyMember = companyMemberRepository.findWithCompanyByUser_Id(user.getId());

        UserDto userDto = userMapper.toDto(
            user,
            companyMember.map(CompanyMember::getCompany).orElse(null),
            companyMember.map(CompanyMember::getRole).orElse(null)
        );

        return UserInfoUpdateResponseDto.builder()
                .id(userDto.getId())
                .loginId(userDto.getLoginId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .company(userDto.getCompany())
                .role(userDto.getRole())
                .certificated(userDto.isCertificated())
                .changed(nameChanged || passwordChanged)
                .build();
    }

    // 사용자 프로필 수정
    @Transactional
    public UserProfileUpdateResponseDto updateUserProfile(
            User currentUser,
            UserProfileUpdateRequestDto userProfileUpdateRequestDto,
            MultipartFile profileImageFile
    ) {
        // 사용자 조회
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        boolean nicknameChanged = !Objects.equals(user.getNickname(), userProfileUpdateRequestDto.getNickname());
        boolean profileImageChanged = profileImageFile != null && !profileImageFile.isEmpty();

        // 닉네임 중복 확인
        if (nicknameChanged) {
            if (userRepository.existsByNickname(userProfileUpdateRequestDto.getNickname())) {
                throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
            }
        }

        // 사용자 프로필 업데이트
        user.updateProfile(userProfileUpdateRequestDto.getNickname());

        // 기존 프로필 이미지 조회
        Optional<File> existingProfile = fileRepository.findByTypeAndTargetId(
                FileType.PROFILE,
                user.getId()
        );

        String previousUrl = existingProfile.map(File::getUrl).orElse(null);

        // 새로운 프로필 이미지 파일을 업로드한 경우
        if (profileImageChanged) {
            // 이미지 확장자 및 ContentType 검증
            validateImagesFile(profileImageFile);

            String originalName = profileImageFile.getOriginalFilename();

            long size = profileImageFile.getSize();

            String extension = Optional.ofNullable(StringUtils.getFilenameExtension(originalName))
                    .orElse("")
                    .toLowerCase();

            // 이미지 AWS 업로드
            String newProfileUrl = amazonS3Service.uploadFile(profileImageFile);

            // 기존 프로필 이미지가 존재하는 경우 업데이트 및 File 삭제
            if (existingProfile.isPresent()) {
                File profile = existingProfile.get();
                // 기존 프로필 이미지 삭제
                amazonS3Service.deleteFile(previousUrl);

                // 기존 프로필 이미지 업데이트
                profile.updateImage(originalName, size, extension, newProfileUrl);

            } else {
                // File 생성
                File file = File.createFile(
                        originalName,
                        size,
                        extension,
                        newProfileUrl,
                        FileType.PROFILE,
                        user.getId()
                );

                // File 저장
                fileRepository.save(file);
            }
            return UserProfileUpdateResponseDto.builder()
                    .nickname(user.getNickname())
                    .profileImageUrl(newProfileUrl)
                    .changed(true)
                    .build();
        }

        return UserProfileUpdateResponseDto.builder()
                .nickname(user.getNickname())
                .profileImageUrl(previousUrl)
                .changed(nicknameChanged)
                .build();
    }

    // 이미지 파일명 길이, 확장자, ContentType 검증
    private void validateImagesFile(MultipartFile ImageFile) {
        String originalName = ImageFile.getOriginalFilename();
        if (originalName != null && originalName.length() > 255) {
            throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_NAME_LENGTH_EXCEEDED);
        }
        String extension = StringUtils.getFilenameExtension(originalName);

        if (extension == null) {
            throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_FORMAT_INVALID);
        }

        extension = extension.toLowerCase();

        boolean validExtension = extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("png")
                || extension.equals("webp");

        if (!validExtension) {
            throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_FORMAT_INVALID);
        }

        String contentType = ImageFile.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String normalizedContentType = contentType.toLowerCase();
            boolean validMimeType = ((extension.equals("jpg") || extension.equals("jpeg"))
                    && normalizedContentType.equals("image/jpeg"))
                    || (extension.equals("png")
                    && normalizedContentType.equals("image/png"))
                    || (extension.equals("webp")
                    && normalizedContentType.equals("image/webp"));

            if (!validMimeType) {
                throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_FORMAT_INVALID);
            }
        }

        if (!hasValidImageSignature(ImageFile, extension)) {
            throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_SIGNATURE_INVALID);
        }
    }

    // 재직 증명서 파일명 길이, 확장자, ContentType 검증
    private void validateEmploymentCertificateFile(MultipartFile employmentCertificateFile) {
        String originalName = employmentCertificateFile.getOriginalFilename();
        if (originalName != null && originalName.length() > 255) {
            throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_NAME_LENGTH_EXCEEDED);
        }
        String extension = StringUtils.getFilenameExtension(originalName);

        if (extension == null) {
            throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_FORMAT_INVALID);
        }

        extension = extension.toLowerCase();

        boolean validExtension = extension.equals("pdf");

        if (!validExtension) {
            throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_FORMAT_INVALID);
        }

        String contentType = employmentCertificateFile.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String normalizedContentType = contentType.toLowerCase();
            boolean validMimeType = extension.equals("pdf")
                    && normalizedContentType.equals("application/pdf");

            if (!validMimeType) {
                throw new UserException(UserErrorCode.USER_PROFILE_IMAGE_FORMAT_INVALID);
            }
        }

        if (!hasValidEmploymentCertificateSignature(employmentCertificateFile, extension)) {
            throw new UserException(UserErrorCode.EMPLOYMENT_CERTIFICATE_SIGNATURE_INVALID);
        }
    }

    private boolean hasValidImageSignature(MultipartFile imageFile, String extension) {
        try {
            byte[] signature = imageFile.getInputStream().readNBytes(12);

            if (extension.equals("jpg") || extension.equals("jpeg")) {
                return signature.length >= 3
                        && (signature[0] & 0xFF) == 0xFF
                        && (signature[1] & 0xFF) == 0xD8
                        && (signature[2] & 0xFF) == 0xFF;
            }

            if (extension.equals("png")) {
                return signature.length >= 8
                        && (signature[0] & 0xFF) == 0x89
                        && (signature[1] & 0xFF) == 0x50
                        && (signature[2] & 0xFF) == 0x4E
                        && (signature[3] & 0xFF) == 0x47
                        && (signature[4] & 0xFF) == 0x0D
                        && (signature[5] & 0xFF) == 0x0A
                        && (signature[6] & 0xFF) == 0x1A
                        && (signature[7] & 0xFF) == 0x0A;
            }

            if (extension.equals("webp")) {
                return signature.length >= 12
                        && signature[0] == 'R'
                        && signature[1] == 'I'
                        && signature[2] == 'F'
                        && signature[3] == 'F'
                        && signature[8] == 'W'
                        && signature[9] == 'E'
                        && signature[10] == 'B'
                        && signature[11] == 'P';
            }

            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasValidEmploymentCertificateSignature(MultipartFile employmentCertificateFile, String extension) {
        if (extension.equals("pdf")) {
            try {
                byte[] signature = employmentCertificateFile.getInputStream().readNBytes(5);
                return signature.length >= 5
                        && signature[0] == '%'
                        && signature[1] == 'P'
                        && signature[2] == 'D'
                        && signature[3] == 'F'
                        && signature[4] == '-';
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }
}
