package kr.co.ta9.meetingroom.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameAvailabilityRequestDto {
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 16, message = "닉네임은 2~16자여야 합니다.")
    @Pattern(regexp = "^[A-Za-z가-힣0-9]{2,16}$", message = "닉네임은 한글, 영문, 숫자만 입력 가능합니다.")
    private String nickname;

    @Builder
    private NicknameAvailabilityRequestDto(String nickname) {
        this.nickname = nickname;
    }
}
