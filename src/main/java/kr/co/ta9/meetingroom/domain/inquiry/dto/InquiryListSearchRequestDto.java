package kr.co.ta9.meetingroom.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InquiryListSearchRequestDto {

    private Long categoryId;

    private String title;

    private Boolean secret;

    private Boolean mineOnly;

    @Builder
    private InquiryListSearchRequestDto(Long categoryId, String title, Boolean secret, Boolean mineOnly) {
        this.categoryId = categoryId;
        this.title = title;
        this.secret = secret;
        this.mineOnly = mineOnly;
    }
}
