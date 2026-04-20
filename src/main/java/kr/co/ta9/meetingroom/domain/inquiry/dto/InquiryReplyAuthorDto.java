package kr.co.ta9.meetingroom.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InquiryReplyAuthorDto {
    private Long id;
    private String nickname;

    @Builder
    private InquiryReplyAuthorDto(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}
