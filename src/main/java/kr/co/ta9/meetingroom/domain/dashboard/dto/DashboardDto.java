package kr.co.ta9.meetingroom.domain.dashboard.dto;


import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class DashboardDto {
    private List<DashboardRoomDto> rooms;

    @Builder
    private DashboardDto(List<DashboardRoomDto> rooms) {
        this.rooms = rooms;
    }
}
