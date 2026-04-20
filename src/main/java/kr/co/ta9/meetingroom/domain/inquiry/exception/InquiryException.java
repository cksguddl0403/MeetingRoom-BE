package kr.co.ta9.meetingroom.domain.inquiry.exception;

import kr.co.ta9.meetingroom.global.error.code.InquiryErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class InquiryException extends BusinessException {
    public InquiryException(InquiryErrorCode errorCode) {
        super(errorCode);
    }
}

