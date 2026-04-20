package kr.co.ta9.meetingroom.global.error.exception;

import kr.co.ta9.meetingroom.global.error.code.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends MeetingRoomException {
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}