package kr.co.ta9.meetingroom.global.common.pageable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/*
 * Pageable 정렬 속성을 도메인 enum으로 허용 목록 검증할 때 공통 처리.
 */
public final class PageableSortValidator {

    private PageableSortValidator() {
    }

    /*
     * Pageable#getSort() 의 모든 Sort.Order 가 fromRequest로 매핑될 때만 통과.
     */
    public static <E extends Enum<E>> void validateAllOrders(
            Pageable pageable,
            Function<String, Optional<E>> fromRequest,
            Supplier<? extends RuntimeException> onUnknownProperty
    ) {
        Sort sort = pageable.getSort();
        if (!sort.isSorted()) {
            return;
        }
        for (Sort.Order order : sort) {
            fromRequest.apply(order.getProperty())
                    .orElseThrow(onUnknownProperty);
        }
    }
}
