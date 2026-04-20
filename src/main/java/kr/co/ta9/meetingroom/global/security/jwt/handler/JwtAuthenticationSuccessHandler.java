package kr.co.ta9.meetingroom.global.security.jwt.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.ta9.meetingroom.global.security.jwt.CustomUserDetails;
import kr.co.ta9.meetingroom.global.security.util.CookieUtils;
import kr.co.ta9.meetingroom.global.security.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.createAccessToken(customUserDetails.getId());

        String refreshToken = jwtUtils.createRefreshToken(customUserDetails.getId());

        long ttlMs = jwtUtils.getRefreshTokenRemainingExpiration(refreshToken);
        stringRedisTemplate.opsForValue().set(
                "refresh_token:" + customUserDetails.getId(),
                refreshToken,
                Math.max(1L, ttlMs),
                TimeUnit.MILLISECONDS
        );

        // Access Token을 HEADER로 전달
        response.setHeader("Authorization", "Bearer " + accessToken);

       // Refresh Token을 HttpOnly Cookie로 전달
        CookieUtils.addCookie(response, "RefreshToken", refreshToken);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
