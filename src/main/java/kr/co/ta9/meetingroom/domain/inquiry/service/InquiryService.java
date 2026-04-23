package kr.co.ta9.meetingroom.domain.inquiry.service;

import kr.co.ta9.meetingroom.domain.category.entity.Category;
import kr.co.ta9.meetingroom.domain.category.enums.CategoryType;
import kr.co.ta9.meetingroom.domain.category.exception.CategoryException;
import kr.co.ta9.meetingroom.domain.category.repository.CategoryRepository;
import kr.co.ta9.meetingroom.domain.file.entity.File;
import kr.co.ta9.meetingroom.domain.file.enums.FileType;
import kr.co.ta9.meetingroom.domain.file.repository.FileRepository;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryCreateRequestDto;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryDto;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryListDto;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryQueryDto;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryUpdateRequestDto;
import kr.co.ta9.meetingroom.domain.inquiry.entity.Inquiry;
import kr.co.ta9.meetingroom.domain.inquiry.exception.InquiryException;
import kr.co.ta9.meetingroom.domain.inquiry.mapper.InquiryMapper;
import kr.co.ta9.meetingroom.domain.inquiry.repository.InquiryReplyRepository;
import kr.co.ta9.meetingroom.domain.inquiry.repository.InquiryRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.exception.UserException;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.CategoryErrorCode;
import kr.co.ta9.meetingroom.global.error.code.InquiryErrorCode;
import kr.co.ta9.meetingroom.global.error.code.UserErrorCode;
import kr.co.ta9.meetingroom.infra.s3.service.AmazonS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryService {
    private static final int MAX_INQUIRY_IMAGE_COUNT = 5;

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final CategoryRepository categoryRepository;
    private final FileRepository fileRepository;
    private final InquiryMapper inquiryMapper;
    private final AmazonS3Service amazonS3Service;

    // 문의 등록
    @Transactional
    public InquiryDto createInquiry(User currentUser, InquiryCreateRequestDto inquiryCreateRequestDto, List<MultipartFile> inquiryImageFiles) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 카테고리 조회
        Category category = categoryRepository.findById(inquiryCreateRequestDto.getInquiryCategoryId())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        // 카테고리 타입이 문의가 아니면 예외
        if (category.getType() != CategoryType.INQUIRY) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_CATEGORY_INVALID_TYPE);
        }

        // MultipartFile 유효성 검사
        validateImagesFiles(inquiryImageFiles);

        // 이미지 개수 검증
        int newImageCount = countValidImagesFiles(inquiryImageFiles);
        if (newImageCount > MAX_INQUIRY_IMAGE_COUNT) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_IMAGE_COUNT_EXCEEDED);
        }

        boolean secret = Boolean.TRUE.equals(inquiryCreateRequestDto.getIsPrivate());

        // 문의 생성
        Inquiry inquiry = Inquiry.createInquiry(
                currentUser,
                category,
                inquiryCreateRequestDto.getTitle(),
                inquiryCreateRequestDto.getContent(),
                secret
        );

        // 문의 저장
        inquiryRepository.save(inquiry);

        // 이미지 AWS 업로드
        List<File> inquiryFiles = uploadImages(inquiry.getId(), inquiryImageFiles);

        // DB에 File 저장
        fileRepository.saveAll(inquiryFiles);

        return inquiryMapper.toDto(inquiry, inquiryFiles);
    }

    // 문의 상세 조회
    public InquiryDto getInquiry(User currentUser, Long id) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 문의 조회
        InquiryQueryDto inquiryQueryDto = inquiryRepository.getInquiryById(currentUser.getId(), id)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        // 비밀 글인데 작성자인지 검사
        if(inquiryQueryDto.isSecret() && !inquiryQueryDto.getAuthor().getId().equals(currentUser.getId())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_AUTHORIZED);
        }

        // 문의에 기존 등록된 File 목록 조회
        List<File> inquiryFiles = fileRepository.findAllByTypeAndTargetId(FileType.INQUIRY, inquiryQueryDto.getId());

        return inquiryMapper.toDto(inquiryQueryDto, inquiryFiles);
    }

    // 문의 목록 조회
    public OffsetPageResponseDto<InquiryListDto> getInquiries(User currentUser, Pageable pageable, InquiryListSearchRequestDto inquiryListSearchRequestDto) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 문의 목록 조회
        Page<InquiryQueryDto> page = inquiryRepository.getInquiries(currentUser.getId(), inquiryListSearchRequestDto, pageable);

        // 조회된 문의 ID 목록으로 File 조회
        List<File> inquiryFiles = fileRepository.findAllByTypeAndTargetIdIn(
                FileType.INQUIRY,
                page.getContent().stream().map(InquiryQueryDto::getId).toList()
        );

        List<InquiryListDto> inquiryListDtos = page.getContent().stream()
                .map(inquiryQueryDto -> {
                    List<File> filesForInquiry = inquiryFiles.stream()
                            .toList();
                    return inquiryMapper.toListDto(inquiryQueryDto, filesForInquiry);
                })
                .toList();

        return OffsetPageResponseDto.<InquiryListDto>builder()
                .totalCount(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .content(inquiryListDtos)
                .build();
    }

    // 문의 수정
    @Transactional
    public InquiryDto updateInquiry(User currentUser, Long inquiryId, InquiryUpdateRequestDto inquiryUpdateRequestDto, List<MultipartFile> inquiryImageFiles) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 카테고리 조회
        Category category = categoryRepository.findById(inquiryUpdateRequestDto.getInquiryCategoryId())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        // 카테고리 타입이 문의가 아니면 예외
        if (category.getType() != CategoryType.INQUIRY) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_CATEGORY_INVALID_TYPE);
        }

        // 문의 조회
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        // 문의 작성자와 현재 사용자가 다르면 예외
        if (!inquiry.getUser().getId().equals(currentUser.getId())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_AUTHORIZED);
        }

        // MultipartFile 유효성 검사
        validateImagesFiles(inquiryImageFiles);

        boolean secret = Boolean.TRUE.equals(inquiryUpdateRequestDto.getIsPrivate());

        // 문의 업데이트
        inquiry.update(category, inquiryUpdateRequestDto.getTitle(), inquiryUpdateRequestDto.getContent(), secret);

        // 문의에 기존 등록된 File 목록 조회
        List<File> existingImages = fileRepository.findAllByTypeAndTargetId(FileType.INQUIRY, inquiry.getId());

        // 유지할 이미지 URL 목록 생성
        List<String> retainUrls = inquiryUpdateRequestDto.getRetainImageUrls();

        // 이미지 개수 검증
        int retainImageCount = retainUrls == null ? 0 : (int) retainUrls.stream().distinct().count();
        int newImageCount = countValidImagesFiles(inquiryImageFiles);
        if (retainImageCount + newImageCount > MAX_INQUIRY_IMAGE_COUNT) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_IMAGE_COUNT_EXCEEDED);
        }

        // 유지할 이미지 URL이 기존 이미지 목록에 모두 존재하는지 검증
        for (String retainUrl : retainUrls) {
            boolean owned = existingImages.stream().anyMatch(f -> retainUrl.equals(f.getUrl()));

            if (!owned) {
                throw new InquiryException(InquiryErrorCode.INQUIRY_IMAGE_INVALID);
            }
        }

        // 유지할 File 목록 생성
        List<File> retainedImages = existingImages.stream()
                .filter(f -> retainUrls.contains(f.getUrl()))
                .toList();

        // 제거할 File 목록 생성
        List<File> toRemoveFiles = existingImages.stream()
                .filter(f -> !retainUrls.contains(f.getUrl()))
                .toList();

        // 제거할 이미지 URL 목록 생성
        List<String> removalPublicUrls = toRemoveFiles.stream()
                .map(File::getUrl)
                .toList();

        // AWS 다중 이미지 삭제
        amazonS3Service.deleteFiles(removalPublicUrls);

        // DB에 File 제거
        fileRepository.deleteAll(toRemoveFiles);

        // 새롭게 업로드된 이미지 AWS 업로드
        List<File> inquiryImages = uploadImages(inquiry.getId(), inquiryImageFiles);

        // 새로운 DB에 새로운 File 저장
        fileRepository.saveAll(inquiryImages);

        // 문의 조회
        InquiryQueryDto inquiryQueryDto = inquiryRepository.getInquiryById(currentUser.getId(), inquiry.getId())
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        List<File> inquiryFiles = new ArrayList<>(retainedImages.size() + inquiryImages.size());

        inquiryFiles.addAll(retainedImages);

        inquiryFiles.addAll(inquiryImages);

        return inquiryMapper.toDto(inquiryQueryDto, inquiryFiles);
    }

    // 문의 삭제
    @Transactional
    public void deleteInquiry(User currentUser, Long inquiryId) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 문의 조회
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        // 문의 작성자와 현재 사용자가 다르면 예외
        if (!inquiry.getUser().getId().equals(currentUser.getId())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_AUTHORIZED);
        }

        // 문의에 답변이 달려있으면 삭제 불가
        if(inquiryReplyRepository.findByInquiry_Id(inquiry.getId()).isPresent()) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_DELETE_NOT_ALLOWED_WITH_REPLY);
        }

        // 문의 삭제
        inquiryRepository.delete(inquiry);

        // 문의에 기존에 등록된 File 목록 조회
        List<File> inquiryImageFiles = fileRepository.findAllByTypeAndTargetId(FileType.INQUIRY, inquiry.getId());

        // 제거할 이미지 URL 목록 생성
        List<String> publicUrls = inquiryImageFiles.stream()
                .map(File::getUrl)
                .toList();

        // AWS 다중 이미지 삭제
        amazonS3Service.deleteFiles(publicUrls);

        // File DB에 제거
        fileRepository.deleteAll(inquiryImageFiles);

    }

    // 이미지 확장자 및 ContentType 검증
    private void validateImagesFiles(List<MultipartFile> ImageFiles) {
        if (ImageFiles == null || ImageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile inquiryImageFile : ImageFiles) {
            if (inquiryImageFile == null || inquiryImageFile.isEmpty()) {
                continue;
            }

            String originalName = inquiryImageFile.getOriginalFilename();

            String extension = Optional.ofNullable(StringUtils.getFilenameExtension(originalName))
                    .orElse("")
                    .toLowerCase();

            boolean validExtension = extension.equals("jpg")
                    || extension.equals("jpeg")
                    || extension.equals("png")
                    || extension.equals("webp");

            if (!validExtension) {
                throw new InquiryException(InquiryErrorCode.INQUIRY_IMAGE_FORMAT_INVALID);
            }

            String contentType = inquiryImageFile.getContentType();

            if (contentType != null && !contentType.isBlank()) {

                boolean validMimeType = ((extension.equals("jpg") || extension.equals("jpeg"))
                        && contentType.equals("image/jpeg"))
                        || (extension.equals("png")
                        && contentType.equals("image/png"))
                        || (extension.equals("webp")
                        && contentType.equals("image/webp"));

                if (!validMimeType) {
                    throw new InquiryException(InquiryErrorCode.INQUIRY_IMAGE_FORMAT_INVALID);
                }
            }
        }
    }

    // 이미지 개수 검증
    private int countValidImagesFiles(List<MultipartFile> ImageFiles) {
        if (ImageFiles == null || ImageFiles.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (MultipartFile imageFile : ImageFiles) {
            if (imageFile == null || imageFile.isEmpty()) {
                continue;
            }
            count++;
        }

        return count;
    }

    // 이미지 업로드
    private List<File> uploadImages(Long inquiryId, List<MultipartFile> ImageFiles) {
        List<File> uploadedFiles = new ArrayList<>();
        List<MultipartFile> uploadFiles = new ArrayList<>();

        if (ImageFiles != null) {
            for (MultipartFile multipartFile : ImageFiles) {
                if (multipartFile == null || multipartFile.isEmpty()) {
                    continue;
                }
                uploadFiles.add(multipartFile);
            }
        }

        if (!uploadFiles.isEmpty()) {
            List<String> uploadPublicUrls = amazonS3Service.uploadFiles(uploadFiles);

            for (int i = 0; i < uploadFiles.size(); i++) {
                MultipartFile multipartFile = uploadFiles.get(i);

                String originalName = multipartFile.getOriginalFilename();

                long size = multipartFile.getSize();

                String extension = Optional.ofNullable(StringUtils.getFilenameExtension(originalName))
                        .orElse("")
                        .toLowerCase();

                String publicUrl = uploadPublicUrls.get(i);

                File file = File.createFile
                        (originalName,
                        size,
                        extension,
                        publicUrl,
                        FileType.INQUIRY,
                        inquiryId
                );

                uploadedFiles.add(file);
            }
        }

        return uploadedFiles;
    }
}
