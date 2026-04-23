package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReservationParticipantDto {
    private Long id;
    private ReservationParticipantCompanyMemberDto companyMember;

    @Builder
    private ReservationParticipantDto(Long id, ReservationParticipantCompanyMemberDto companyMember) {
        this.id = id;
        this.companyMember = companyMember;
    }
}
