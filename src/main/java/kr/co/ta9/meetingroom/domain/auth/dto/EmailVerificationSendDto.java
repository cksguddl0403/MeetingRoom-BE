package kr.co.ta9.meetingroom.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EmailVerificationSendDto {
    private long expiresInSeconds;

    @Builder
    private EmailVerificationSendDto(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
