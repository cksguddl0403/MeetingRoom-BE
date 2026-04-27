package kr.co.ta9.meetingroom.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserProfileUpdateResponseDto {
    private String nickname;
    private String profileImageUrl;
    private boolean changed;

    @Builder
    private UserProfileUpdateResponseDto(String nickname, String profileImageUrl, boolean changed) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.changed = changed;
    }
}
