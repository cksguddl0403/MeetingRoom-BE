package kr.co.ta9.meetingroom.domain.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequestDto {
    private String currentPassword;

    private String newPassword;

    @AssertTrue(message = "비밀번호는 8~64자여야 합니다.")
    public boolean isNewPasswordLengthValid() {
        if (newPassword == null || newPassword.isBlank()) {
            return true;
        }
        int len = newPassword.length();
        return len >= 8 && len <= 64;
    }

    @AssertTrue(message = "비밀번호는 영문 대/소문자, 특수문자를 포함해야 하며 공백은 허용되지 않습니다.")
    public boolean isNewPasswordPatternValid() {
        if (newPassword == null || newPassword.isBlank()) {
            return true;
        }
        int len = newPassword.length();
        if (len < 8 || len > 64) {
            return true;
        }
        return newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])\\S{8,64}$");
    }

    @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
    @Pattern(regexp = "^[A-Za-z가-힣]{2,20}$", message = "이름은 한글 또는 영문 대/소문자만 입력 가능합니다.")
    private String name;

    @Builder
    private UserUpdateRequestDto(String newPassword, String currentPassword, String name) {
        this.newPassword = newPassword;
        this.currentPassword = currentPassword;
        this.name = name;
    }
}
