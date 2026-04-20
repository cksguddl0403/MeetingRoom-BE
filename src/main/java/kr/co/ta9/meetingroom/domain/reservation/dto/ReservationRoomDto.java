package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReservationRoomDto {
    private Long id;
    private String name;

    @Builder
    private ReservationRoomDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
