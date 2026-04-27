package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-01", "사용자를 찾을 수 없습니다."),
    INVALID_EMAIL_VERIFICATION(HttpStatus.BAD_REQUEST, "USER-03", "유효하지 않거나 만료된 이메일 인증 정보입니다. 기존 인증번호를 지우고 다시 인증을 시도하세요."),
    CURRENT_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "USER-04", "비밀번호를 변경하려면 기존 비밀번호를 입력해야 합니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "USER-05", "기존 비밀번호가 일치하지 않습니다."),
    USER_PROFILE_IMAGE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "USER-06", "프로필 이미지는 JPG/PNG/WEBP 형식만 업로드할 수 있습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST, "USER-07", "이미 존재하는 로그인 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "USER-08", "이미 존재하는 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "USER-09", "이미 존재하는 이메일입니다."),
    NOT_CERTIFICATED_USER(HttpStatus.FORBIDDEN, "USER-10", "인증되지 않은 사용자입니다. 관리자에게 문의하세요."),
    USER_PROFILE_IMAGE_NAME_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "USER-11", "프로필 이미지 파일명은 255자를 초과할 수 없습니다."),
    USER_PROFILE_IMAGE_SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "USER-12", "프로필 이미지 파일 시그니처가 확장자와 일치하지 않습니다."),
    EMPLOYMENT_CERTIFICATE_SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "USER-13", "재직 증명서 파일 시그니처가 확장자와 일치하지 않습니다."),
    SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "USER-14", "새 비밀번호는 기존 비밀번호와 달라야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
