package kr.co.ta9.meetingroom.domain.company.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 50)
    private String industry;

    @Column(nullable = false)
    private LocalDate foundedDate;

    @Column(length = 200)
    private String introduction;

    @Column(nullable = false)
    private int employeeCount;

    @Column(nullable = false, unique = true, length = 12)
    private String businessRegistrationNumber;

    @Builder(access = AccessLevel.PRIVATE)
    private Company(
            String name,
            String industry,
            LocalDate foundedDate,
            String introduction,
            int employeeCount,
            String businessRegistrationNumber
    ) {
        this.name = name;
        this.industry = industry;
        this.foundedDate = foundedDate;
        this.introduction = introduction;
        this.employeeCount = employeeCount;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public static Company createCompany(
            String name,
            String industry,
            LocalDate foundedDate,
            String introduction,
            int employeeCount,
            String businessRegistrationNumber
    ) {
        return Company.builder()
                .name(name)
                .industry(industry)
                .foundedDate(foundedDate)
                .introduction(introduction)
                .employeeCount(employeeCount)
                .businessRegistrationNumber(businessRegistrationNumber)
                .build();
    }
}
