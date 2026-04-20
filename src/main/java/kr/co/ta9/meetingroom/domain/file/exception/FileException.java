package kr.co.ta9.meetingroom.domain.file.exception;

import kr.co.ta9.meetingroom.global.error.code.FileErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class FileException extends BusinessException {
    public FileException(FileErrorCode errorCode) {
        super(errorCode);
    }
}
