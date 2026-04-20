package kr.co.ta9.meetingroom.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReservationParticipantDto {
    private Long id;
    private Long userId;
    private String nickname;
    private String profileImageUrl;

    @Builder
    private ReservationParticipantDto(Long id, Long userId, String nickname, String profileImageUrl) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
