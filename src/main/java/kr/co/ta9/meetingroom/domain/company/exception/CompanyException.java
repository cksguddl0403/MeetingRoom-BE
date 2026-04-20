package kr.co.ta9.meetingroom.domain.company.exception;

import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class CompanyException extends BusinessException {
    public CompanyException(CompanyErrorCode errorCode) {
        super(errorCode);
    }
}

