package kr.co.ta9.meetingroom.domain.inquiry.exception;

import kr.co.ta9.meetingroom.domain.inquiry.enums.InquiryReplyErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class InquiryReplyException extends BusinessException {
    public InquiryReplyException(InquiryReplyErrorCode errorCode) {
        super(errorCode);
    }
}
