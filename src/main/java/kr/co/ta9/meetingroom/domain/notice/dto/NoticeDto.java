package kr.co.ta9.meetingroom.domain.notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeDto {

    private Long id;

    private NoticeCategoryDto category;

    private String title;

    private String content;

    private NoticeAuthorDto author;

    private int viewCount;

    private LocalDateTime createdAt;

    @Builder
    private NoticeDto(Long id, NoticeCategoryDto category, String title, String content, NoticeAuthorDto author, int viewCount, LocalDateTime createdAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.content = content;
        this.author = author;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }
}
