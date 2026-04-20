package kr.co.ta9.meetingroom.domain.auth.service;

import kr.co.ta9.meetingroom.domain.auth.dto.AvailabilityResponseDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindLoginIdRevealRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindLoginIdRevealResponseDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindLoginIdAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindPasswordAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindPasswordResetRequestDto;
import kr.co.ta9.meetingroom.domain.auth.enums.EmailVerificationPurpose;
import kr.co.ta9.meetingroom.domain.auth.exception.AuthException;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.repository.UserRepository;
import kr.co.ta9.meetingroom.global.error.code.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {
    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 아이디 찾기 가능 여부
    public AvailabilityResponseDto checkFindLoginIdAvailability(FindLoginIdAvailabilityRequestDto findLoginIdAvailabilityRequestDto) {
        boolean available = userRepository.findByNameAndEmail(findLoginIdAvailabilityRequestDto.getName(), findLoginIdAvailabilityRequestDto.getEmail()).isPresent();
        return AvailabilityResponseDto.builder()
                .available(available)
                .build();
    }

    // 비밀번호 찾기 가능 여부
    public AvailabilityResponseDto checkFindPasswordAvailability(FindPasswordAvailabilityRequestDto findPasswordAvailabilityRequestDto) {
        boolean available = userRepository.findByLoginId(findPasswordAvailabilityRequestDto.getLoginId())
                .map(user -> user.getEmail().equals(findPasswordAvailabilityRequestDto.getEmail()))
                .orElse(false);
        return AvailabilityResponseDto.builder()
                .available(available)
                .build();
    }

    // 아이디 찾기
    public FindLoginIdRevealResponseDto findLoginId(FindLoginIdRevealRequestDto findLoginIdRevealRequestDto) {
        String key = EmailVerificationPurpose.FIND_LOGIN_ID.verifiedKey(findLoginIdRevealRequestDto.getVerificationId());
        String email = stringRedisTemplate.opsForValue().get(key);

        if (email == null) {
            throw new AuthException(AuthErrorCode.ACCOUNT_VERIFICATION_EXPIRED_OR_INVALID);
        }

        assertRedisEmailMatchesRequest(email, findLoginIdRevealRequestDto.getEmail());

        User user = userRepository.findByNameAndEmail(findLoginIdRevealRequestDto.getName(), email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH));

        stringRedisTemplate.delete(key);

        return FindLoginIdRevealResponseDto.builder()
                .loginId(user.getLoginId())
                .build();
    }

    // 비밀번호 초기화 요청
    @Transactional
    public void resetPassword(FindPasswordResetRequestDto findPasswordResetRequestDto) {
        String key = EmailVerificationPurpose.FIND_PASSWORD.verifiedKey(findPasswordResetRequestDto.getVerificationId());

        String email = stringRedisTemplate.opsForValue().get(key);

        if (email == null) {
            throw new AuthException(AuthErrorCode.ACCOUNT_VERIFICATION_EXPIRED_OR_INVALID);
        }

        assertRedisEmailMatchesRequest(email, findPasswordResetRequestDto.getEmail());

        User user = userRepository.findByLoginId(findPasswordResetRequestDto.getLoginId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH));

        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new AuthException(AuthErrorCode.ACCOUNT_RECOVERY_INFO_MISMATCH);
        }

        stringRedisTemplate.delete(key);

        user.updateInfo(passwordEncoder.encode(findPasswordResetRequestDto.getNewPassword()), null);
    }

    // 인증 이메일 일치 검증
    private static void assertRedisEmailMatchesRequest(String redisEmail, String requestEmail) {
        if (!redisEmail.equals(requestEmail)) {
            throw new AuthException(AuthErrorCode.ACCOUNT_VERIFICATION_EMAIL_MISMATCH);
        }
    }
}
