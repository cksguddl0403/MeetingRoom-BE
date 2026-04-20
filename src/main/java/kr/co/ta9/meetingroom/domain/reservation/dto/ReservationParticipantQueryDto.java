package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationParticipantQueryDto {
    private Long reservationId;
    private Long id;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
}
