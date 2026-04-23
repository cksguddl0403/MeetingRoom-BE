package kr.co.ta9.meetingroom.domain.notice.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.notice.dto.NoticeListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.notice.entity.Notice;
import kr.co.ta9.meetingroom.domain.notice.entity.QNotice;
import kr.co.ta9.meetingroom.domain.notice.enums.NoticeSortBy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QNotice notice = QNotice.notice;

    /*
     * 공지 목록과 전체 건수를 페이징 조회합니다.
     *
     * SELECT COUNT(n.id)
     * FROM notice n
     * LEFT JOIN category c ON n.category_id = c.id
     * WHERE (:categoryId IS NULL OR n.category_id = :categoryId)
     *   AND (:title IS NULL OR n.title LIKE CONCAT('%', :title, '%'))
     *
     * SELECT n.*
     * FROM notice n
     * LEFT JOIN FETCH n.noticeCategory c
     * WHERE (:categoryId IS NULL OR n.category_id = :categoryId)
     *   AND (:title IS NULL OR n.title LIKE CONCAT('%', :title, '%'))
     * ORDER BY n.created_at ASC|DESC
     * -- sort=viewCount: ORDER BY n.view_count ASC|DESC
     * -- 정렬 미지정/미지원: ORDER BY n.id DESC
     * LIMIT ? OFFSET ?
     */
    @Override
    public Page<Notice> getNotices(NoticeListSearchRequestDto noticeListSearchRequestDto, Pageable pageable) {
        Long total = queryFactory
                .select(notice.id.count())
                .from(notice)
                .where(
                        eqCategoryId(noticeListSearchRequestDto),
                        titleContains(noticeListSearchRequestDto)
                )
                .fetchOne();

        long totalElements = total == null ? 0L : total;

        List<Notice> content = queryFactory
                .selectFrom(notice)
                .leftJoin(notice.noticeCategory).fetchJoin()
                .where(
                        eqCategoryId(noticeListSearchRequestDto),
                        titleContains(noticeListSearchRequestDto)
                )
                .orderBy(resolveSortOrders(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, totalElements);
    }

    /*
     * 카테고리 필터 조건을 생성합니다.
     *
     * WHERE n.category_id = ?
     *   (categoryId가 NULL이면 조건 생략)
     */
    private BooleanExpression eqCategoryId(NoticeListSearchRequestDto noticeListSearchRequestDto) {
        if (noticeListSearchRequestDto == null || noticeListSearchRequestDto.getCategoryId() == null) {
            return null;
        }
        return notice.noticeCategory.id.eq(noticeListSearchRequestDto.getCategoryId());
    }

    /*
     * 제목 검색 필터 조건을 생성합니다.
     *
     * WHERE n.title LIKE CONCAT('%', ?, '%')
     *   (title이 NULL 또는 공백이면 조건 생략)
     */
    private BooleanExpression titleContains(NoticeListSearchRequestDto noticeListSearchRequestDto) {
        if (noticeListSearchRequestDto == null || !StringUtils.hasText(noticeListSearchRequestDto.getTitle())) {
            return null;
        }
        return notice.title.contains(noticeListSearchRequestDto.getTitle());
    }

    /*
     * 정렬 요청을 ORDER BY 절로 변환합니다.
     *
     * ORDER BY n.created_at ASC|DESC   (sort=createdAt)
     * ORDER BY n.view_count ASC|DESC   (sort=viewCount)
     * ORDER BY n.id DESC               (sort 미지정)
     * ORDER BY n.id DESC               (지원하지 않는 sort)
     */
    private List<OrderSpecifier<?>> resolveSortOrders(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (!sort.isSorted()) {
            return List.of(notice.id.desc());
        }
        for (Sort.Order order : sort) {
            NoticeSortBy by = NoticeSortBy.fromRequest(order.getProperty()).orElse(null);
            if (by == NoticeSortBy.CREATED_AT) {
                return List.of(order.isAscending() ? notice.createdAt.asc() : notice.createdAt.desc());
            }
            if (by == NoticeSortBy.VIEW_COUNT) {
                return List.of(order.isAscending() ? notice.viewCount.asc() : notice.viewCount.desc());
            }
        }
        return List.of(notice.id.desc());
    }
}
