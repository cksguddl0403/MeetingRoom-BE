package kr.co.ta9.meetingroom.domain.inquiry.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.inquiry.dto.*;
import kr.co.ta9.meetingroom.domain.inquiry.entity.QInquiry;
import kr.co.ta9.meetingroom.domain.inquiry.entity.QInquiryReply;
import kr.co.ta9.meetingroom.domain.inquiry.enums.InquirySortBy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom {

    private static final String PRIVATE_INQUIRY_TITLE = "비공개 글입니다.";

    private final JPAQueryFactory queryFactory;
    private final QInquiry inquiry = QInquiry.inquiry;
    private final QInquiryReply inquiryReply = QInquiryReply.inquiryReply;

    /*
     * 문의 ID 기준 상세 정보를 조회합니다.
     *
     * SELECT i.id, ic.id, ic.name,
     *        CASE WHEN i.secret AND i.user_id <> :currentUserId THEN '비공개 글입니다.' ELSE i.title END,
     *        i.content, u.id, u.nickname, i.secret,
     *        CASE WHEN EXISTS (SELECT 1 FROM inquiry_reply ir WHERE ir.inquiry_id = i.id) THEN true ELSE false END,
     *        i.created_at,
     *        ir.id, ir.content, NULL
     * FROM inquiry i
     * LEFT JOIN category ic ON i.category_id = ic.id
     * LEFT JOIN user u ON i.user_id = u.id
     * LEFT JOIN inquiry_reply ir ON ir.inquiry_id = i.id
     * WHERE i.id = ?
     */
    @Override
    public Optional<InquiryQueryDto> getInquiryById(Long currentUserId, Long inquiryId) {
        InquiryQueryDto inquiryQueryDto = queryFactory
                .select(Projections.constructor(InquiryQueryDto.class,
                        inquiry.id,
                        inquiry.inquiryCategory.id,
                        inquiry.inquiryCategory.name,
                        resolveTitleExpression(currentUserId),
                        inquiry.content,
                                Projections.constructor(InquiryAuthorQueryDto.class,
                                        inquiry.user.id,
                                        inquiry.user.nickname
                                ),
                        inquiry.secret,
                        resolveAnsweredExpression(),
                        inquiry.createdAt,
                        Projections.constructor(InquiryReplyQueryDto.class,
                                inquiryReply.id,
                                inquiryReply.content,
                                Expressions.nullExpression(InquiryReplyAuthorQueryDto.class)
                        )
                ))
                .from(inquiry)
                .leftJoin(inquiry.inquiryCategory).on(inquiry.inquiryCategory.id.eq(inquiry.inquiryCategory.id))
                .leftJoin(inquiry.user).on(inquiry.user.id.eq(inquiry.user.id))
                .leftJoin(inquiryReply).on(inquiryReply.inquiry.eq(inquiry))
                .where(inquiry.id.eq(inquiryId))
                .fetchOne();

        return Optional.ofNullable(inquiryQueryDto);
    }

    /*
     * 문의 목록과 전체 건수를 페이징 조회합니다.
     *
     * SELECT COUNT(i.id)
     * FROM inquiry i
     * WHERE (:categoryId IS NULL OR i.category_id = :categoryId)
     *   AND (:title IS NULL OR i.title LIKE CONCAT('%', :title, '%'))
     *   AND (:secret IS NULL OR i.secret = :secret)
     *   AND (:mineOnly IS NOT TRUE OR i.user_id = :currentUserId)
     *
     * SELECT i.id, ic.id, ic.name,
     *        CASE
     *          WHEN i.secret = TRUE AND i.user_id <> :currentUserId THEN '비공개 글입니다.'
     *          ELSE i.title
     *        END AS title,
     *        i.content, u.id, u.nickname, i.secret,
     *        CASE
     *          WHEN EXISTS (
     *            SELECT 1 FROM inquiry_reply ir2 WHERE ir2.inquiry_id = i.id
     *          ) THEN TRUE
     *          ELSE FALSE
     *        END AS answered,
     *        i.created_at,
     *        ir.id, ir.content, NULL
     * FROM inquiry i
     * LEFT JOIN category ic ON i.category_id = ic.id
     * LEFT JOIN user u ON i.user_id = u.id
     * LEFT JOIN inquiry_reply ir ON ir.inquiry_id = i.id
     * WHERE (:categoryId IS NULL OR i.category_id = :categoryId)
     *   AND (:title IS NULL OR i.title LIKE CONCAT('%', :title, '%'))
     *   AND (:secret IS NULL OR i.secret = :secret)
     *   AND (:mineOnly IS NOT TRUE OR i.user_id = :currentUserId)
     * ORDER BY i.created_at ASC|DESC
     * -- 그 외(미지정/미지원): ORDER BY i.id ASC
     * LIMIT ? OFFSET ?
     */
    @Override
    public Page<InquiryQueryDto> getInquiries(Long currentUserId, InquiryListSearchRequestDto inquiryListSearchRequestDto, Pageable pageable) {

        Long total = queryFactory
                .select(inquiry.id.count())
                .from(inquiry)
                .where(
                        eqCategoryId(inquiryListSearchRequestDto),
                        titleContains(inquiryListSearchRequestDto),
                        eqSecret(inquiryListSearchRequestDto),
                        eqAuthorWhenMineOnly(currentUserId, inquiryListSearchRequestDto)
                )
                .fetchOne();

        long totalElements = total == null ? 0L : total;

        List<InquiryQueryDto> content = queryFactory
                .select(Projections.constructor(InquiryQueryDto.class,
                        inquiry.id,
                        inquiry.inquiryCategory.id,
                        inquiry.inquiryCategory.name,
                        resolveTitleExpression(currentUserId),
                        inquiry.content,
                            Projections.constructor(InquiryAuthorQueryDto.class,
                                    inquiry.user.id,
                                    inquiry.user.nickname
                            ),
                        inquiry.secret,
                        resolveAnsweredExpression(),
                        inquiry.createdAt,
                        Projections.constructor(InquiryReplyQueryDto.class,
                                inquiryReply.id,
                                inquiryReply.content,
                                Expressions.nullExpression(InquiryReplyAuthorQueryDto.class)
                        )
                ))
                .from(inquiry)
                .leftJoin(inquiry.inquiryCategory)
                .leftJoin(inquiry.user)
                .leftJoin(inquiryReply).on(inquiryReply.inquiry.eq(inquiry))
                .where(
                        eqCategoryId(inquiryListSearchRequestDto),
                        titleContains(inquiryListSearchRequestDto),
                        eqSecret(inquiryListSearchRequestDto),
                        eqAuthorWhenMineOnly(currentUserId, inquiryListSearchRequestDto)
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
     * WHERE i.category_id = ?
     *   (categoryId가 NULL이면 조건 생략)
     */
    private BooleanExpression eqCategoryId(InquiryListSearchRequestDto inquiryListSearchRequestDto) {
        if (inquiryListSearchRequestDto == null || inquiryListSearchRequestDto.getCategoryId() == null) {
            return null;
        }
        return inquiry.inquiryCategory.id.eq(inquiryListSearchRequestDto.getCategoryId());
    }

    /*
     * 제목 검색 필터 조건을 생성합니다.
     *
     * WHERE i.title LIKE CONCAT('%', ?, '%')
     *   (title이 NULL 또는 공백이면 조건 생략)
     */
    private BooleanExpression titleContains(InquiryListSearchRequestDto inquiryListSearchRequestDto) {
        if (inquiryListSearchRequestDto == null || !StringUtils.hasText(inquiryListSearchRequestDto.getTitle())) {
            return null;
        }
        return inquiry.title.contains(inquiryListSearchRequestDto.getTitle());
    }

    /*
     * 비밀글 여부 필터 조건을 생성합니다.
     *
     * WHERE i.secret = true|false
     *   (secret이 NULL이면 조건 생략)
     */
    private BooleanExpression eqSecret(InquiryListSearchRequestDto inquiryListSearchRequestDto) {
        if (inquiryListSearchRequestDto == null || inquiryListSearchRequestDto.getSecret() == null) {
            return null;
        }
        Boolean secret = inquiryListSearchRequestDto.getSecret();
        return secret ? inquiry.secret.isTrue() : inquiry.secret.isFalse();
    }

    /*
     * 작성자 필터 조건을 생성합니다.
     *
     * WHERE i.user_id = ?
     *   (mineOnly가 TRUE가 아니면 조건 생략)
     */
    private BooleanExpression eqAuthorWhenMineOnly(Long currentUserId, InquiryListSearchRequestDto inquiryListSearchRequestDto) {
        if (inquiryListSearchRequestDto == null || !Boolean.TRUE.equals(inquiryListSearchRequestDto.getMineOnly())) {
            return null;
        }
        return inquiry.user.id.eq(currentUserId);
    }

    /*
     * 정렬 요청을 ORDER BY 절로 변환합니다.
     *
     * ORDER BY i.created_at ASC|DESC  (sort=createdAt)
     * ORDER BY i.id ASC               (그 외)
     */
    private List<OrderSpecifier<?>> resolveSortOrders(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (!sort.isSorted()) {
            return List.of(inquiry.id.asc());
        }
        for (Sort.Order order : sort) {
            InquirySortBy by = InquirySortBy.fromRequest(order.getProperty()).orElse(null);
            if (by == InquirySortBy.CREATED_AT) {
                return List.of(order.isAscending() ? inquiry.createdAt.asc() : inquiry.createdAt.desc());
            }
        }
        return List.of(inquiry.id.asc());
    }

    /*
     * 비밀글이면서 작성자가 아닌 조건을 생성합니다.
     *
     * i.secret = true AND i.user_id <> ?
     */
    private BooleanExpression isSecretNonOwner(Long viewerId) {
        return inquiry.secret.isTrue().and(inquiry.user.id.ne(viewerId));
    }

    /*
     * 비밀글 제목 마스킹 CASE 식을 생성합니다.
     *
     * CASE WHEN i.secret = true AND i.user_id <> ? THEN '비공개 글입니다.' ELSE i.title END
     */
    private Expression<String> resolveTitleExpression(Long viewerId) {
        return new CaseBuilder()
                .when(isSecretNonOwner(viewerId))
                .then(Expressions.constant(PRIVATE_INQUIRY_TITLE))
                .otherwise(inquiry.title);
    }

    /*
     * 답변 존재 여부 CASE 식을 생성합니다.
     *
     * CASE WHEN EXISTS (
     *   SELECT 1 FROM inquiry_reply ir WHERE ir.inquiry_id = i.id
     * ) THEN true ELSE false END
     */
    private Expression<Boolean> resolveAnsweredExpression() {
        return new CaseBuilder()
                .when(
                        JPAExpressions.selectOne()
                                .from(inquiryReply)
                                .where(inquiryReply.inquiry.eq(inquiry))
                                .exists()
                )
                .then(Expressions.constant(true))
                .otherwise(Expressions.constant(false));
    }
}