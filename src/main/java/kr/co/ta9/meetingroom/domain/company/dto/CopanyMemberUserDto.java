package kr.co.ta9.meetingroom.domain.company.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CopanyMemberUserDto {

    private Long id;
    private String nickname;

    @Builder
    private CopanyMemberUserDto(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}
