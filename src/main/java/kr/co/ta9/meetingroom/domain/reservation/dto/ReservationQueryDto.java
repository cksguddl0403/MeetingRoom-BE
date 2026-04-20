package kr.co.ta9.meetingroom.domain.reservation.dto;

import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationQueryDto {
    private Long id;
    private String title;
    private ReservationRoomQueryDto room;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;
    private ReservationApplicantQueryDto applicant;
}
