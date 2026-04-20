package kr.co.ta9.meetingroom.global.common.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class OffsetPageResponseDto<T> {
    private long totalCount;
    private int page;
    private int size;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasPrevious;
    private boolean hasNext;
    private List<T> content;

    @Builder
    private OffsetPageResponseDto(long totalCount, int page, int size, int totalPages, boolean first, boolean last, boolean hasPrevious, boolean hasNext, List<T> content) {
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
        this.content = content;
    }
}
