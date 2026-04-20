package kr.co.ta9.meetingroom.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InquiryImageDto {
    private Long id;
    private String url;

    @Builder
    private InquiryImageDto(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
