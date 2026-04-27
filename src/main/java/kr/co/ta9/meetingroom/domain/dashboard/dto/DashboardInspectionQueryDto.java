package kr.co.ta9.meetingroom.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DashboardInspectionQueryDto {
    private Long id;
    private Long roomId;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
}
