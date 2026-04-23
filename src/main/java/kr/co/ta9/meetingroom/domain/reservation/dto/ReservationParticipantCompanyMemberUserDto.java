package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReservationParticipantCompanyMemberUserDto {
    private Long id;
    private String nickname;
    private String profileImageUrl;

    @Builder
    private ReservationParticipantCompanyMemberUserDto(Long id, String nickname, String profileImageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
