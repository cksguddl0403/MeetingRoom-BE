package kr.co.ta9.meetingroom.domain.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeListSearchRequestDto {

    private Long categoryId;

    private String title;

    @Builder
    private NoticeListSearchRequestDto(Long categoryId, String title) {
        this.categoryId = categoryId;
        this.title = title;
    }
}
