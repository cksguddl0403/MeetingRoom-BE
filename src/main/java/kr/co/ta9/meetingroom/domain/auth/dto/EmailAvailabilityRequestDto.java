package kr.co.ta9.meetingroom.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailAvailabilityRequestDto {
    @NotBlank(message = "이메일은 필수입니다.")
    @Size(max = 255, message = "이메일은 최대 255자까지 입력 가능합니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Pattern(regexp = "^\\S+$", message = "이메일에 공백은 허용되지 않습니다.")
    private String email;

    @Builder
    private EmailAvailabilityRequestDto(String email) {
        this.email = email;
    }
}
