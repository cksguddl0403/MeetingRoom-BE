package kr.co.ta9.meetingroom.domain.user.exception;

import kr.co.ta9.meetingroom.global.error.code.UserErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class UserException extends BusinessException {
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}
