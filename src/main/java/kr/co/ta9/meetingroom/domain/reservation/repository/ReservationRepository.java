package kr.co.ta9.meetingroom.domain.reservation.repository;

import kr.co.ta9.meetingroom.domain.reservation.entity.Reservation;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
    /*
     * 신규 예약 생성 시 회의실 시간 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM reservation r
     * WHERE r.room_id = ?
     *   AND r.status = ?
     *   AND r.start_at < ?
     *   AND r.end_at > ?
     */
    boolean existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long roomId,
            ReservationStatus status,
            LocalDateTime endAt,
            LocalDateTime startAt
    );

    /*
     * 예약 수정 시 자기 자신을 제외한 회의실 시간 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM reservation r
     * WHERE r.room_id = ?
     *   AND r.status = ?
     *   AND r.start_at < ?
     *   AND r.end_at > ?
     *   AND r.id <> ?
     */
    boolean existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            Long roomId,
            ReservationStatus status,
            LocalDateTime endAt,
            LocalDateTime startAt,
            Long id
    );

    /*
     * 예약 시간이 겹치는 회사 멤버 ID 목록을 조회합니다.
     *
     * SELECT DISTINCT cm.id
     * FROM reservation_participant rp
     * JOIN reservation r ON rp.reservation_id = r.id
     * JOIN company_member cm ON rp.company_member_id = cm.id
     * WHERE r.status = 'CONFIRMED'
     *   AND r.start_at < ?
     *   AND r.end_at > ?
     *   AND cm.id IN (...companyMemberIds)
     *   AND (? IS NULL OR r.id <> ?)
     */
    @Query("""
            SELECT DISTINCT cm.id
            FROM ReservationParticipant p
            JOIN p.reservation r
            JOIN p.companyMember cm
            WHERE r.status = kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus.CONFIRMED
              AND r.startAt < :endAt
              AND r.endAt > :startAt
              AND cm.id IN :companyMemberIds
              AND (:excludeReservationId IS NULL OR r.id <> :excludeReservationId)
            """)
    Set<Long> findCompanyMemberIdsWithOverlappingReservationAsParticipant(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("companyMemberIds") List<Long> companyMemberIds,
            @Param("excludeReservationId") Long excludeReservationId
    );
}
