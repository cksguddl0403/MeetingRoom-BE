package kr.co.ta9.meetingroom.domain.inquiry.mapper;

import kr.co.ta9.meetingroom.domain.file.entity.File;
import kr.co.ta9.meetingroom.domain.inquiry.dto.*;
import kr.co.ta9.meetingroom.domain.inquiry.entity.Inquiry;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InquiryMapper {

    default InquiryDto toDto(Inquiry inquiry, List<File> inquiryFiles) {
        if (inquiry == null) {
            return null;
        }
        return InquiryDto.builder()
                .id(inquiry.getId())
                .category(InquiryCategoryDto.builder()
                        .id(inquiry.getInquiryCategory().getId())
                        .name(inquiry.getInquiryCategory().getName())
                        .build())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .author(InquiryAuthorDto.builder()
                        .id(inquiry.getUser().getId())
                        .nickname(inquiry.getUser().getNickname())
                        .build())
                .answered(false)
                .secret(inquiry.isSecret())
                .createdAt(inquiry.getCreatedAt())
                .reply(null)
                .images(
                        inquiryFiles == null || inquiryFiles.isEmpty()
                                ? List.of()
                                : inquiryFiles.stream()
                                .map(file -> InquiryImageDto.builder()
                                        .id(file.getId())
                                        .url(file.getUrl())
                                        .build())
                                .toList()
                )
                .build();
    }

    default InquiryDto toDto(InquiryQueryDto inquiryQueryDto, List<File> inquiryFiles) {
        if (inquiryQueryDto == null) {
            return null;
        }
        InquiryAuthorDto authorDto = null;
        if (inquiryQueryDto.getAuthor() != null) {
            authorDto = InquiryAuthorDto.builder()
                    .id(inquiryQueryDto.getAuthor().getId())
                    .nickname(inquiryQueryDto.getAuthor().getName())
                    .build();
        }

        InquiryReplyDto replyDto = null;
        if (inquiryQueryDto.getReply() != null && inquiryQueryDto.getReply().getId() != null) {
            InquiryReplyAuthorDto replyAuthorDto = null;
            if (inquiryQueryDto.getReply().getAuthor() != null) {
                replyAuthorDto = InquiryReplyAuthorDto.builder()
                        .id(inquiryQueryDto.getReply().getAuthor().getId())
                        .nickname(inquiryQueryDto.getReply().getAuthor().getName())
                        .build();
            }
            replyDto = InquiryReplyDto.builder()
                    .content(inquiryQueryDto.getReply().getContent())
                    .author(replyAuthorDto)
                    .build();
        }

        return InquiryDto.builder()
                .id(inquiryQueryDto.getId())
                .category(InquiryCategoryDto.builder()
                        .id(inquiryQueryDto.getCategoryId())
                        .name(inquiryQueryDto.getCategoryName())
                        .build())
                .title(inquiryQueryDto.getTitle())
                .content(inquiryQueryDto.getContent())
                .author(authorDto)
                .answered(inquiryQueryDto.isAnswered())
                .secret(inquiryQueryDto.isSecret())
                .createdAt(inquiryQueryDto.getCreatedAt())
                .reply(replyDto)
                .images(
                        inquiryFiles == null || inquiryFiles.isEmpty()
                                ? List.of()
                                : inquiryFiles.stream()
                                .map(file -> InquiryImageDto.builder()
                                        .id(file.getId())
                                        .url(file.getUrl())
                                        .build())
                                .toList()
                )
                .build();
    }

    default InquiryListDto toListDto(InquiryQueryDto inquiryQueryDto, List<File> inquiryFiles) {
        if (inquiryQueryDto == null) {
            return null;
        }
        InquiryAuthorDto authorDto = null;
        if (inquiryQueryDto.getAuthor() != null) {
            authorDto = InquiryAuthorDto.builder()
                    .id(inquiryQueryDto.getAuthor().getId())
                    .nickname(inquiryQueryDto.getAuthor().getName())
                    .build();
        }

        return InquiryListDto.builder()
                .id(inquiryQueryDto.getId())
                .category(InquiryCategoryDto.builder()
                        .id(inquiryQueryDto.getCategoryId())
                        .name(inquiryQueryDto.getCategoryName())
                        .build())
                .title(inquiryQueryDto.getTitle())
                .author(authorDto)
                .answered(inquiryQueryDto.isAnswered())
                .secret(inquiryQueryDto.isSecret())
                .createdAt(inquiryQueryDto.getCreatedAt())
                .build();
    }
}
