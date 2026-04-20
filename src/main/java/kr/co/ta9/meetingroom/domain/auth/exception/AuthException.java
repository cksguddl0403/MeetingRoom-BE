package kr.co.ta9.meetingroom.domain.auth.exception;

import kr.co.ta9.meetingroom.global.error.code.AuthErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class AuthException extends BusinessException {
    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}

