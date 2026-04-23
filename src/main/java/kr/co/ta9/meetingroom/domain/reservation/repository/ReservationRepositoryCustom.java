package kr.co.ta9.meetingroom.domain.reservation.repository;

import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReservationRepositoryCustom {
    Optional<ReservationQueryDto> getReservationById(Long currentUserId, Long reservationId);

    Page<ReservationQueryDto> getReservations(
            Long currentUserId,
            Long companyId,
            ReservationListSearchRequestDto reservationListSearchRequestDto,
            Pageable pageable
    );

    List<ReservationQueryDto> getAllReservations(Long currentUserId, Long companyId, List<Long> roomIds);
}
