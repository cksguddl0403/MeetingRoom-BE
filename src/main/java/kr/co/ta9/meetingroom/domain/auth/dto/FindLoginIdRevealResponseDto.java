package kr.co.ta9.meetingroom.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FindLoginIdRevealResponseDto {
    private String loginId;

    @Builder
    private FindLoginIdRevealResponseDto(String loginId) {
        this.loginId = loginId;
    }
}
