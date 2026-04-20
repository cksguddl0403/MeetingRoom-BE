package kr.co.ta9.meetingroom.domain.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * 이메일 인증 용도별 Redis 키 접두사를 구분합니다.
 */
@Getter
@RequiredArgsConstructor
public enum EmailVerificationPurpose {
    /* 회원가입 이메일 인증 */
    SIGNUP("signup"),
    /* 아이디 찾기 */
    FIND_LOGIN_ID("find-login-id"),
    /* 비밀번호 찾기 */
    FIND_PASSWORD("find-password");

    private final String redisKeyPrefix;

    public String codeKey(String email) {
        return redisKeyPrefix + ":email:code:" + email;
    }

    public String verifiedKey(String verificationId) {
        return redisKeyPrefix + ":email:verified:" + verificationId;
    }

    public static EmailVerificationPurpose defaultIfNull(EmailVerificationPurpose purpose) {
        return purpose != null ? purpose : SIGNUP;
    }
}
