package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-01", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER(HttpStatus.BAD_REQUEST, "USER-02", "이미 존재하는 사용자입니다."),
    INVALID_EMAIL_VERIFICATION(HttpStatus.BAD_REQUEST, "USER-03", "유효하지 않거나 만료된 이메일 인증 정보입니다."),
    CURRENT_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "USER-04", "비밀번호를 변경하려면 기존 비밀번호를 입력해야 합니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "USER-05", "기존 비밀번호가 일치하지 않습니다."),
    USER_PROFILE_IMAGE_INVALID(HttpStatus.BAD_REQUEST, "USER-06", "프로필 이미지는 JPG/PNG/WEBP 형식만 업로드할 수 있습니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "USER-07", "이미 존재하는 닉네임입니다."),
    NOT_CERTIFICATED_USER(HttpStatus.FORBIDDEN, "USER-08", "인증되지 않은 사용자입니다. 관리자에게 문의하세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
