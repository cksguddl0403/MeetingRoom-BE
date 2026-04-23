package kr.co.ta9.meetingroom.domain.equipment.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentQueryDto;
import kr.co.ta9.meetingroom.domain.equipment.entity.QEquipment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EquipmentRepositoryImpl implements EquipmentRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QEquipment equipment = QEquipment.equipment;

    /*
     * 회사별 비품 단건 정보를 조회합니다.
     *
     * SELECT e.id,
     *        CASE WHEN e.is_deleted = TRUE
     *             THEN CONCAT(SUBSTRING_INDEX(e.name, '_deleted_', 1), ' (삭제 됨)')
     *             ELSE e.name END AS name,
     *        e.created_at
     * FROM equipment e
     * WHERE e.id = ?
     *   AND e.is_deleted = FALSE
     * LIMIT 1
     */

    @Override
    public Optional<EquipmentQueryDto> getByEquipmentId(Long equipmentId) {
        EquipmentQueryDto equipmentQueryDto = queryFactory
                .select(Projections.constructor(EquipmentQueryDto.class,
                        equipment.id,
                        equipmentDisplayNameExpr(),
                        equipment.createdAt
                ))
                .from(equipment)
                .where(
                        equipment.id.eq(equipmentId),
                        isNotDeleted()
                )
                .fetchOne();

        return Optional.ofNullable(equipmentQueryDto);
    }

    /*
     * 회사별 비품 목록과 전체 건수를 페이징 조회합니다.
     *
     * SELECT e.id,
     *        CASE WHEN e.is_deleted = TRUE
     *             THEN CONCAT(SUBSTRING_INDEX(e.name, '_deleted_', 1), ' (삭제 됨)')
     *             ELSE e.name END AS name,
     *        e.created_at
     * FROM equipment e
     * WHERE e.company_id = ?
     *   AND e.is_deleted = FALSE
     *   AND ( :name IS NULL OR e.name LIKE CONCAT('%', :name, '%') )
     * ORDER BY e.created_at ASC|DESC
     * -- 정렬 미지정 시: ORDER BY e.id DESC
     * -- 지원하지 않는 정렬 필드 지정 시: ORDER BY e.id ASC
     * LIMIT ? OFFSET ?
     *
     * SELECT COUNT(e.id)
     * FROM equipment e
     * WHERE e.company_id = ?
     *   AND e.is_deleted = FALSE
     */
    @Override
    public Page<EquipmentQueryDto> getEquipments(Long companyId, String name, Pageable pageable) {
        List<EquipmentQueryDto> rows = queryFactory
                .select(Projections.constructor(EquipmentQueryDto.class,
                        equipment.id,
                        equipmentDisplayNameExpr(),
                        equipment.createdAt
                ))
                .from(equipment)
                .where(
                        companyIdEq(companyId),
                        isNotDeleted(),
                        nameEq(name)
                )
                .orderBy(resolveSortOrders(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(equipment.count())
                .from(equipment)
                .where(
                        companyIdEq(companyId),
                        isNotDeleted()
                )
                .fetchOne();

        return new PageImpl<>(rows, pageable, total == null ? 0L : total);
    }

    /*
     * 회사별 비품 전체 목록을 조회합니다.
     *
     * SELECT e.id,
     *        CASE WHEN e.is_deleted = TRUE
     *             THEN CONCAT(SUBSTRING_INDEX(e.name, '_deleted_', 1), ' (삭제 됨)')
     *             ELSE e.name END AS name,
     *        e.created_at
     * FROM equipment e
     * WHERE e.company_id = ?
     *   AND e.is_deleted = FALSE
     */
    @Override
    public List<EquipmentQueryDto> getAllEquipments(Long companyId) {
        return queryFactory
                .select(Projections.constructor(EquipmentQueryDto.class,
                        equipment.id,
                        equipmentDisplayNameExpr(),
                        equipment.createdAt
                ))
                .from(equipment)
                .where(
                        companyIdEq(companyId),
                        isNotDeleted()
                )
                .fetch();
    }

    /*
     * 회사 ID 필터 조건을 생성합니다.
     *
     * WHERE e.company_id = ?
     */
    private BooleanExpression companyIdEq(Long companyId) {
        return equipment.company.id.eq(companyId);
    }

    /*
     * 소프트 삭제 제외 조건을 생성합니다.
     *
     * WHERE e.is_deleted = FALSE
     */
    private BooleanExpression isNotDeleted() {
        return equipment.deleted.isFalse();
    }

    /*
     * 비품 이름 검색 필터 조건을 생성합니다.
     *
     * WHERE e.name LIKE CONCAT('%', ?, '%')
     *   (omit WHEN name IS NULL OR name = '')
     */
    private BooleanExpression nameEq(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        return equipment.name.contains(name);
    }

    /*
     * 정렬 요청을 ORDER BY 절로 변환합니다.
     *
     * ORDER BY e.created_at ASC|DESC  WHEN sort=createdAt
     * ORDER BY e.id DESC               WHEN sort 미지정
     * ORDER BY e.id ASC                WHEN sort 지정이나 필드 미지원
     */
    private List<OrderSpecifier<?>> resolveSortOrders(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (!sort.isSorted()) {
            return List.of(equipment.id.desc());
        }
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            if ("createdAt".equals(property)) {
                return List.of(order.isAscending() ? equipment.createdAt.asc() : equipment.createdAt.desc());
            }
        }
        return List.of(equipment.id.asc());
    }

    private StringExpression equipmentDisplayNameExpr() {
        return Expressions.stringTemplate(
                "case when {0} = true then concat(function('substring_index', {1}, '_deleted_', 1), ' (삭제 됨)') else {1} end",
                equipment.deleted,
                equipment.name
        );
    }
}
