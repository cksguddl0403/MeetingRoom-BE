package kr.co.ta9.meetingroom.domain.reservation.mapper;

import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationApplicantDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationApplicantQueryDto;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationApplicantMapper {

    default ReservationApplicantDto toReservationApplicantDto(User user) {
        if (user == null) {
            return null;
        }
        return ReservationApplicantDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .build();
    }

    default ReservationApplicantDto toReservationApplicantDto(ReservationApplicantQueryDto reservationApplicantQueryDto) {
        if (reservationApplicantQueryDto == null) {
            return null;
        }
        return ReservationApplicantDto.builder()
                .id(reservationApplicantQueryDto.getId())
                .nickname(reservationApplicantQueryDto.getNickname())
                .build();
    }
}
