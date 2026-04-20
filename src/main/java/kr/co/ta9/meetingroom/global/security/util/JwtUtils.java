package kr.co.ta9.meetingroom.global.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpiration;

    private SecretKey accessSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(accessSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey refreshSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(refreshSecret);
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

    /*
     * 엑세스 토큰 생성
     */
    public String createAccessToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(accessSigningKey())
                .compact();
    }

    /*
     * 엑세스 토큰에서 사용자 ID 추출
     */
    public Long extractIdFromAccessToken(String accessToken) {
        String idString = Jwts.parser()
                .verifyWith(accessSigningKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();
        return Long.parseLong(idString);
    }
    /*
     * 엑세스 토근 유효성 검사
     */
    public boolean isAccessTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * 리프래쉬 토큰 생성
     */
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(refreshSigningKey())
                .compact();
    }

    /*
     * 리프레시 토큰에서 사용자 ID 추출
     */
    public Long extractIdFromRefreshToken(String refreshToken) {
        String idString = Jwts.parser()
                .verifyWith(refreshSigningKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getSubject();

        return Long.parseLong(idString);
    }


    /*
     * 리프래쉬 토큰 유효성 검사
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(refreshSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /*
     * 리프레시 토큰 남은 유효기간 조회
     */
    public long getRefreshTokenRemainingExpiration(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(refreshSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }
}