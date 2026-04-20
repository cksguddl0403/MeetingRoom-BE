package kr.co.ta9.meetingroom.domain.category.exception;

import kr.co.ta9.meetingroom.global.error.code.CategoryErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.BusinessException;

public class CategoryException extends BusinessException {
    public CategoryException(CategoryErrorCode errorCode) {
        super(errorCode);
    }
}
