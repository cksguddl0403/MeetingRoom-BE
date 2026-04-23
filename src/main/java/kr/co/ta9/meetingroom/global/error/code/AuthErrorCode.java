package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH-01", "인증번호가 만료되었습니다. 재전송 해주세요."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-02", "인증번호가 일치하지 않습니다."),
    EMAIL_SEND_FAILED(HttpStatus.BAD_GATEWAY, "AUTH-03", "이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요."),
    ACCOUNT_RECOVERY_INFO_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-04", "입력하신 정보와 일치하는 회원이 없습니다."),
    ACCOUNT_VERIFICATION_EXPIRED_OR_INVALID(HttpStatus.BAD_REQUEST, "AUTH-5", "유효하지 않거나 만료된 계정 인증 정보입니다."),
    ACCOUNT_VERIFICATION_EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-6", "이메일 인증 정보와 요청에 담긴 이메일이 일치하지 않습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-7", "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요."),
    NOT_CERTIFIED_USER(HttpStatus.UNAUTHORIZED, "AUTH-8", "인증된 사용자만 이용할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

