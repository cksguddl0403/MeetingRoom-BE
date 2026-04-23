package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements ErrorCode {
    INQUIRY_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY-03", "문의 유형을 찾을 수 없습니다."),
    INQUIRY_CATEGORY_INVALID_TYPE(HttpStatus.BAD_REQUEST, "INQUIRY-04", "문의에 사용할 수 있는 유형이 아닙니다."),

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY-01", "문의를 찾을 수 없습니다."),
    INQUIRY_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "INQUIRY-02", "문의 권한이 없습니다."),

    INQUIRY_IMAGE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY-07", "문의 이미지는 JPG/PNG/WEBP 형식만 업로드할 수 있습니다."),
    INQUIRY_IMAGE_INVALID(HttpStatus.BAD_REQUEST, "INQUIRY-06", "이 문의에 속하지 않는 이미지입니다."),
    INQUIRY_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "INQUIRY-08", "문의 이미지는 최대 5개까지 등록할 수 있습니다."),

    INQUIRY_DELETE_NOT_ALLOWED_WITH_REPLY(HttpStatus.BAD_REQUEST, "INQUIRY-05", "답변이 등록된 문의는 삭제할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

