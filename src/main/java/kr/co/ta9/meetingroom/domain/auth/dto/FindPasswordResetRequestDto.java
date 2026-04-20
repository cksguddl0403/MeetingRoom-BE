package kr.co.ta9.meetingroom.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindPasswordResetRequestDto {
    @NotBlank
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "인증 토큰 형식이 올바르지 않습니다."
    )
    private String verificationId;

    @NotBlank
    private String loginId;

    @NotBlank
    @Email
    @Pattern(regexp = "^\\S+$", message = "이메일에 공백은 허용되지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])\\S{8,64}$",
            message = "비밀번호는 영문 대/소문자, 특수문자를 포함해야 하며 공백은 허용되지 않습니다."
    )
    private String newPassword;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;

    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    public boolean isNewPasswordMatching() {
        if (newPassword == null || newPasswordConfirm == null) {
            return true;
        }
        return newPassword.equals(newPasswordConfirm);
    }

    @Builder
    private FindPasswordResetRequestDto(
            String verificationId,
            String loginId,
            String email,
            String newPassword,
            String newPasswordConfirm
    ) {
        this.verificationId = verificationId;
        this.loginId = loginId;
        this.email = email;
        this.newPassword = newPassword;
        this.newPasswordConfirm = newPasswordConfirm;
    }
}
