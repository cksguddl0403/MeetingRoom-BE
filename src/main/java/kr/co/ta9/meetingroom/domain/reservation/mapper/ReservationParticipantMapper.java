package kr.co.ta9.meetingroom.domain.reservation.mapper;

import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantCompanyMemberDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantCompanyMemberUserDto;
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
                .companyMember(ReservationParticipantCompanyMemberDto.builder()
                        .id(reservationParticipantQueryDto.getCompanyMember().getId())
                        .user(ReservationParticipantCompanyMemberUserDto.builder()
                                .id(reservationParticipantQueryDto.getCompanyMember().getUser().getId())
                                .nickname(reservationParticipantQueryDto.getCompanyMember().getUser().getNickname())
                                .profileImageUrl(reservationParticipantQueryDto.getCompanyMember().getUser().getProfileImageUrl())
                                .build())
                        .build())
                .build();
    }

    default ReservationParticipantDto toDto(ReservationParticipant participant) {
        if (participant == null) {
            return null;
        }
        return ReservationParticipantDto.builder()
                .id(participant.getId())
                .companyMember(ReservationParticipantCompanyMemberDto.builder()
                        .id(participant.getCompanyMember().getId())
                        .user(ReservationParticipantCompanyMemberUserDto.builder()
                                .id(participant.getCompanyMember().getUser().getId())
                                .nickname(participant.getCompanyMember().getUser().getNickname())
                                .profileImageUrl(null)
                                .build())
                        .build())
                .build();
    }
}
