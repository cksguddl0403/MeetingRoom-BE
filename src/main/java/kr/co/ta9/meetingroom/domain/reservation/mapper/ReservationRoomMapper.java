package kr.co.ta9.meetingroom.domain.reservation.mapper;

import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationRoomDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationRoomQueryDto;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationRoomMapper {

    default ReservationRoomDto toReservationRoomDto(ReservationRoomQueryDto reservationRoomQueryDto) {
        if (reservationRoomQueryDto == null) {
            return null;
        }
        return ReservationRoomDto.builder()
                .id(reservationRoomQueryDto.getId())
                .name(reservationRoomQueryDto.getName())
                .build();
    }

    default ReservationRoomDto toReservationRoomDto(Room room) {
        if (room == null) {
            return null;
        }
        return ReservationRoomDto.builder()
                .id(room.getId())
                .name(room.getName())
                .build();
    }
}
