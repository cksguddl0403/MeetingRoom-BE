package kr.co.ta9.meetingroom.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class DashboardRoomDto {
    private Long id;
    private String name;
    private int maxCapacity;
    private Long companyId;
    private String status;
    private List<DashboardReservationDto> reservations;
    private List<DashboardInspectionDto> inspections;

    @Builder
    private DashboardRoomDto(
            Long id,
            String name,
            int maxCapacity,
            Long companyId,
            String status,
            List<DashboardReservationDto> reservations,
            List<DashboardInspectionDto> inspections
    ) {
        this.id = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.companyId = companyId;
        this.status = status;
        this.reservations = reservations;
        this.inspections = inspections;
    }
}
