package kr.co.ta9.meetingroom.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
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

    @NotNull(message = "회의실 ID는 필수 입력값입니다.")
    private Long roomId;

    @NotNull(message = "시작 일시는 필수 입력값입니다.")
    @FutureOrPresent(message = "시작 일시는 현재 이후여야 합니다.")
    private LocalDateTime startAt;

    @NotNull(message = "종료 일시는 필수 입력값입니다.")
    private LocalDateTime endAt;

    private List<Long> participantCompanyMemberIds = new ArrayList<>();

    @Builder
    private ReservationUpdateRequestDto(
            String title,
            Long roomId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            List<Long> participantCompanyMemberIds
    ) {
        this.title = title;
        this.roomId = roomId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.participantCompanyMemberIds = participantCompanyMemberIds != null ? participantCompanyMemberIds : new ArrayList<>();
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
