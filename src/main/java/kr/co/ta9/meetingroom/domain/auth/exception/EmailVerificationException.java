package kr.co.ta9.meetingroom.domain.auth.exception;

import kr.co.ta9.meetingroom.global.error.code.ErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class EmailVerificationException extends BusinessException {
    public EmailVerificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
