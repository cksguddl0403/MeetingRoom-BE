package kr.co.ta9.meetingroom.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardRoomQueryDto {
    private Long id;
    private String name;
    private Integer maxCapacity;
    private Long companyId;
    private String status;
}
