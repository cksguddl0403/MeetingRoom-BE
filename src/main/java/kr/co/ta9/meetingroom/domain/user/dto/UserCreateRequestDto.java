package kr.co.ta9.meetingroom.domain.user.dto;

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
public class UserCreateRequestDto {
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
    @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "아이디는 영어 소문자와 숫자만 입력 가능합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])\\S{8,64}$",
            message = "비밀번호는 영문 대/소문자, 특수문자를 포함해야 하며 공백은 허용되지 않습니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
    @Pattern(regexp = "^[A-Za-z가-힣]{2,20}$", message = "이름은 한글 또는 영문 대/소문자만 입력 가능합니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 16, message = "닉네임은 2~16자여야 합니다.")
    @Pattern(regexp = "^[A-Za-z가-힣0-9]{2,16}$", message = "닉네임은 한글, 영문, 숫자만 입력 가능합니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수입니다.")
    @Size(max = 255, message = "이메일은 최대 255자까지 입력 가능합니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Pattern(regexp = "^\\S+$", message = "이메일에 공백은 허용되지 않습니다.")
    private String email;

    @NotBlank(message = "이메일 인증 토큰은 필수입니다.")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "이메일 인증 토큰 형식이 올바르지 않습니다."
    )
    private String verificationId;

    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordMatching() {
        if (password == null || passwordConfirm == null) {
            return true;
        }
        return password.equals(passwordConfirm);
    }

    @Builder
    private UserCreateRequestDto(
            String loginId,
            String password,
            String passwordConfirm,
            String name,
            String nickname,
            String email,
            String verificationId
    ) {
        this.loginId = loginId;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.verificationId = verificationId;
    }
}
