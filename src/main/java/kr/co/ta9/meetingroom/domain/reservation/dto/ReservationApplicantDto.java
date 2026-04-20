package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReservationApplicantDto {
    private Long id;
    private String nickname;

    @Builder
    private ReservationApplicantDto(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}
