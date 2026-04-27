package kr.co.ta9.meetingroom.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardQueryDto {
    private DashboardRoomQueryDto room;
    private DashboardReservationQueryDto reservation;
    private DashboardInspectionQueryDto inspection;
}
