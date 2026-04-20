package kr.co.ta9.meetingroom.domain.reservation.mapper;

import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.entity.ReservationParticipant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationParticipantMapper {

    default ReservationParticipantDto toDto(ReservationParticipantQueryDto reservationParticipantQueryDto) {
        if (reservationParticipantQueryDto == null) {
            return null;
        }
        return ReservationParticipantDto.builder()
                .id(reservationParticipantQueryDto.getId())
                .userId(reservationParticipantQueryDto.getUserId())
                .nickname(reservationParticipantQueryDto.getNickname())
                .profileImageUrl(reservationParticipantQueryDto.getProfileImageUrl())
                .build();
    }

    default ReservationParticipantDto toDto(ReservationParticipant participant) {
        if (participant == null) {
            return null;
        }
        return ReservationParticipantDto.builder()
                .id(participant.getId())
                .userId(participant.getCompanyMember().getUser().getId())
                .nickname(participant.getCompanyMember().getUser().getNickname())
                .profileImageUrl(null)
                .build();
    }
}
