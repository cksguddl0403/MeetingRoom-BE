package kr.co.ta9.meetingroom.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.ta9.meetingroom.domain.auth.dto.AvailabilityResponseDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.LoginIdAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.NicknameAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.mapper.AuthMapper;
import kr.co.ta9.meetingroom.domain.user.repository.UserRepository;
import kr.co.ta9.meetingroom.global.security.util.CookieUtils;
import kr.co.ta9.meetingroom.global.security.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final AuthMapper authMapper;

    // 엑세스 토큰 갱신
    @Transactional
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.getCookieValue(request, "RefreshToken");

        if(refreshToken == null) {
            throw new BadCredentialsException("쿠키에 리프래쉬 토큰이 존재하지 않습니다.");
        }

        if(!jwtUtils.isRefreshTokenValid(refreshToken)) {
            CookieUtils.deleteCookie(request, response, "RefreshToken");
            throw new BadCredentialsException("쿠키에 있는 리프래쉬 토큰이 유효하지 않습니다.");
        }

        Long userId = jwtUtils.extractIdFromRefreshToken(refreshToken);

        String storedRefreshToken = stringRedisTemplate.opsForValue().get("refresh_token:" + userId);

        if(storedRefreshToken == null) {
            throw new BadCredentialsException("저장소에 리프래쉬 토큰이 존재하지 않습니다. 다시 로그인 해주세요.");
        }

        if(!storedRefreshToken.equals(refreshToken)) {
            stringRedisTemplate.delete("refresh_token:" + userId);
            CookieUtils.deleteCookie(request, response, "RefreshToken");
            throw new BadCredentialsException("리프래쉬 토큰이 일치하지 않습니다. 다시 로그인 해주세요.");
        }

        // 엑세스 토큰 재발급
        String accessToken = jwtUtils.createAccessToken(userId);

        // 리프래쉬 토큰 재발급
        String newRefreshToken = jwtUtils.createRefreshToken(userId);

        // RTR 정책에 따라 리프래쉬 토큰 갱신
        long ttlMs = jwtUtils.getRefreshTokenRemainingExpiration(newRefreshToken);
        stringRedisTemplate.opsForValue().set(
                "refresh_token:" + userId,
                newRefreshToken,
                Math.max(1L, ttlMs),
                TimeUnit.MILLISECONDS
        );

        // 재발급 된 리프레쉬 토큰 응답 헤더와 쿠키에 담아주기
        response.setHeader("Authorization", "Bearer " + accessToken);
        CookieUtils.addCookie(response, "RefreshToken", newRefreshToken);
    }

    // 아이디 중복 체크
    public AvailabilityResponseDto checkLoginIdAvailability(LoginIdAvailabilityRequestDto loginIdAvailabilityRequestDto) {
        boolean available = !userRepository.existsByLoginId(loginIdAvailabilityRequestDto.getLoginId());
        return AvailabilityResponseDto.builder()
                .available(available)
                .build();
    }

    // 닉네임 중복 체크
    public AvailabilityResponseDto checkNicknameAvailability(NicknameAvailabilityRequestDto nicknameAvailabilityRequestDto) {
        boolean available = !userRepository.existsByNickname(nicknameAvailabilityRequestDto.getNickname());
        return AvailabilityResponseDto.builder()
                .available(available)
                .build();
    }

    // 이메일 중복 체크
    public AvailabilityResponseDto checkEmailAvailability(EmailAvailabilityRequestDto emailAvailabilityRequestDto) {
        boolean available = !userRepository.existsByEmail(emailAvailabilityRequestDto.getEmail());
        return AvailabilityResponseDto.builder()
                .available(available)
                .build();
    }

    // 로그아웃
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.getCookieValue(request, "RefreshToken");

        if(refreshToken == null) {
            throw new AccessDeniedException("쿠키에 리프래쉬 토큰이 존재하지 않습니다.");
        }

        if(!jwtUtils.isRefreshTokenValid(refreshToken)) {
            CookieUtils.deleteCookie(request, response, "RefreshToken");
            throw new AccessDeniedException("쿠키에 있는 리프래쉬 토큰이 유효하지 않습니다.");
        }

        // 여기서 부터 쿠키에 리프래쉬 토큰이 존재하고 유효한 경우
        Long userId = jwtUtils.extractIdFromRefreshToken(refreshToken);
        stringRedisTemplate.delete("refresh_token:" + userId);

        CookieUtils.deleteCookie(request, response, "RefreshToken");
    }
}
