package kr.co.ta9.meetingroom.domain.equipment.exception;

import kr.co.ta9.meetingroom.global.error.code.EquipmentErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class EquipmentException extends BusinessException {
    public EquipmentException(EquipmentErrorCode errorCode) {
        super(errorCode);
    }
}

