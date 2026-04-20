package kr.co.ta9.meetingroom.domain.notice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NoticeCategoryDto {
    private Long id;
    private String name;

    @Builder
    private NoticeCategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
