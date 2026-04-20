package kr.co.ta9.meetingroom.domain.dashboard.dto;

import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationListSearchRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardSearchRequestDto {
    private ReservationListSearchRequestDto reservationListSearchRequestDto;
    private InspectionListSearchRequestDto inspectionListSearchRequestDto;

    @Builder
    private DashboardSearchRequestDto(ReservationListSearchRequestDto reservationListSearchRequestDto, InspectionListSearchRequestDto inspectionListSearchRequestDto) {
        this.reservationListSearchRequestDto = reservationListSearchRequestDto;
        this.inspectionListSearchRequestDto = inspectionListSearchRequestDto;
    }
}
