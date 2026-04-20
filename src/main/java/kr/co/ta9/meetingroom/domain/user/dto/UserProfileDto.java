package kr.co.ta9.meetingroom.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserProfileDto {
    private String nickname;
    private String profileImageUrl;

    @Builder
    private UserProfileDto(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
