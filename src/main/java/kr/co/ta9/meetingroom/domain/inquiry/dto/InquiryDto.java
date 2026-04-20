package kr.co.ta9.meetingroom.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InquiryDto {
    private Long id;

    private InquiryCategoryDto category;

    private String title;

    private String content;

    private InquiryAuthorDto author;

    private boolean answered;

    private boolean secret;

    private LocalDateTime createdAt;

    private InquiryReplyDto reply;

    private List<InquiryImageDto> images;

    @Builder
    private InquiryDto(Long id, InquiryCategoryDto category, String title, String content, InquiryAuthorDto author, boolean answered, boolean secret, LocalDateTime createdAt, InquiryReplyDto reply, List<InquiryImageDto> images) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.content = content;
        this.author = author;
        this.answered = answered;
        this.secret = secret;
        this.createdAt = createdAt;
        this.reply = reply;
        this.images = images;
    }
}
