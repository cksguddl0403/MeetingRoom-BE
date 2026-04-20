package kr.co.ta9.meetingroom.domain.reservation.repository;

import kr.co.ta9.meetingroom.domain.reservation.entity.ReservationParticipant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationParticipantRepository extends JpaRepository<ReservationParticipant, Long>, ReservationParticipantRepositoryCustom {

    /*
     * 예약의 참가자 ID 목록을 조회합니다.
     *
     * SELECT rp.id
     * FROM reservation_participant rp
     * WHERE rp.reservation_id = ?
     */
    @Query("""
            SELECT rp.id
            FROM ReservationParticipant rp
            WHERE rp.reservation.id = :reservationId
            """)
    List<Long> findIdsByReservationId(@Param("reservationId") Long reservationId);

    /*
     * 참가자 ID 목록을 일괄 삭제합니다.
     *
     * DELETE FROM reservation_participant rp
     * WHERE rp.id IN (...)
     */
    @Modifying
    @Query("""
            DELETE FROM ReservationParticipant rp
            WHERE rp.id IN :ids
            """)
    void deleteByIds(@Param("ids") List<Long> ids);
}

