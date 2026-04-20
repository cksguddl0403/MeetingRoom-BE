package kr.co.ta9.meetingroom.domain.inquiry.repository;

import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.inquiry.dto.InquiryQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface InquiryRepositoryCustom {
    Optional<InquiryQueryDto> getInquiryById(Long currentUserId, Long inquiryId);
    Page<InquiryQueryDto> getInquiries(Long currentUserId, InquiryListSearchRequestDto inquiryListSearchRequestDto, Pageable pageable);
}
