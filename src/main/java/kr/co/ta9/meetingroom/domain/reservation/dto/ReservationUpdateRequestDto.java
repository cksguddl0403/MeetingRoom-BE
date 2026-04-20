package kr.co.ta9.meetingroom.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ReservationUpdateRequestDto {

    @NotBlank
    @Size(max = 50, message = "제목은 공백 포함 최대 50자까지 입력할 수 있습니다.")
    private String title;

    @NotNull
    private Long roomId;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private List<Long> participantUserIds = new ArrayList<>();

    @Builder
    private ReservationUpdateRequestDto(
            String title,
            Long roomId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            List<Long> participantUserIds
    ) {
        this.title = title;
        this.roomId = roomId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.participantUserIds = participantUserIds != null ? participantUserIds : new ArrayList<>();
    }

    @JsonIgnore
    @AssertTrue(message = "종료 일시는 시작 일시보다 이후여야 합니다.")
    public boolean isEndAfterStart() {
        if (startAt == null || endAt == null) {
            return true;
        }
        return endAt.isAfter(startAt);
    }
}
