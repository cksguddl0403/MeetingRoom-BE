package kr.co.ta9.meetingroom.domain.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InquiryQueryDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String content;
    private InquiryAuthorQueryDto author;
    private boolean secret;
    private boolean answered;
    private LocalDateTime createdAt;
    private InquiryReplyQueryDto reply;
}
