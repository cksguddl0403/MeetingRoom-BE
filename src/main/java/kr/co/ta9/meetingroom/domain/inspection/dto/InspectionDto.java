package kr.co.ta9.meetingroom.domain.inspection.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InspectionDto {
    private Long id;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private InspectionRoomDto room;

    @Builder
    private InspectionDto(Long id, String name, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt, InspectionRoomDto room) {
        this.id = id;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdAt = createdAt;
        this.room = room;
    }
}