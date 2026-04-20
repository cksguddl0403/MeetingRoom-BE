package kr.co.ta9.meetingroom.domain.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EquipmentQueryDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
