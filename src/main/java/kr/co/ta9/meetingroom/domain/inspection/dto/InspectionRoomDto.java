package kr.co.ta9.meetingroom.domain.inspection.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InspectionRoomDto {
    private Long id;
    private String name;

    @Builder
    private InspectionRoomDto(Long id, String name, LocalDateTime startAt, LocalDateTime endAt) {
        this.id = id;
        this.name = name;
    }
}
