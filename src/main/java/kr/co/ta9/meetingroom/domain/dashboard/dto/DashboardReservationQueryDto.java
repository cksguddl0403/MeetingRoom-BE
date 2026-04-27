package kr.co.ta9.meetingroom.domain.dashboard.dto;

import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DashboardReservationQueryDto {
    private Long id;
    private Long roomId;
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;
}
