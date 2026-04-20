package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationApplicantQueryDto {
    private Long id;
    private String nickname;
}
