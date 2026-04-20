package kr.co.ta9.meetingroom.domain.dashboard.dto;


import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationDto;
import kr.co.ta9.meetingroom.domain.room.dto.RoomDto;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class DashboardDto {
    private List<RoomDto> rooms;
    private List<ReservationDto> reservations;
    private List<InspectionDto> inspections;

    @Builder
    private DashboardDto(List<RoomDto> rooms, List<ReservationDto> reservations, List<InspectionDto> inspections) {
        this.rooms = rooms;
        this.reservations = reservations;
        this.inspections = inspections;
    }
}
