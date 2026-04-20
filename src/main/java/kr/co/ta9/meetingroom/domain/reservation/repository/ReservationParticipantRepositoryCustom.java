package kr.co.ta9.meetingroom.domain.reservation.repository;

import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantQueryDto;

import java.util.Collection;
import java.util.List;

/*
 * 예약 참가자 목록 조회 전용 커스텀 리포지토리입니다.
 */
public interface ReservationParticipantRepositoryCustom {

    /*
     * 예약 ID 집합에 연결된 참가자 목록을 조회합니다.
     */
    List<ReservationParticipantQueryDto> getReservationParticipantsByReservationIds(
            Collection<Long> reservationIds
    );
}
