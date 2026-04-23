package kr.co.ta9.meetingroom.domain.reservation.mapper;

import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationListDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationRoomDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationApplicantDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantCompanyMemberDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantCompanyMemberUserDto;
import kr.co.ta9.meetingroom.domain.reservation.entity.Reservation;
import kr.co.ta9.meetingroom.domain.reservation.entity.ReservationParticipant;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    default ReservationDto toDto(Reservation reservation, List<ReservationParticipant> participants) {
        return ReservationDto.builder()
                .id(reservation.getId())
                .title(reservation.getTitle())
                .room(ReservationRoomDto.builder()
                        .id(reservation.getRoom().getId())
                        .name(reservation.getRoom().getName())
                        .build())
                .startAt(reservation.getStartAt())
                .endAt(reservation.getEndAt())
                .status(reservation.getStatus())
                .participants(
                        participants == null || participants.isEmpty()
                                ? Collections.emptyList()
                                : participants.stream()
                                .map(participant -> ReservationParticipantDto.builder()
                                        .id(participant.getId())
                                        .companyMember(ReservationParticipantCompanyMemberDto.builder()
                                                .id(participant.getCompanyMember().getId())
                                                .user(ReservationParticipantCompanyMemberUserDto.builder()
                                                        .id(participant.getCompanyMember().getUser().getId())
                                                        .nickname(participant.getCompanyMember().getUser().getNickname())
                                                        .profileImageUrl(null)
                                                        .build())
                                                .build())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    default ReservationDto reservationQueryDtoToReservationDto(ReservationQueryDto reservationQueryDto) {
        if (reservationQueryDto == null) {
            return null;
        }
        return toDto(reservationQueryDto, Collections.emptyList());
    }

    default ReservationDto toDto(
            ReservationQueryDto reservationQueryDto,
            List<ReservationParticipantQueryDto> participantQueryDtos
    ) {
        return ReservationDto.builder()
                .id(reservationQueryDto.getId())
                .title(reservationQueryDto.getTitle())
                .room(ReservationRoomDto.builder()
                        .id(reservationQueryDto.getRoom().getId())
                        .name(reservationQueryDto.getRoom().getName())
                        .build())
                .startAt(reservationQueryDto.getStartAt())
                .endAt(reservationQueryDto.getEndAt())
                .status(reservationQueryDto.getStatus())
                .participants(
                        participantQueryDtos == null || participantQueryDtos.isEmpty()
                                ? Collections.emptyList()
                                : participantQueryDtos.stream()
                                .map(participant -> ReservationParticipantDto.builder()
                                        .id(participant.getId())
                                        .companyMember(ReservationParticipantCompanyMemberDto.builder()
                                                .id(participant.getCompanyMember().getId())
                                                .user(ReservationParticipantCompanyMemberUserDto.builder()
                                                        .id(participant.getCompanyMember().getUser().getId())
                                                        .nickname(participant.getCompanyMember().getUser().getNickname())
                                                        .profileImageUrl(participant.getCompanyMember().getUser().getProfileImageUrl())
                                                        .build())
                                                .build())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    default ReservationListDto toListDto(
            ReservationQueryDto reservationQueryDto,
            List<ReservationParticipantQueryDto> participantQueryDtos
    ) {
        return ReservationListDto.builder()
                .id(reservationQueryDto.getId())
                .title(reservationQueryDto.getTitle())
                .room(ReservationRoomDto.builder()
                        .id(reservationQueryDto.getRoom().getId())
                        .name(reservationQueryDto.getRoom().getName())
                        .build())
                .startAt(reservationQueryDto.getStartAt())
                .endAt(reservationQueryDto.getEndAt())
                .status(reservationQueryDto.getStatus())
                .applicant(
                        reservationQueryDto.getApplicant() == null
                                ? null
                                : ReservationApplicantDto.builder()
                                .id(reservationQueryDto.getApplicant().getId())
                                .nickname(reservationQueryDto.getApplicant().getNickname())
                                .build()
                )
                .participants(
                        participantQueryDtos == null || participantQueryDtos.isEmpty()
                                ? Collections.emptyList()
                                : participantQueryDtos.stream()
                                .map(participant -> ReservationParticipantDto.builder()
                                        .id(participant.getId())
                                        .companyMember(ReservationParticipantCompanyMemberDto.builder()
                                                .id(participant.getCompanyMember().getId())
                                                .user(ReservationParticipantCompanyMemberUserDto.builder()
                                                        .id(participant.getCompanyMember().getUser().getId())
                                                        .nickname(participant.getCompanyMember().getUser().getNickname())
                                                        .profileImageUrl(participant.getCompanyMember().getUser().getProfileImageUrl())
                                                        .build())
                                                .build())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
