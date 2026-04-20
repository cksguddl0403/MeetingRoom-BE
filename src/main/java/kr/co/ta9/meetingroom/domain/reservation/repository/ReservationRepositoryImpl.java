package kr.co.ta9.meetingroom.domain.reservation.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationApplicantQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationRoomQueryDto;
import kr.co.ta9.meetingroom.domain.company.entity.QCompanyMember;
import kr.co.ta9.meetingroom.domain.reservation.entity.QReservation;
import kr.co.ta9.meetingroom.domain.reservation.entity.QReservationParticipant;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationSortBy;
import kr.co.ta9.meetingroom.domain.reservation.enums.TimePeriod;
import kr.co.ta9.meetingroom.domain.room.entity.QRoom;
import kr.co.ta9.meetingroom.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReservation reservation = QReservation.reservation;
    private final QRoom room = QRoom.room;
    private final QCompanyMember applicantMember = new QCompanyMember("applicantMember");
    private final QUser user = QUser.user;
    private final QReservationParticipant reservationParticipant = QReservationParticipant.reservationParticipant;

    /*
     * 예약 목록과 전체 건수를 페이징 조회합니다.
     *
     * SELECT COUNT(r.id)
     * FROM reservation r
     * LEFT JOIN room rm ON r.room_id = rm.id
     * WHERE rm.company_id = :companyId
     *   AND (:status IS NULL OR r.status = :status)
     *   AND (:fromDate IS NULL OR r.start_at >= :fromDateStartOfDay)
     *   AND (:toDate IS NULL OR r.end_at < :toDatePlusOneStartOfDay)
     *   AND (
     *     :timePeriod IS NULL
     *     OR (:timePeriod = 'MORNING' AND EXTRACT(HOUR FROM r.start_at) < 12)
     *     OR (:timePeriod = 'AFTERNOON' AND EXTRACT(HOUR FROM r.start_at) >= 12)
     *   )
     *   AND (
     *     (:applicantOnly IS NOT TRUE AND :participatedOnly IS NOT TRUE)
     *     OR (:applicantOnly IS TRUE AND am.user_id = :currentUserId)
     *     OR (:participatedOnly IS TRUE AND EXISTS (
     *         SELECT 1
     *         FROM reservation_participant rp
     *         JOIN company_member pcm ON rp.company_member_id = pcm.id
     *         WHERE rp.reservation_id = r.id
     *           AND pcm.user_id = :currentUserId
     *     ))
     *   )
     *
     * SELECT r.id, r.title, rm.id,
     *        CASE WHEN rm.is_deleted = TRUE THEN CONCAT(rm.name, ' (삭제 됨)') ELSE rm.name END AS room_name,
     *        r.start_at, r.end_at, r.status, u.id,
     *        CASE WHEN am.status = 'RESIGNED' THEN CONCAT(u.nickname, ' (전 직원)') ELSE u.nickname END AS applicant_nickname
     * FROM reservation r
     * LEFT JOIN room rm ON r.room_id = rm.id
     * LEFT JOIN company_member am ON r.company_member_id = am.id
     * LEFT JOIN user u ON am.user_id = u.id
     * WHERE rm.company_id = :companyId
     *   AND (:status IS NULL OR r.status = :status)
     *   AND (:fromDate IS NULL OR r.start_at >= :fromDateStartOfDay)
     *   AND (:toDate IS NULL OR r.end_at < :toDatePlusOneStartOfDay)
     *   AND (
     *     :timePeriod IS NULL
     *     OR (:timePeriod = 'MORNING' AND EXTRACT(HOUR FROM r.start_at) < 12)
     *     OR (:timePeriod = 'AFTERNOON' AND EXTRACT(HOUR FROM r.start_at) >= 12)
     *   )
     *   AND (
     *     (:applicantOnly IS NOT TRUE AND :participatedOnly IS NOT TRUE)
     *     OR (:applicantOnly IS TRUE AND am.user_id = :currentUserId)
     *     OR (:participatedOnly IS TRUE AND EXISTS (
     *         SELECT 1
     *         FROM reservation_participant rp
     *         JOIN company_member pcm ON rp.company_member_id = pcm.id
     *         WHERE rp.reservation_id = r.id
     *           AND pcm.user_id = :currentUserId
     *     ))
     *   )
     * ORDER BY r.created_at ASC|DESC
     * -- 그 외(미지정/미지원): ORDER BY r.id ASC
     * LIMIT ? OFFSET ?
     */
    @Override
    public Page<ReservationQueryDto> getReservations(
            Long currentUserId,
            Long companyId,
            ReservationListSearchRequestDto reservationListSearchRequestDto,
            Pageable pageable
    ) {
        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .leftJoin(reservation.room, room)
                .where(
                        eqCompanyId(companyId),
                        eqStatus(reservationListSearchRequestDto),
                        fromDateGoe(reservationListSearchRequestDto),
                        toDateLt(reservationListSearchRequestDto),
                        timePeriodEq(reservationListSearchRequestDto),
                        userInvolvement(currentUserId, reservationListSearchRequestDto)
                )
                .fetchOne();

        long totalElements = total == null ? 0L : total;

        List<ReservationQueryDto> content = queryFactory
                .select(Projections.constructor(ReservationQueryDto.class,
                        reservation.id,
                        reservation.title,
                        Projections.constructor(ReservationRoomQueryDto.class,
                                room.id,
                                roomDisplayNameExpr()
                        ),
                        reservation.startAt,
                        reservation.endAt,
                        reservation.status,
                        Projections.constructor(ReservationApplicantQueryDto.class,
                                user.id,
                                applicantDisplayNameExpr())
                        )
                )
                .from(reservation)
                .leftJoin(reservation.room, room)
                .leftJoin(reservation.companyMember, applicantMember)
                .leftJoin(applicantMember.user, user)
                .where(
                        eqCompanyId(companyId),
                        eqStatus(reservationListSearchRequestDto),
                        fromDateGoe(reservationListSearchRequestDto),
                        toDateLt(reservationListSearchRequestDto),
                        timePeriodEq(reservationListSearchRequestDto),
                        userInvolvement(currentUserId, reservationListSearchRequestDto)
                )
                .orderBy(resolveSortOrders(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, totalElements);
    }

    /*
     * 회의실 목록 조건으로 예약 목록을 조회합니다.
     *
     * SELECT r.id, r.title, rm.id,
     *        CASE WHEN rm.is_deleted = TRUE THEN CONCAT(rm.name, ' (삭제 됨)') ELSE rm.name END AS room_name,
     *        r.start_at, r.end_at, r.status, u.id,
     *        CASE WHEN am.status = 'RESIGNED' THEN CONCAT(u.nickname, ' (전 직원)') ELSE u.nickname END AS applicant_nickname
     * FROM reservation r
     * LEFT JOIN room rm ON r.room_id = rm.id
     * LEFT JOIN company_member am ON r.company_member_id = am.id
     * LEFT JOIN user u ON am.user_id = u.id
     * WHERE rm.company_id = :companyId
     *   AND (:status IS NULL OR r.status = :status)
     *   AND (:fromDate IS NULL OR r.start_at >= :fromDateStartOfDay)
     *   AND (:toDate IS NULL OR r.end_at < :toDatePlusOneStartOfDay)
     *   AND (
     *     :timePeriod IS NULL
     *     OR (:timePeriod = 'MORNING' AND EXTRACT(HOUR FROM r.start_at) < 12)
     *     OR (:timePeriod = 'AFTERNOON' AND EXTRACT(HOUR FROM r.start_at) >= 12)
     *   )
     *   AND (
     *     (:applicantOnly IS NOT TRUE AND :participatedOnly IS NOT TRUE)
     *     OR (:applicantOnly IS TRUE AND am.user_id = :currentUserId)
     *     OR (:participatedOnly IS TRUE AND EXISTS (
     *         SELECT 1
     *         FROM reservation_participant rp
     *         JOIN company_member pcm ON rp.company_member_id = pcm.id
     *         WHERE rp.reservation_id = r.id
     *           AND pcm.user_id = :currentUserId
     *     ))
     *   )
     *   AND rm.id IN (...)
     */
    @Override
    public List<ReservationQueryDto> getReservations(Long currentUserId, Long companyId, List<Long> roomIds, ReservationListSearchRequestDto reservationListSearchRequestDto) {
        return queryFactory
                .select(Projections.constructor(ReservationQueryDto.class,
                                reservation.id,
                                reservation.title,
                                Projections.constructor(ReservationRoomQueryDto.class,
                                        room.id,
                                        roomDisplayNameExpr()
                                ),
                                reservation.startAt,
                                reservation.endAt,
                                reservation.status,
                                Projections.constructor(ReservationApplicantQueryDto.class,
                                        user.id,
                                        applicantDisplayNameExpr())
                        )
                )
                .from(reservation)
                .leftJoin(reservation.room, room)
                .leftJoin(reservation.companyMember, applicantMember)
                .leftJoin(applicantMember.user, user)
                .where(
                        eqCompanyId(companyId),
                        eqStatus(reservationListSearchRequestDto),
                        fromDateGoe(reservationListSearchRequestDto),
                        toDateLt(reservationListSearchRequestDto),
                        timePeriodEq(reservationListSearchRequestDto),
                        userInvolvement(currentUserId, reservationListSearchRequestDto),
                        room.id.in(roomIds)
                ).fetch();
    }

    /*
     * 예약 ID 기준 상세 정보를 조회합니다.
     *
     * SELECT r.id, r.title, rm.id,
     *        CASE WHEN rm.is_deleted = TRUE THEN CONCAT(rm.name, ' (삭제 됨)') ELSE rm.name END AS room_name,
     *        r.start_at, r.end_at, r.status, u.id,
     *        CASE WHEN am.status = 'RESIGNED' THEN CONCAT(u.nickname, ' (전 직원)') ELSE u.nickname END AS applicant_nickname
     * FROM reservation r
     * LEFT JOIN room rm ON r.room_id = rm.id
     * LEFT JOIN company_member am ON r.company_member_id = am.id
     * LEFT JOIN user u ON am.user_id = u.id
     * WHERE r.id = ?
     */
    @Override
    public Optional<ReservationQueryDto> getReservationById(Long currentUserId, Long reservationId) {
        ReservationQueryDto reservationQueryDto = queryFactory
                .select(Projections.constructor(ReservationQueryDto.class,
                                reservation.id,
                                reservation.title,
                                Projections.constructor(ReservationRoomQueryDto.class,
                                        room.id,
                                        roomDisplayNameExpr()
                                ),
                                reservation.startAt,
                                reservation.endAt,
                                reservation.status,
                                Projections.constructor(ReservationApplicantQueryDto.class,
                                        user.id,
                                        applicantDisplayNameExpr())
                        )
                )
                .from(reservation)
                .leftJoin(reservation.room, room)
                .leftJoin(reservation.companyMember, applicantMember)
                .leftJoin(applicantMember.user, user)
                .where(reservation.id.eq(reservationId))
                .fetchOne();

        return Optional.ofNullable(reservationQueryDto);
    }

    /*
     * 회사 ID 필터 조건을 생성합니다.
     *
     * WHERE rm.company_id = ?
     *   (omit WHEN companyId IS NULL)
     */
    private BooleanExpression eqCompanyId(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return room.company.id.eq(companyId);
    }

    /*
     * 예약 상태 필터 조건을 생성합니다.
     *
     * WHERE r.status = ?
     *   (omit WHEN status IS NULL)
     */
    private BooleanExpression eqStatus(ReservationListSearchRequestDto reservationListSearchRequestDto) {
        if (reservationListSearchRequestDto == null || reservationListSearchRequestDto.getStatus() == null) {
            return null;
        }
        return reservation.status.eq(reservationListSearchRequestDto.getStatus());
    }

    /*
     * 시작일 필터 조건을 생성합니다.
     *
     * WHERE r.start_at >= :fromDateStartOfDay
     *   (omit WHEN fromDate IS NULL)
     */
    private BooleanExpression fromDateGoe(ReservationListSearchRequestDto reservationListSearchRequestDto) {
        if(reservationListSearchRequestDto == null || reservationListSearchRequestDto.getFromDate() == null) {
            return null;
        }
        LocalDate fromDate = reservationListSearchRequestDto.getFromDate();
        return reservation.startAt.goe(fromDate.atStartOfDay());
    }

    /*
     * 종료일 필터 조건을 생성합니다.
     *
     * WHERE r.end_at < :toDatePlusOneStartOfDay
     *   (omit WHEN toDate IS NULL)
     */
    private BooleanExpression toDateLt(ReservationListSearchRequestDto reservationListSearchRequestDto) {
        if (reservationListSearchRequestDto == null || reservationListSearchRequestDto.getToDate() == null) {
            return null;
        }
        LocalDate toDate = reservationListSearchRequestDto.getToDate();
        return reservation.endAt.lt(toDate.plusDays(1).atStartOfDay());
    }

    /*
     * 시간대 필터 조건을 생성합니다.
     *
     * WHERE EXTRACT(HOUR FROM r.start_at) < 12   WHEN MORNING
     * WHERE EXTRACT(HOUR FROM r.start_at) >= 12  WHEN AFTERNOON
     *   (omit WHEN timePeriod IS NULL)
     */
    private BooleanExpression timePeriodEq(ReservationListSearchRequestDto reservationListSearchRequestDto) {
        if (reservationListSearchRequestDto == null || reservationListSearchRequestDto.getTimePeriod() == null) {
            return null;
        }

        TimePeriod dayPart = reservationListSearchRequestDto.getTimePeriod();
        var hourExpr = Expressions.numberTemplate(Integer.class, "extract(hour from {0})", reservation.startAt);
        return switch (dayPart) {
            case MORNING -> hourExpr.lt(12);
            case AFTERNOON -> hourExpr.goe(12);
        };
    }

    /*
     * 신청자/참여자 조건을 조합한 필터를 생성합니다.
     *
     * WHERE am.user_id = ?
     *    OR EXISTS (
     *         SELECT 1 FROM reservation_participant rp
     *         JOIN company_member pcm ON rp.company_member_id = pcm.id
     *         WHERE rp.reservation_id = r.id AND pcm.user_id = ?
     *       )
     *   (variants by applicantOnly / participatedOnly; omit WHEN both false)
     */
    private BooleanExpression userInvolvement(Long currentUserId, ReservationListSearchRequestDto reservationListSearchRequestDto) {
        if(reservationListSearchRequestDto == null) {
            return null;
        }

        boolean applicantOnly = Boolean.TRUE.equals(reservationListSearchRequestDto.getApplicantOnly());
        boolean participated = Boolean.TRUE.equals(reservationListSearchRequestDto.getParticipatedOnly());
        if (!applicantOnly && !participated) {
            return null;
        }
        BooleanExpression iAmApplicant = reservation.companyMember.user.id.eq(currentUserId);
        BooleanExpression iAmParticipant = JPAExpressions.selectOne()
                .from(reservationParticipant)
                .where(
                        reservationParticipant.reservation.eq(reservation),
                        reservationParticipant.companyMember.user.id.eq(currentUserId)
                )
                .exists();
        if (applicantOnly && participated) {
            return iAmApplicant.or(iAmParticipant);
        }
        if (applicantOnly) {
            return iAmApplicant;
        }
        return iAmParticipant;
    }

    /*
     * 회의실 표시명을 생성합니다.
     *
     * CASE WHEN rm.is_deleted = TRUE THEN CONCAT(rm.name, ' (삭제 됨)') ELSE rm.name END
     */
    private StringExpression roomDisplayNameExpr() {
        return Expressions.stringTemplate(
                "case when {0} = true then concat({1}, ' (삭제 됨)') else {1} end",
                room.deleted,
                room.name
        );
    }

    /*
     * 신청자 표시명을 생성합니다.
     *
     * CASE WHEN am.status = 'RESIGNED' THEN CONCAT(u.nickname, ' (전 직원)') ELSE u.nickname END
     */
    private StringExpression applicantDisplayNameExpr() {
        return Expressions.stringTemplate(
                "case when {0} = 'RESIGNED' then concat({1}, ' (전 직원)') else {1} end",
                applicantMember.status.stringValue(),
                user.nickname
        );
    }

    /*
     * 정렬 요청을 ORDER BY 절로 변환합니다.
     *
     * ORDER BY r.created_at ASC|DESC  WHEN sort=createdAt
     * ORDER BY r.id ASC              OTHERWISE
     */
    private List<OrderSpecifier<?>> resolveSortOrders(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (!sort.isSorted()) {
            return List.of(reservation.id.asc());
        }

        for (Sort.Order order : sort) {
            ReservationSortBy by = ReservationSortBy.fromRequest(order.getProperty()).orElse(null);
            if (by == ReservationSortBy.CREATED_AT) {
                return List.of(order.isAscending() ? reservation.createdAt.asc() : reservation.createdAt.desc());
            }
        }

        return List.of(reservation.id.asc());
    }
}
