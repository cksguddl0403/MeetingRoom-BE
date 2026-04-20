package kr.co.ta9.meetingroom.domain.company.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CompanyListDto {
    private Long id;
    private String name;
    private String logo;

    @Builder
    private CompanyListDto(Long id, String name, String logo) {
        this.id = id;
        this.name = name;
        this.logo = logo;
    }
}
