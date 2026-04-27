package kr.co.ta9.meetingroom.domain.inspection.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class InspectionUpdateRequestDto {

    @NotBlank(message = "점검 이름을 입력해 주세요.")
    @Size(max = 50, message = "점검 이름은 최대 50자까지 입력할 수 있습니다.")
    private String name;

    @NotNull(message = "시작 일시를 입력해 주세요.")
    @FutureOrPresent(message = "시작 일시는 현재 이후여야 합니다.")
    private LocalDateTime startAt;

    @NotNull(message = "종료 일시를 입력해 주세요.")
    private LocalDateTime endAt;

    @NotNull(message = "회의실을 선택해 주세요.")
    private Long roomId;

    @Builder
    private InspectionUpdateRequestDto(
            String name,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long roomId
    ) {
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.roomId = roomId;
    }
}
