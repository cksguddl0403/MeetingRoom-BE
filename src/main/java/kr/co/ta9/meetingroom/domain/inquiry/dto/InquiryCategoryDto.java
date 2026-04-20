package kr.co.ta9.meetingroom.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InquiryCategoryDto {
    private Long id;
    private String name;

    @Builder
    private InquiryCategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
