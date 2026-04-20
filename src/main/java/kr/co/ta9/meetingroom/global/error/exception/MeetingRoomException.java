package kr.co.ta9.meetingroom.global.error.exception;

import kr.co.ta9.meetingroom.global.error.code.ErrorCode;
import lombok.Getter;

@Getter
public class MeetingRoomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public MeetingRoomException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
}
