package kr.co.ta9.meetingroom.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DashboardInspectionDto {
    private Long id;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;

    @Builder
    private DashboardInspectionDto(
            Long id,
            String name,
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdAt = createdAt;
    }
}
