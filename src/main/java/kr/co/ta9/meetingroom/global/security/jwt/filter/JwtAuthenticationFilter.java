package kr.co.ta9.meetingroom.global.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.ta9.meetingroom.global.security.CustomUserPrincipal;
import kr.co.ta9.meetingroom.global.security.util.CookieUtils;
import kr.co.ta9.meetingroom.global.security.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePaths = {"/api/auth/sign-in"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePaths).anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);

        // 토큰이 없으면 바로 다음 필터로 => 익명
        if(accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 여기서부터 토큰이 있는 경우

        // 유효하지 않은 토큰일 경우 => Cookie에 있는 리플래쉬 토큰으로 엑세스 토큰 재발급 시도
        if(!jwtUtils.isAccessTokenValid(accessToken)) {
            log.info("유효하지 않은 엑세스 토큰입니다. 쿠키에 있는 리프래쉬 토큰으로 엑세스 토큰 재발급을 시도합니다.");
            String refreshToken = CookieUtils.getCookieValue(request, "RefreshToken");

            if(refreshToken == null) {
                throw new BadCredentialsException("쿠키에 리프래쉬 토큰이 존재하지 않습니다.");
            }

            if(!jwtUtils.isRefreshTokenValid(refreshToken)) {
                CookieUtils.deleteCookie(request, response, "RefreshToken");
                throw new BadCredentialsException("쿠키에 있는 리프래쉬 토큰이 유효하지 않습니다.");
            }

            // 여기서 부터 쿠키에 리프래쉬 토큰이 존재하고 유효한 경우

            // 리프래쉬 토큰 안에 있는 userId로 기존의 리프래쉬 토큰 조회
            Long userId = jwtUtils.extractIdFromRefreshToken(refreshToken);

            String storedRefreshToken = stringRedisTemplate.opsForValue().get("refresh_token:" + userId);

            if(storedRefreshToken == null) {
                throw new BadCredentialsException("저장소에 리프래쉬 토큰이 존재하지 않습니다. 다시 로그인 해주세요.");
            }

            // 쿠키에 있는 리프래쉬 토큰이 Redis에 존재 하나
            // 값이 다르다면 리프래쉬 토큰이 탈취된 것으로 간주하고 리프래쉬 토큰 삭제
            if(!storedRefreshToken.equals(refreshToken)) {
                stringRedisTemplate.delete("refresh_token:" + userId);
                CookieUtils.deleteCookie(request, response, "RefreshToken"); // 해커의 쿠키도 삭제
                throw new BadCredentialsException("리프래쉬 토큰이 탈취된 것으로 간주되었습니다. 강제 로그아웃 처리 되었습니다.");
            }

            // 엑세스 토큰 재발급
            accessToken = jwtUtils.createAccessToken(userId);

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

            Authentication authentication = getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }else {
            // 유효한 토큰일 경우 => Authentication 객체 생성하여 SecurityContext에 저장
            Authentication authentication = getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            return accessToken.substring(7);
        }
        return null;
    }

    private Authentication getAuthentication(String accessToken) {
        Long id = jwtUtils.extractIdFromAccessToken(accessToken);

        return new UsernamePasswordAuthenticationToken(CustomUserPrincipal.builder().id(id).build(), null, Collections.emptyList());
    }
}
