package kr.co.ta9.meetingroom.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kr.co.ta9.meetingroom.domain.auth.enums.EmailVerificationPurpose;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerificationSendRequestDto {
    private EmailVerificationPurpose purpose;

    @NotBlank
    @Email
    private String email;

    private String name;

    private String loginId;

    @Builder
    private EmailVerificationSendRequestDto(EmailVerificationPurpose purpose, String email, String name, String loginId) {
        this.purpose = purpose;
        this.email = email;
        this.name = name;
        this.loginId = loginId;
    }
}

