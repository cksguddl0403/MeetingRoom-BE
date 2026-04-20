package kr.co.ta9.meetingroom.domain.equipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EquipmentDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;

    @Builder
    private EquipmentDto(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }
}
