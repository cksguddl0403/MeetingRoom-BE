package kr.co.ta9.meetingroom.domain.reservation.exception;

import kr.co.ta9.meetingroom.global.error.code.ReservationErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class ReservationException extends BusinessException {
    public ReservationException(ReservationErrorCode errorCode) {
        super(errorCode);
    }
}

