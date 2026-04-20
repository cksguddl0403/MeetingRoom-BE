package kr.co.ta9.meetingroom.infra.s3.exception;

import kr.co.ta9.meetingroom.global.error.code.ErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.ExternalServiceException;
import lombok.Getter;

@Getter
public class AwsException extends ExternalServiceException {
    public AwsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
