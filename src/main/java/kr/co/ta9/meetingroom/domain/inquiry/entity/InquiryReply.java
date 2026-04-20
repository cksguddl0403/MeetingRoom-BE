package kr.co.ta9.meetingroom.domain.inquiry.entity;


import jakarta.persistence.*;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryReply extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, unique = true)
    private Inquiry inquiry;

    @Builder(access = AccessLevel.PRIVATE)
    private InquiryReply(String content) {
        this.content = content;
    }

    public static InquiryReply createInquiryReply(String content) {
        return InquiryReply.builder()
                .content(content)
                .build();
    }
}
