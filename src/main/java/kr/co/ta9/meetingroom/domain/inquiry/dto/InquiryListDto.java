package kr.co.ta9.meetingroom.domain.inquiry.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryListDto {
    private Long id;

    private InquiryCategoryDto category;

    private String title;

    private InquiryAuthorDto author;

    private boolean answered;

    private boolean secret;

    private LocalDateTime createdAt;

    @Builder
    private InquiryListDto(Long id, InquiryCategoryDto category, String title, InquiryAuthorDto author, boolean answered, boolean secret, LocalDateTime createdAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.author = author;
        this.answered = answered;
        this.secret = secret;
        this.createdAt = createdAt;
    }
}
