package kr.co.ta9.meetingroom.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import kr.co.ta9.meetingroom.global.error.code.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String code;
    private String message;
    private List<FieldError> fieldErrors;

    @Builder
    private ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.fieldErrors = fieldErrors;
    }

    private ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(
                errorCode,
                FieldError.of(bindingResult)
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, ConstraintViolationException ex) {
        return new ErrorResponse(
                errorCode,
                ex.getConstraintViolations().stream()
                        .map(FieldError::of)
                        .toList()
        );
    }

    @Getter
    static class FieldError {
        private final String field;
        private final Object rejectedValue;
        private final String message;

        @Builder
        private FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue(),
                            error.getDefaultMessage()
                    ))
                    .toList();
        }

        private static FieldError of(ConstraintViolation<?> violation) {
            return new FieldError(
                    violation.getPropertyPath().toString(),
                    violation.getInvalidValue(),
                    violation.getMessage()
            );
        }
    }
}