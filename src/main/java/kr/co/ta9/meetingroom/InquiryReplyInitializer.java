package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.inquiry.entity.Inquiry;
import kr.co.ta9.meetingroom.domain.inquiry.entity.InquiryReply;
import kr.co.ta9.meetingroom.domain.inquiry.exception.InquiryException;
import kr.co.ta9.meetingroom.domain.inquiry.repository.InquiryReplyRepository;
import kr.co.ta9.meetingroom.domain.inquiry.repository.InquiryRepository;
import kr.co.ta9.meetingroom.global.error.code.InquiryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(11)
@RequiredArgsConstructor
public class InquiryReplyInitializer implements ApplicationRunner {
    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
/*
        Inquiry inquiry = inquiryRepository.findById(1L)
                .orElseThrow(() -> new InquiryException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        InquiryReply reply = InquiryReply.createInquiryReply("테스트 답변 내용입니다.");
        reply.setInquiry(inquiry);
        inquiryReplyRepository.save(reply);
*/
    }
}
