package kr.co.ta9.meetingroom.domain.dashboard.mapper;

import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionQueryDto;
import kr.co.ta9.meetingroom.domain.inspection.mapper.InspectionMapper;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.mapper.ReservationMapper;
import kr.co.ta9.meetingroom.domain.room.dto.RoomQueryDto;
import kr.co.ta9.meetingroom.domain.room.mapper.RoomMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {RoomMapper.class, ReservationMapper.class, InspectionMapper.class}
)
public interface DashboardMapper {

    @Mapping(target = "rooms", source = "roomQueryDtos")
    @Mapping(target = "reservations", source = "reservationQueryDtos")
    @Mapping(target = "inspections", source = "inspectionQueryDtos")
    DashboardDto toDto(
            List<RoomQueryDto> roomQueryDtos,
            List<ReservationQueryDto> reservationQueryDtos,
            List<InspectionQueryDto> inspectionQueryDtos
    );
}
