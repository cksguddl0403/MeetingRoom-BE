package kr.co.ta9.meetingroom.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import kr.co.ta9.meetingroom.domain.reservation.enums.TimePeriod;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ReservationListSearchRequestDto {

    private ReservationStatus status;

    private LocalDate fromDate;

    private LocalDate toDate;

    private TimePeriod timePeriod;

    private Boolean participatedOnly;

    private Boolean applicantOnly;

    @Builder
    private ReservationListSearchRequestDto(
            ReservationStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            TimePeriod timePeriod,
            Boolean participatedOnly,
            Boolean applicantOnly
    ) {
        this.status = status;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.timePeriod = timePeriod;
        this.participatedOnly = participatedOnly;
        this.applicantOnly = applicantOnly;
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
