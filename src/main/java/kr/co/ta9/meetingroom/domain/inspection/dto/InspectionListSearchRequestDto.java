package kr.co.ta9.meetingroom.domain.inspection.dto;

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
}
