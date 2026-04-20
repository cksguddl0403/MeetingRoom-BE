package kr.co.ta9.meetingroom.global.error.exception;

import kr.co.ta9.meetingroom.global.error.code.ErrorCode;

public class ExternalServiceException extends MeetingRoomException {
    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
