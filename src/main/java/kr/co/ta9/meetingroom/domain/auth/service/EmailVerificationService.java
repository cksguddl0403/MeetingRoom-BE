package kr.co.ta9.meetingroom.domain.auth.service;

import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationSendDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationSendRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationVerifyDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationVerifyRequestDto;
import kr.co.ta9.meetingroom.domain.auth.enums.EmailVerificationPurpose;
import kr.co.ta9.meetingroom.domain.auth.exception.EmailVerificationException;
import kr.co.ta9.meetingroom.global.error.code.AuthErrorCode;
import kr.co.ta9.meetingroom.global.error.exception.ExternalServiceException;
import kr.co.ta9.meetingroom.infra.mail.dto.MailTxtSendDto;
import kr.co.ta9.meetingroom.infra.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate stringRedisTemplate;
    private final MailService mailService;

    // 이메일 인증 코드 발송
    public EmailVerificationSendDto sendCode(EmailVerificationSendRequestDto emailVerificationSendRequestDto) {
        EmailVerificationPurpose purpose = EmailVerificationPurpose.defaultIfNull(emailVerificationSendRequestDto.getPurpose());
        String email = emailVerificationSendRequestDto.getEmail();

        String key = purpose.codeKey(email);

        String code = generate6Digits();
        stringRedisTemplate.opsForValue().set(key, code, CODE_TTL);

        try {
            mailService.sendSimpleMail(MailTxtSendDto.builder()
                    .emailAddress(email)
                    .subject(mailSubject(purpose))
                    .content(mailBody(purpose, code))
                    .build());
        } catch (MailException e) {
            stringRedisTemplate.delete(key);
            throw new ExternalServiceException(AuthErrorCode.EMAIL_SEND_FAILED);
        }

        return EmailVerificationSendDto.builder()
                .expiresInSeconds(CODE_TTL.toSeconds())
                .build();
    }

    // 이메일 인증 코드 검증
    public EmailVerificationVerifyDto verifyCode(EmailVerificationVerifyRequestDto emailVerificationVerifyRequestDto) {
        EmailVerificationPurpose purpose = EmailVerificationPurpose.defaultIfNull(emailVerificationVerifyRequestDto.getPurpose());
        String email = emailVerificationVerifyRequestDto.getEmail();
        String codeKey = purpose.codeKey(email);
        String saved = stringRedisTemplate.opsForValue().get(codeKey);

        if (saved == null) {
            throw new EmailVerificationException(AuthErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!saved.equals(emailVerificationVerifyRequestDto.getCode())) {
            throw new EmailVerificationException(AuthErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        stringRedisTemplate.delete(codeKey);

        String verificationId = UUID.randomUUID().toString();
        String verifiedKey = purpose.verifiedKey(verificationId);
        stringRedisTemplate.opsForValue().set(verifiedKey, email, VERIFIED_TTL);

        return EmailVerificationVerifyDto.builder()
                .verificationId(verificationId)
                .build();
    }

    // 이메일 제목 생성
    private static String mailSubject(EmailVerificationPurpose purpose) {
        return switch (purpose) {
            case SIGNUP -> "[MeetingRoom] 회원가입 이메일 인증번호";
            case FIND_LOGIN_ID -> "[MeetingRoom] 아이디 찾기 인증번호";
            case FIND_PASSWORD -> "[MeetingRoom] 비밀번호 찾기 인증번호";
        };
    }

    // 이메일 본문 생성
    private static String mailBody(EmailVerificationPurpose purpose, String code) {
        String label = switch (purpose) {
            case SIGNUP -> "회원가입";
            case FIND_LOGIN_ID -> "아이디 찾기";
            case FIND_PASSWORD -> "비밀번호 찾기";
        };
        return label + " 인증번호는 [" + code + "] 입니다.\n"
                + "유효시간은 " + CODE_TTL.toMinutes() + "분 입니다.";
    }

    // 인증 코드 생성
    private static String generate6Digits() {
        int n = RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }
}
