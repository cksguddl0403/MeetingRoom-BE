package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AmazonS3ErrorCode implements ErrorCode {
    FAILED_TO_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "AMAZONS3-01", "파일 업로드를 실패했습니다"),
    FAILED_TO_DELETE_OBJECT(HttpStatus.INTERNAL_SERVER_ERROR, "AMAZONS3-02", "파일 삭제에 실패했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}