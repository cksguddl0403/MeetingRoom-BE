package kr.co.ta9.meetingroom.domain.inspection.exception;

import kr.co.ta9.meetingroom.global.error.code.InspectionErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class InspectionException extends BusinessException {
    public InspectionException(InspectionErrorCode errorCode) {
        super(errorCode);
    }
}
