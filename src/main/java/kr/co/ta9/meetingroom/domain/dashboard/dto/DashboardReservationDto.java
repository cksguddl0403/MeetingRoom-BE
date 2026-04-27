package kr.co.ta9.meetingroom.domain.dashboard.dto;

import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DashboardReservationDto {
    private Long id;
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;

    @Builder
    private DashboardReservationDto(
            Long id,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            ReservationStatus status
    ) {
        this.id = id;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
    }
}
