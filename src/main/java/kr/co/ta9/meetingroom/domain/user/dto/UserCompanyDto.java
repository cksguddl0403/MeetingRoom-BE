package kr.co.ta9.meetingroom.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserCompanyDto {
    private Long id;
    private String name;

    @Builder
    private UserCompanyDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
