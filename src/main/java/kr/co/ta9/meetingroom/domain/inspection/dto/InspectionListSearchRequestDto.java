package kr.co.ta9.meetingroom.domain.inspection.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class InspectionListSearchRequestDto {
    private Long roomId;
    @Size(max = 40, message = "이름은 최대 40자까지 입력할 수 있습니다.")
    private String name;
    private LocalDate fromDate;
    private LocalDate toDate;

    @Builder
    private InspectionListSearchRequestDto(Long roomId, String name, LocalDate fromDate, LocalDate toDate) {
        this.roomId = roomId;
        this.name = name;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @JsonIgnore
    @AssertTrue(message = "시작일은 종료일보다 늦을 수 없습니다.")
    public boolean isDateRangeValid() {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return !fromDate.isAfter(toDate);
    }
}
