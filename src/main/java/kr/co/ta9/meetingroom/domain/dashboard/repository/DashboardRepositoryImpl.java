package kr.co.ta9.meetingroom.domain.dashboard.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardInspectionQueryDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardQueryDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardReservationQueryDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardRoomQueryDto;
import kr.co.ta9.meetingroom.domain.inspection.entity.QInspection;
import kr.co.ta9.meetingroom.domain.reservation.entity.QReservation;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import kr.co.ta9.meetingroom.domain.room.entity.QRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepository {
    private final JPAQueryFactory queryFactory;

    private final QRoom room = QRoom.room;
    private final QReservation reservation = QReservation.reservation;
    private final QInspection inspection = QInspection.inspection;

    /*
     * 회사의 전체 회의실을 기준으로 대시보드 행 데이터를 조회합니다.
     *
     * SELECT
     *   r.id,
     *   r.name,
     *   r.max_capacity,
     *   r.company_id,
     *   CASE
     *     WHEN EXISTS (
     *       SELECT 1
     *       FROM inspection i_status
     *       WHERE i_status.room_id = r.id
     *         AND i_status.start_at <= :at
     *         AND i_status.end_at >= :at
     *     ) THEN '점검 중'
     *     WHEN EXISTS (
     *       SELECT 1
     *       FROM reservation rv_status
     *       WHERE rv_status.room_id = r.id
     *         AND rv_status.status = 'CONFIRMED'
     *         AND rv_status.start_at <= :at
     *         AND rv_status.end_at >= :at
     *     ) THEN '사용 중'
     *     ELSE '사용가능'
     *   END AS room_status,
     *   rv.id,
     *   r.id,
     *   rv.title,
     *   rv.start_at,
     *   rv.end_at,
     *   rv.status,
     *   i.id,
     *   r.id,
     *   i.name,
     *   i.start_at,
     *   i.end_at,
     *   i.created_at
     * FROM room r
     * LEFT JOIN reservation rv ON rv.room_id = r.id
     * LEFT JOIN inspection i ON i.room_id = r.id
     * WHERE r.company_id = :companyId
     *   AND r.is_deleted = FALSE
     * ORDER BY
     *   r.id ASC,
     *   rv.start_at ASC,
     *   i.created_at DESC
     */
    @Override
    public List<DashboardQueryDto> getDashboard(Long companyId, LocalDateTime at) {
        return queryFactory
                .select(Projections.constructor(
                        DashboardQueryDto.class,
                        Projections.constructor(
                                DashboardRoomQueryDto.class,
                                room.id,
                                room.name,
                                room.maxCapacity,
                                room.company.id,
                                resolveRoomStatusExpression(at)
                        ),
                        Projections.constructor(
                                DashboardReservationQueryDto.class,
                                reservation.id,
                                room.id,
                                reservation.title,
                                reservation.startAt,
                                reservation.endAt,
                                reservation.status
                        ),
                        Projections.constructor(
                                DashboardInspectionQueryDto.class,
                                inspection.id,
                                room.id,
                                inspection.name,
                                inspection.startAt,
                                inspection.endAt,
                                inspection.createdAt
                        )
                ))
                .from(room)
                .leftJoin(reservation).on(reservation.room.id.eq(room.id))
                .leftJoin(inspection).on(inspection.room.id.eq(room.id))
                .where(
                        room.company.id.eq(companyId),
                        room.deleted.isFalse()
                )
                .orderBy(
                        room.id.asc(),
                        reservation.startAt.asc(),
                        inspection.createdAt.desc()
                )
                .fetch();
    }

    /*
     * 특정 시점 기준 회의실 상태 CASE 식을 생성합니다.
     *
     * CASE
     *   WHEN EXISTS (
     *     SELECT 1
     *     FROM inspection i
     *     WHERE i.room_id = r.id
     *       AND i.start_at <= :at
     *       AND i.end_at >= :at
     *   ) THEN '점검 중'
     *   WHEN EXISTS (
     *     SELECT 1
     *     FROM reservation rv
     *     WHERE rv.room_id = r.id
     *       AND rv.status = 'CONFIRMED'
     *       AND rv.start_at <= :at
     *       AND rv.end_at >= :at
     *   ) THEN '사용 중'
     *   ELSE '사용가능'
     * END
     */
    private Expression<String> resolveRoomStatusExpression(LocalDateTime at) {
        BooleanExpression underInspection = JPAExpressions.selectOne()
                .from(inspection)
                .where(
                        inspection.room.id.eq(room.id),
                        inspection.startAt.loe(at),
                        inspection.endAt.goe(at)
                )
                .exists();

        BooleanExpression inUse = JPAExpressions.selectOne()
                .from(reservation)
                .where(
                        reservation.room.id.eq(room.id),
                        reservation.status.eq(ReservationStatus.CONFIRMED),
                        reservation.startAt.loe(at),
                        reservation.endAt.goe(at)
                )
                .exists();

        return new CaseBuilder()
                .when(underInspection)
                .then("점검 중")
                .when(inUse)
                .then("사용 중")
                .otherwise("사용가능");
    }
}
