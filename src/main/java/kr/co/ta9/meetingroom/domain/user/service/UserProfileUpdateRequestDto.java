package kr.co.ta9.meetingroom.domain.user.service;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequestDto {
    private String nickname;

    @Builder
    private UserProfileUpdateRequestDto(String nickname) {
        this.nickname = nickname;
    }
}
