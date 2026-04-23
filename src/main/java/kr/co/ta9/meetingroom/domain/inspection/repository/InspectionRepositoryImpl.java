package kr.co.ta9.meetingroom.domain.inspection.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionQueryDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionRoomQueryDto;
import kr.co.ta9.meetingroom.domain.inspection.entity.QInspection;
import kr.co.ta9.meetingroom.domain.room.entity.QRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class InspectionRepositoryImpl implements InspectionRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QInspection inspection = QInspection.inspection;
    private final QRoom room = QRoom.room;

    /*
     * 점검 ID 기준 단건 정보를 조회합니다.
     *
     * SELECT i.id, i.name, i.start_at, i.end_at, i.created_at, rm.id, rm.name
     * FROM inspection i
     * LEFT JOIN room rm ON i.room_id = rm.id
     * WHERE i.id = ?
     */
    @Override
    public Optional<InspectionQueryDto> getInspectionById(Long inspectionId) {
        InspectionQueryDto inspectionQueryDto = queryFactory
                .select(Projections.constructor(
                        InspectionQueryDto.class,
                        inspection.id,
                        inspection.name,
                        inspection.startAt,
                        inspection.endAt,
                        inspection.createdAt,
                        Projections.constructor(
                                InspectionRoomQueryDto.class,
                                room.id,
                                room.name
                        )
                ))
                .from(inspection)
                .leftJoin(inspection.room, room)
                .where(
                        inspectionIdEq(inspectionId)
                )
                .fetchOne();

        return Optional.ofNullable(inspectionQueryDto);
    }

    /*
     * 점검 목록과 전체 건수를 페이징 조회합니다.
     *
     * SELECT COUNT(i.id)
     * FROM inspection i
     * LEFT JOIN room rm ON i.room_id = rm.id
     * WHERE rm.company_id = :companyId
     *   AND (:roomId IS NULL OR rm.id = :roomId)
     *   AND (:name IS NULL OR i.name LIKE CONCAT('%', :name, '%'))
     *   AND (:fromDate IS NULL OR i.end_at >= :fromDateStartOfDay)
     *   AND (:toDate IS NULL OR i.start_at < :toDatePlusOneStartOfDay)
     *
     * SELECT i.id, i.name, i.start_at, i.end_at, i.created_at, rm.id, rm.name
     * FROM inspection i
     * LEFT JOIN room rm ON i.room_id = rm.id
     * WHERE rm.company_id = :companyId
     *   AND (:roomId IS NULL OR rm.id = :roomId)
     *   AND (:name IS NULL OR i.name LIKE CONCAT('%', :name, '%'))
     *   AND (:fromDate IS NULL OR i.end_at >= :fromDateStartOfDay)
     *   AND (:toDate IS NULL OR i.start_at < :toDatePlusOneStartOfDay)
     * ORDER BY i.created_at ASC|DESC
     * -- 정렬 미지정 시: ORDER BY i.id DESC
     * -- 지원하지 않는 정렬 필드 지정 시: ORDER BY i.id ASC
     * LIMIT ? OFFSET ?
     */
    @Override
    public Page<InspectionQueryDto> getInspections(Long companyId, Pageable pageable, InspectionListSearchRequestDto inspectionListSearchRequestDto) {
        Long total = queryFactory
                .select(inspection.count())
                .from(inspection)
                .leftJoin(inspection.room, room)
                .where(
                        companyIdEq(companyId),
                        roomIdEq(inspectionListSearchRequestDto),
                        nameContains(inspectionListSearchRequestDto),
                        fromDateGoe(inspectionListSearchRequestDto),
                        toDateLt(inspectionListSearchRequestDto)
                )
                .fetchOne();

        long totalElements = total ==  null ? 0L : total;

        List<InspectionQueryDto> content = queryFactory
                .select(Projections.constructor(
                        InspectionQueryDto.class,
                        inspection.id,
                        inspection.name,
                        inspection.startAt,
                        inspection.endAt,
                        inspection.createdAt,
                        Projections.constructor(
                                InspectionRoomQueryDto.class,
                                room.id,
                                room.name
                        )
                ))
                .from(inspection)
                .leftJoin(inspection.room, room)
                .where(companyIdEq(companyId),
                        roomIdEq(inspectionListSearchRequestDto),
                        nameContains(inspectionListSearchRequestDto),
                        fromDateGoe(inspectionListSearchRequestDto),
                        toDateLt(inspectionListSearchRequestDto)
                )
                .orderBy(resolveSortOrders(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, totalElements);

    }

    /*
     * 조건 기반 점검 목록을 조회합니다.
     *
     * SELECT i.id, i.name, i.start_at, i.end_at, i.created_at, rm.id, rm.name
     * FROM inspection i
     * LEFT JOIN room rm ON i.room_id = rm.id
     * WHERE rm.company_id = :companyId
     *   AND (:roomId IS NULL OR rm.id = :roomId)
     *   AND (:name IS NULL OR i.name LIKE CONCAT('%', :name, '%'))
     *   AND (:fromDate IS NULL OR i.end_at >= :fromDateStartOfDay)
     *   AND (:toDate IS NULL OR i.start_at < :toDatePlusOneStartOfDay)
     */
    @Override
    public List<InspectionQueryDto> getAllInspections(Long companyId, InspectionListSearchRequestDto inspectionListSearchRequestDto) {
        return queryFactory
                .select(Projections.constructor(
                        InspectionQueryDto.class,
                        inspection.id,
                        inspection.name,
                        inspection.startAt,
                        inspection.endAt,
                        inspection.createdAt,
                        Projections.constructor(
                                InspectionRoomQueryDto.class,
                                room.id,
                                room.name
                        )
                ))
                .from(inspection)
                .leftJoin(inspection.room, room)
                .where(
                        companyIdEq(companyId),
                        roomIdEq(inspectionListSearchRequestDto),
                        nameContains(inspectionListSearchRequestDto),
                        fromDateGoe(inspectionListSearchRequestDto),
                        toDateLt(inspectionListSearchRequestDto)
                )
                .fetch();
    }

    /*
     * 점검 ID 일치 조건을 생성합니다.
     *
     * WHERE i.id = ?
     */
    private BooleanExpression inspectionIdEq(Long inspectionId) {
        return inspection.id.eq(inspectionId);
    }

    /*
     * 회사 ID 일치 조건을 생성합니다.
     *
     * WHERE rm.company_id = ?
     */
    private BooleanExpression companyIdEq(Long companyId) {
        return room.company.id.eq(companyId);
    }

    /*
     * 회의실 ID 필터 조건을 생성합니다.
     *
     * WHERE rm.id = ?
     *   (omit WHEN roomId IS NULL)
     */
    private BooleanExpression roomIdEq(InspectionListSearchRequestDto inspectionListSearchRequestDto) {
        if (inspectionListSearchRequestDto == null || inspectionListSearchRequestDto.getRoomId() == null) {
            return null;
        }
        return room.id.eq(inspectionListSearchRequestDto.getRoomId());
    }

    /*
     * 정렬 요청을 ORDER BY 절로 변환합니다.
     *
     * ORDER BY i.created_at ASC|DESC  WHEN sort=createdAt
     * ORDER BY i.id DESC              WHEN sort unspecified
     * ORDER BY i.id ASC               WHEN unsupported property
     */
    private List<OrderSpecifier<?>> resolveSortOrders(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (!sort.isSorted()) {
            return List.of(inspection.id.desc());
        }
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            if ("createdAt".equals(property)) {
                return List.of(order.isAscending() ? inspection.createdAt.asc() : inspection.createdAt.desc());
            }
        }
        return List.of(inspection.id.asc());
    }

    /*
     * 점검명 검색 필터 조건을 생성합니다.
     *
     * WHERE i.name LIKE CONCAT('%', ?, '%')
     *   (omit WHEN search name IS NULL OR blank)
     */
    private BooleanExpression nameContains(InspectionListSearchRequestDto inspectionListSearchRequestDto) {
        if (inspectionListSearchRequestDto == null) {
            return null;
        }
        String title = inspectionListSearchRequestDto.getName();
        if (!StringUtils.hasText(title)) {
            return null;
        }
        return inspection.name.contains(title);
    }

    /*
     * 시작일 하한 필터 조건을 생성합니다.
     *
     * WHERE i.end_at >= :fromDateStartOfDay
     *   (omit WHEN fromDate IS NULL)
     */
    private BooleanExpression fromDateGoe(InspectionListSearchRequestDto inspectionListSearchRequestDto) {
        if (inspectionListSearchRequestDto == null || inspectionListSearchRequestDto.getFromDate() == null) {
            return null;
        }
        LocalDate fromDate = inspectionListSearchRequestDto.getFromDate();
        return inspection.endAt.goe(fromDate.atStartOfDay());
    }

    /*
     * 종료일 상한 필터 조건을 생성합니다.
     *
     * WHERE i.start_at < :toDatePlusOneStartOfDay
     *   (omit WHEN toDate IS NULL)
     */
    private BooleanExpression toDateLt(InspectionListSearchRequestDto inspectionListSearchRequestDto) {
        if (inspectionListSearchRequestDto == null || inspectionListSearchRequestDto.getToDate() == null) {
            return null;
        }
        LocalDate toDate = inspectionListSearchRequestDto.getToDate();
        return inspection.startAt.lt(toDate.plusDays(1).atStartOfDay());
    }
}
