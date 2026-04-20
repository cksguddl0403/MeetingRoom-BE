package kr.co.ta9.meetingroom.domain.notice.exception;

import kr.co.ta9.meetingroom.global.error.code.NoticeErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class NoticeException extends BusinessException {
    public NoticeException(NoticeErrorCode errorCode) {
        super(errorCode);
    }
}

