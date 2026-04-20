package kr.co.ta9.meetingroom.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindLoginIdRevealRequestDto {
    @NotBlank
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "인증 토큰 형식이 올바르지 않습니다."
    )
    private String verificationId;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    @Pattern(regexp = "^\\S+$", message = "이메일에 공백은 허용되지 않습니다.")
    private String email;

    @Builder
    private FindLoginIdRevealRequestDto(String verificationId, String name, String email) {
        this.verificationId = verificationId;
        this.name = name;
        this.email = email;
    }
}
