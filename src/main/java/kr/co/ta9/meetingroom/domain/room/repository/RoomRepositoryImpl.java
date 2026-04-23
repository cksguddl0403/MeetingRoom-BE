package kr.co.ta9.meetingroom.domain.room.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.equipment.entity.QRoomEquipment;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import kr.co.ta9.meetingroom.domain.room.dto.EquipmentSearchRequestDto;
import kr.co.ta9.meetingroom.domain.room.dto.RoomSearchRequestDto;
import kr.co.ta9.meetingroom.domain.inspection.entity.QInspection;
import kr.co.ta9.meetingroom.domain.reservation.entity.QReservation;
import kr.co.ta9.meetingroom.domain.room.dto.RoomQueryDto;
import kr.co.ta9.meetingroom.domain.room.entity.QRoom;
import kr.co.ta9.meetingroom.domain.room.enums.RoomSortBy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QRoom room = QRoom.room;

    private final QInspection inspection = QInspection.inspection;

    private final QReservation reservation = QReservation.reservation;

    private final QRoomEquipment roomEquipment = QRoomEquipment.roomEquipment;

    /*
     * 특정 시점 기준 회의실 단건 정보를 조회합니다.
     *
     * SELECT rm.id,
     *        CASE WHEN rm.is_deleted = TRUE
     *             THEN CONCAT(SUBSTRING_INDEX(rm.name, '_deleted_', 1), ' (삭제 됨)')
     *             ELSE rm.name END AS name,
     *        rm.max_capacity, rm.company_id,
     *        CASE
     *          WHEN EXISTS (
     *            SELECT 1 FROM inspection i
     *            WHERE i.room_id = rm.id
     *              AND i.start_at <= :at
     *              AND i.end_at >= :at
     *          ) THEN '점검 중'
     *          WHEN EXISTS (
     *            SELECT 1 FROM reservation r
     *            WHERE r.room_id = rm.id
     *              AND r.status = 'CONFIRMED'
     *              AND r.start_at <= :at
     *              AND r.end_at >= :at
     *          ) THEN '사용 중'
     *          ELSE '사용가능'
     *        END
     * FROM room rm
     * WHERE rm.id = ?
     *   AND rm.is_deleted = FALSE
     */
    @Override
    public Optional<RoomQueryDto> getRoomById(Long roomId, LocalDateTime at) {
        RoomQueryDto roomQueryDto = queryFactory
                .select(Projections.constructor(
                        RoomQueryDto.class,
                        room.id,
                        roomDisplayNameExpression(),
                        room.maxCapacity,
                        room.company.id,
                        resolveRoomStatusExpression(at)
                ))
                .from(room)
                .where(
                        room.id.eq(roomId),
                        isNotDeleted()
                )
                .fetchOne();

        return Optional.ofNullable(roomQueryDto);
    }

    /*
     * 필터 조건으로 회의실 목록을 페이징 조회합니다.
     *
     * SELECT COUNT(rm.id)
     * FROM room rm
     * WHERE rm.is_deleted = FALSE
     *   AND rm.company_id = :companyId
     *   AND (:maxCapacity IS NULL OR rm.max_capacity >= :maxCapacity)
     *   AND (
     *     :equipmentFiltersEmpty = TRUE
     *     OR (
     *       EXISTS (
     *         SELECT 1
     *         FROM room_equipment re
     *         WHERE re.room_id = rm.id
     *           AND re.equipment_id = :equipmentId1
     *           AND (:minQuantity1 IS NULL OR re.quantity >= :minQuantity1)
     *           AND (:status1 IS NULL OR re.status = :status1)
     *       )
     *       -- 비품 조건 개수만큼 AND EXISTS가 추가됨
     *     )
     *   )
     *
     * SELECT rm.id,
     *        CASE WHEN rm.is_deleted = TRUE
     *             THEN CONCAT(SUBSTRING_INDEX(rm.name, '_deleted_', 1), ' (삭제 됨)')
     *             ELSE rm.name END AS name,
     *        rm.max_capacity, rm.company_id,
     *        CASE
     *          WHEN EXISTS (
     *            SELECT 1 FROM inspection i
     *            WHERE i.room_id = rm.id
     *              AND i.start_at <= :at
     *              AND i.end_at >= :at
     *          ) THEN '점검 중'
     *          WHEN EXISTS (
     *            SELECT 1 FROM reservation r
     *            WHERE r.room_id = rm.id
     *              AND r.status = 'CONFIRMED'
     *              AND r.start_at <= :at
     *              AND r.end_at >= :at
     *          ) THEN '사용 중'
     *          ELSE '사용가능'
     *        END
     * FROM room rm
     * WHERE rm.is_deleted = FALSE
     *   AND rm.company_id = :companyId
     *   AND (:maxCapacity IS NULL OR rm.max_capacity >= :maxCapacity)
     *   AND (
     *     :equipmentFiltersEmpty = TRUE
     *     OR (
     *       EXISTS (
     *         SELECT 1
     *         FROM room_equipment re
     *         WHERE re.room_id = rm.id
     *           AND re.equipment_id = :equipmentId1
     *           AND (:minQuantity1 IS NULL OR re.quantity >= :minQuantity1)
     *           AND (:status1 IS NULL OR re.status = :status1)
     *       )
     *       -- 비품 조건 개수만큼 AND EXISTS가 추가됨
     *     )
     *   )
     * ORDER BY rm.max_capacity ASC|DESC
     * -- 그 외(미지정/미지원): ORDER BY rm.id ASC
     * LIMIT ? OFFSET ?
     */
    @Override
    public Page<RoomQueryDto> getRooms(
            Long companyId,
            RoomSearchRequestDto roomSearchRequestDto,
            LocalDateTime at,
            Pageable pageable
    ) {
        Long total = queryFactory
                .select(room.id.count())
                .from(room)
                .where(
                        isNotDeleted(),
                        eqCompanyId(companyId),
                        maxCapacityGoe(roomSearchRequestDto),
                        equipmentConditionsAll(roomSearchRequestDto)
                )
                .fetchOne();

        long totalElements = total == null ? 0L : total;

        List<RoomQueryDto> content = queryFactory
                .select(Projections.constructor(
                        RoomQueryDto.class,
                        room.id,
                        roomDisplayNameExpression(),
                        room.maxCapacity,
                        room.company.id,
                        resolveRoomStatusExpression(at)
                ))
                .from(room)
                .where(
                        isNotDeleted(),
                        eqCompanyId(companyId),
                        maxCapacityGoe(roomSearchRequestDto),
                        equipmentConditionsAll(roomSearchRequestDto)
                )
                .orderBy(resolveSortOrders(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, totalElements);
    }

    /*
     * 특정 회사의 회의실 목록을 조회합니다.
     *
     * SELECT rm.id,
     *        CASE WHEN rm.is_deleted = TRUE
     *             THEN CONCAT(SUBSTRING_INDEX(rm.name, '_deleted_', 1), ' (삭제 됨)')
     *             ELSE rm.name END AS name,
     *        rm.max_capacity, rm.company_id,
     *        CASE
     *          WHEN EXISTS (
     *            SELECT 1 FROM inspection i
     *            WHERE i.room_id = rm.id
     *              AND i.start_at <= :at
     *              AND i.end_at >= :at
     *          ) THEN '점검 중'
     *          WHEN EXISTS (
     *            SELECT 1 FROM reservation r
     *            WHERE r.room_id = rm.id
     *              AND r.status = 'CONFIRMED'
     *              AND r.start_at <= :at
     *              AND r.end_at >= :at
     *          ) THEN '사용 중'
     *          ELSE '사용가능'
     *        END
     * FROM room rm
     * WHERE rm.company_id = ?
     *   AND rm.is_deleted = FALSE
     */
    @Override
    public List<RoomQueryDto> getAllRooms(Long companyId, LocalDateTime at) {
        return queryFactory
                .select(Projections.constructor(
                        RoomQueryDto.class,
                        room.id,
                        roomDisplayNameExpression(),
                        room.maxCapacity,
                        room.company.id,
                        resolveRoomStatusExpression(at)
                ))
                .from(room)
                .where(
                        isNotDeleted(),
                        eqCompanyId(companyId)
                )
                .fetch();
    }


    /*
     * 회사 ID 필터 조건을 생성합니다.
     *
     * WHERE rm.company_id = ?
     *   (companyId가 NULL이면 조건 생략)
     */
    private BooleanExpression eqCompanyId(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return room.company.id.eq(companyId);
    }

    /*
     * 소프트 삭제 제외 조건을 생성합니다.
     *
     * WHERE rm.is_deleted = FALSE
     */
    private BooleanExpression isNotDeleted() {
        return room.deleted.isFalse();
    }

    /*
     * 최소 수용 인원 필터 조건을 생성합니다.
     *
     * WHERE rm.max_capacity >= ?
     *   (maxCapacity가 NULL이면 조건 생략)
     */
    private BooleanExpression maxCapacityGoe(RoomSearchRequestDto roomSearchRequestDto) {
        if (roomSearchRequestDto == null || roomSearchRequestDto.getMaxCapacity() == null) {
            return null;
        }
        return room.maxCapacity.goe(roomSearchRequestDto.getMaxCapacity());
    }

    /*
     * 비품 조건 목록을 AND EXISTS 조건으로 결합합니다.
     *
     * AND EXISTS (
     *   SELECT 1 FROM room_equipment re
     *   WHERE re.room_id = rm.id AND re.equipment_id = ? AND ...
     * )
     *   (비품 조건 개수만큼 AND EXISTS 반복, 목록이 비어 있으면 조건 생략)
     */
    private BooleanExpression equipmentConditionsAll(RoomSearchRequestDto roomSearchRequestDto) {
        if (roomSearchRequestDto == null) {
            return null;
        }

        List<EquipmentSearchRequestDto> equipmentSearchRequestDtos = roomSearchRequestDto.getEquipmentSearchRequestDtos() == null
                ? List.of()
                : roomSearchRequestDto.getEquipmentSearchRequestDtos();
        if (equipmentSearchRequestDtos.isEmpty()) {
            return null;
        }

        BooleanExpression combined = null;
        for (EquipmentSearchRequestDto equipmentSearchRequestDto : equipmentSearchRequestDtos) {
            BooleanExpression exists = equipmentExists(equipmentSearchRequestDto);
            if (exists == null) {
                continue;
            }
            combined = combined == null ? exists : combined.and(exists);
        }
        return combined;
    }

    private BooleanExpression equipmentExists(EquipmentSearchRequestDto equipmentSearchRequestDto) {
        if (equipmentSearchRequestDto == null || equipmentSearchRequestDto.getEquipmentId() == null) {
            return null;
        }

        BooleanExpression subQueryWhere = roomEquipment.room.id.eq(room.id)
                .and(roomEquipment.equipment.id.eq(equipmentSearchRequestDto.getEquipmentId()));

        if (equipmentSearchRequestDto.getMinQuantity() != null) {
            subQueryWhere = subQueryWhere.and(roomEquipment.quantity.goe(equipmentSearchRequestDto.getMinQuantity()));
        }
        if (equipmentSearchRequestDto.getStatus() != null) {
            subQueryWhere = subQueryWhere.and(roomEquipment.status.eq(equipmentSearchRequestDto.getStatus()));
        }

        return JPAExpressions.selectOne()
                .from(roomEquipment)
                .where(subQueryWhere)
                .exists();
    }

    /*
     * 정렬 요청을 ORDER BY 절로 변환합니다.
     *
     * ORDER BY rm.max_capacity ASC|DESC  (sort=maxCapacity)
     * ORDER BY rm.id ASC                 (그 외)
     */
    private List<OrderSpecifier<?>> resolveSortOrders(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (!sort.isSorted()) {
            return List.of(room.id.asc());
        }

        for (Sort.Order order : sort) {
            RoomSortBy by = RoomSortBy.fromRequest(order.getProperty()).orElse(null);
            if (by == RoomSortBy.MAX_CAPACITY) {
                return List.of(order.isAscending() ? room.maxCapacity.asc() : room.maxCapacity.desc());
            }
        }

        return List.of(room.id.asc());
    }

    /*
     * 특정 시점 기준 회의실 상태 CASE 식을 생성합니다.
     *
     * CASE
     *   WHEN EXISTS (SELECT 1 FROM inspection i
     *                WHERE i.room_id = rm.id AND i.start_at <= :at AND i.end_at >= :at)
     *        THEN '점검 중'
     *   WHEN EXISTS (SELECT 1 FROM reservation r
     *                WHERE r.room_id = rm.id AND r.status = 'CONFIRMED'
     *                  AND r.start_at <= :at AND r.end_at >= :at)
     *        THEN '사용 중'
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

    private Expression<String> roomDisplayNameExpression() {
        return Expressions.stringTemplate(
                "case when {0} = true then concat(function('substring_index', {1}, '_deleted_', 1), ' (삭제 됨)') else {1} end",
                room.deleted,
                room.name
        );
    }
}
