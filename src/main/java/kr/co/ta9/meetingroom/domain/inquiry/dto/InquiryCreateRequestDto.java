package kr.co.ta9.meetingroom.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryCreateRequestDto {

    @NotNull
    private Long inquiryCategoryId;

    @NotBlank
    @Size(max = 20, message = "제목은 공백 포함 최대 20자까지 입력할 수 있습니다.")
    private String title;

    @NotBlank
    private String content;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isPrivate;

    @Builder
    private InquiryCreateRequestDto(
            Long inquiryCategoryId,
            String title,
            String content,
            Boolean isPrivate
    ) {
        this.inquiryCategoryId = inquiryCategoryId;
        this.title = title;
        this.content = content;
        this.isPrivate = isPrivate;
    }
}
