package kr.co.ta9.meetingroom.domain.room.exception;

import kr.co.ta9.meetingroom.global.error.code.RoomErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class RoomException extends BusinessException {
    public RoomException(RoomErrorCode errorCode) {
        super(errorCode);
    }
}

