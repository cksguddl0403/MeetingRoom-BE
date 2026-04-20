package kr.co.ta9.meetingroom.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JwtTokenDto {
    private String accessToken;

    @Builder
    private JwtTokenDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
