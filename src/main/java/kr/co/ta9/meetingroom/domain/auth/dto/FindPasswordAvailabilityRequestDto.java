package kr.co.ta9.meetingroom.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindPasswordAvailabilityRequestDto {
    @NotBlank
    private String loginId;

    @NotBlank
    @Email
    @Pattern(regexp = "^\\S+$", message = "이메일에 공백은 허용되지 않습니다.")
    private String email;

    @Builder
    private FindPasswordAvailabilityRequestDto(String loginId, String email) {
        this.loginId = loginId;
        this.email = email;
    }
}
