package kr.co.ta9.meetingroom.domain.company.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CompanyDto {

    private Long id;
    private String name;
    private String logo;
    private String industry;
    private LocalDate foundedDate;
    private String introduction;
    private Integer employeeCount;
    private String businessRegistrationNumber;

    @Builder
    private CompanyDto(
            Long id,
            String name,
            String logo,
            String industry,
            LocalDate foundedDate,
            String introduction,
            Integer employeeCount,
            String businessRegistrationNumber
    ) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.industry = industry;
        this.foundedDate = foundedDate;
        this.introduction = introduction;
        this.employeeCount = employeeCount;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }
}
