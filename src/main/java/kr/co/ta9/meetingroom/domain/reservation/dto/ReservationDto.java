package kr.co.ta9.meetingroom.domain.reservation.dto;

import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReservationDto {
    private Long id;
    private String title;
    private ReservationRoomDto room;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;
    private List<ReservationParticipantDto> participants;

    @Builder
    private ReservationDto(Long id, String title, ReservationRoomDto room, LocalDateTime startAt, LocalDateTime endAt, ReservationStatus status, List<ReservationParticipantDto> participants) {
        this.id = id;
        this.title = title;
        this.room = room;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.participants = participants;
    }
}
