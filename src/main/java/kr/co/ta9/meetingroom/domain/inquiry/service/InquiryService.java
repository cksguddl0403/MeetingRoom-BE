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
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.CategoryErrorCode;
import kr.co.ta9.meetingroom.global.error.code.InquiryErrorCode;
import kr.co.ta9.meetingroom.infra.s3.service.AmazonS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final CategoryRepository categoryRepository;
    private final FileRepository fileRepository;
    private final InquiryMapper inquiryMapper;
    private final AmazonS3Service amazonS3Service;

    // 문의 등록
    @Transactional
    public InquiryDto createInquiry(User currentUser, InquiryCreateRequestDto inquiryCreateRequestDto, List<MultipartFile> imageFiles) {
        Category category = categoryRepository.findById(inquiryCreateRequestDto.getInquiryCategoryId())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        if (category.getType() != CategoryType.INQUIRY) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_CATEGORY_INVALID_TYPE);
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile == null || imageFile.isEmpty()) {
                    continue;
                }

                String originalName = imageFile.getOriginalFilename();
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

                String contentType = imageFile.getContentType();
                if (contentType != null && !contentType.isBlank()) {
                    String normalizedContentType = contentType.toLowerCase();
                    boolean validMimeType = ((extension.equals("jpg") || extension.equals("jpeg"))
                            && normalizedContentType.equals("image/jpeg"))
                            || (extension.equals("png")
                            && normalizedContentType.equals("image/png"))
                            || (extension.equals("webp")
                            && normalizedContentType.equals("image/webp"));

                    if (!validMimeType) {
                        throw new InquiryException(InquiryErrorCode.INQUIRY_IMAGE_FORMAT_INVALID);
                    }
                }
            }
        }

        boolean secret = Boolean.TRUE.equals(inquiryCreateRequestDto.getIsPrivate());

        Inquiry inquiry = Inquiry.createInquiry(
                currentUser,
                category,
                inquiryCreateRequestDto.getTitle(),
                inquiryCreateRequestDto.getContent(),
                secret
        );
        inquiryRepository.save(inquiry);

        List<File> inquiryFiles = saveInquiryImagesFromMultipart(inquiry.getId(), imageFiles);
        return inquiryMapper.toDto(inquiry, inquiryFiles);
    }

    // 문의 상세 조회
    public InquiryDto getInquiry(User currentUser, Long id) {
        InquiryQueryDto inquiryQueryDto = inquiryRepository.getInquiryById(currentUser.getId(), id)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        if(inquiryQueryDto.isSecret() && !inquiryQueryDto.getAuthor().getId().equals(currentUser.getId())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_AUTHORIZED);
        }

        List<File> inquiryFiles = fileRepository.findAllByTypeAndTargetId(FileType.INQUIRY, inquiryQueryDto.getId());

        return inquiryMapper.toDto(inquiryQueryDto, inquiryFiles);
    }

    // 문의 목록 조회
    public OffsetPageResponseDto<InquiryListDto> getInquiries(User currentUser, Pageable pageable, InquiryListSearchRequestDto inquiryListSearchRequestDto) {
        Page<InquiryQueryDto> page = inquiryRepository.getInquiries(currentUser.getId(), inquiryListSearchRequestDto, pageable);

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
    public InquiryDto updateInquiry(User currentUser, Long inquiryId, InquiryUpdateRequestDto inquiryUpdateRequestDto, List<MultipartFile> imageFiles) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        if (!inquiry.getUser().getId().equals(currentUser.getId())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_AUTHORIZED);
        }

        List<File> existingImages = fileRepository.findAllByTypeAndTargetId(FileType.INQUIRY, inquiry.getId());

        boolean secret = Boolean.TRUE.equals(inquiryUpdateRequestDto.getIsPrivate());

        inquiry.update(inquiryUpdateRequestDto.getTitle(), inquiryUpdateRequestDto.getContent(), secret);

        List<String> retainUrls = inquiryUpdateRequestDto.getRetainImageUrls();

        Set<String> retainSet = retainUrls.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(HashSet::new));

        for (String retainUrl : retainSet) {
            boolean owned = existingImages.stream().anyMatch(f -> retainUrl.equals(f.getUrl()));
            if (!owned) {
                throw new InquiryException(InquiryErrorCode.INQUIRY_IMAGE_INVALID);
            }
        }

        // 제거할 파일 목록 생성
        List<File> toRemove = existingImages.stream()
                .filter(f -> !retainSet.contains(f.getUrl()))
                .toList();

        // AWS S3에 실제로 제거
        deleteFilesFromStorage(toRemove);

        // 이미 존재하는 파일 목록 생성
        existingImages = existingImages.stream()
                .filter(f -> retainSet.contains(f.getUrl()))
                .toList();

        // 새롭게 업로드된 파일 저장
        List<File> newlyUploaded = saveInquiryImagesFromMultipart(inquiry.getId(), imageFiles);

        List<File> inquiryFiles = new ArrayList<>(existingImages.size() + newlyUploaded.size());

        inquiryFiles.addAll(existingImages);
        inquiryFiles.addAll(newlyUploaded);

        InquiryQueryDto inquiryQueryDto = inquiryRepository.getInquiryById(currentUser.getId(), inquiry.getId())
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        return inquiryMapper.toDto(inquiryQueryDto, inquiryFiles);
    }

    // 문의 삭제
    @Transactional
    public void deleteInquiry(User currentUser, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));
        if (!inquiry.getUser().getId().equals(currentUser.getId())) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_NOT_AUTHORIZED);
        }

        if(inquiryReplyRepository.findByInquiry_Id(inquiry.getId()).isPresent()) {
            throw new InquiryException(InquiryErrorCode.INQUIRY_DELETE_NOT_ALLOWED_WITH_REPLY);
        }

        List<File> inquiryImageFiles = fileRepository.findAllByTypeAndTargetId(FileType.INQUIRY, inquiry.getId());
        deleteFilesFromStorage(inquiryImageFiles);

        inquiryRepository.delete(inquiry);
    }

    // 문의 이미지 저장
    private List<File> saveInquiryImagesFromMultipart(Long inquiryId, List<MultipartFile> imageFiles) {
        List<File> savedFiles = new ArrayList<>();
        if (imageFiles == null || imageFiles.isEmpty()) {
            return savedFiles;
        }
        for (MultipartFile multipartFile : imageFiles) {
            if (multipartFile == null || multipartFile.isEmpty()) {
                continue;
            }
            String publicUrl = amazonS3Service.uploadFile(multipartFile);
            String originalName = multipartFile.getOriginalFilename();
            String name = StringUtils.hasText(originalName) ? originalName : "inquiry-image";
            long size = multipartFile.getSize();
            String extension = Optional.ofNullable(StringUtils.getFilenameExtension(originalName))
                    .orElse("");
            File saved = fileRepository.save(File.createFile(
                    name,
                    size,
                    extension,
                    publicUrl,
                    FileType.INQUIRY,
                    inquiryId
            ));
            savedFiles.add(saved);
        }
        return savedFiles;
    }

    // 문의 이미지 삭제
    private void deleteFilesFromStorage(List<File> files) {
        for (File file : files) {
            amazonS3Service.deleteFile(file.getUrl());
            fileRepository.delete(file);
        }
    }
}
