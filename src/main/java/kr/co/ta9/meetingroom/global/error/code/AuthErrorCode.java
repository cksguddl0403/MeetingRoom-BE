package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-01", "잘못된 인증 정보입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-02", "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-03", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-04", "리프레시 토큰을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "AUTH-05", "접근 권한이 없습니다."),

    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH-06", "인증번호가 만료되었습니다. 재전송 해주세요."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-07", "인증번호가 일치하지 않습니다."),
    EMAIL_SEND_FAILED(HttpStatus.BAD_GATEWAY, "AUTH-08", "이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요."),
    ACCOUNT_RECOVERY_INFO_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-09", "입력하신 정보와 일치하는 회원이 없습니다."),
    ACCOUNT_RECOVERY_FIELD_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH-10", "요청에 필요한 정보가 누락되었습니다."),
    ACCOUNT_VERIFICATION_EXPIRED_OR_INVALID(HttpStatus.BAD_REQUEST, "AUTH-11", "유효하지 않거나 만료된 계정 인증 정보입니다."),
    ACCOUNT_VERIFICATION_EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-12", "이메일 인증 정보와 요청에 담긴 이메일이 일치하지 않습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-13", "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

