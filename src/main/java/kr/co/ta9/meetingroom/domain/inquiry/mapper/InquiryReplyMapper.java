package kr.co.ta9.meetingroom.domain.inquiry.mapper;

import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryReplyAuthorDto;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryReplyDto;
import kr.co.ta9.meetingroom.domain.inquiry.entity.InquiryReply;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InquiryReplyMapper {

    default InquiryReplyDto toDto(InquiryReply inquiryReply) {
        if (inquiryReply == null) {
            return null;
        }
        return InquiryReplyDto.builder()
                .content(inquiryReply.getContent())
                .author(null)
                .build();
    }

    default InquiryReplyDto toMaskedDto(String maskedText) {
        return InquiryReplyDto.builder()
                .content(maskedText)
                .author(InquiryReplyAuthorDto.builder()
                        .id(null)
                        .nickname(maskedText)
                        .build())
                .build();
    }
}

