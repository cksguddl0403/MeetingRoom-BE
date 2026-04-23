package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationParticipantUserCompanyMemberQueryDto {
    private Long id;
    private ReservationParticipantUserCompanyMemberUserQueryDto user;
}
