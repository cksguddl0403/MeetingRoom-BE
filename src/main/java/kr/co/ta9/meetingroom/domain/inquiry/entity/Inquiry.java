package kr.co.ta9.meetingroom.domain.inquiry.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.category.entity.Category;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, name = "is_secret", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean secret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category inquiryCategory;

    @Builder(access = AccessLevel.PRIVATE)
    private Inquiry(
            User user,
            Category inquiryCategory,
            String title,
            String content,
            boolean secret
    ) {
        this.user = user;
        this.inquiryCategory = inquiryCategory;
        this.title = title;
        this.content = content;
        this.secret = secret;
    }

    public static Inquiry createInquiry(
            User user,
            Category inquiryCategory,
            String title,
            String content,
            boolean secret
    ) {

        return Inquiry.builder()
                .user(user)
                .inquiryCategory(inquiryCategory)
                .title(title)
                .content(content)
                .secret(secret)
                .build();
    }

    public void update(String title, String content, boolean secret) {
        this.title = title;
        this.content = content;
        this.secret = secret;
    }
}
