package kr.co.ta9.meetingroom.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EmailVerificationVerifyDto {
    private String verificationId;

    @Builder
    private EmailVerificationVerifyDto(String verificationId) {
        this.verificationId = verificationId;
    }
}
