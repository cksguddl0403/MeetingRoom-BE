package kr.co.ta9.meetingroom.domain.inquiry.repository;

import kr.co.ta9.meetingroom.domain.inquiry.entity.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {

    /*
     * 문의 ID로 답변 단건을 조회합니다.
     *
     * SELECT ir.*
     * FROM inquiry_reply ir
     * WHERE ir.inquiry_id = ?
     * LIMIT 1
     */
    Optional<InquiryReply> findByInquiry_Id(Long inquiryId);
}

