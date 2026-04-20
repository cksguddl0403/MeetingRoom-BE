package kr.co.ta9.meetingroom.domain.room.dto;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoomSearchRequestDto {

    private Integer maxCapacity;

    @Valid
    private List<EquipmentSearchRequestDto> equipmentSearchRequestDtos = new ArrayList<>();

    @Builder
    private RoomSearchRequestDto(Integer maxCapacity, List<EquipmentSearchRequestDto> equipmentSearchRequestDtos) {
        this.maxCapacity = maxCapacity;
        this.equipmentSearchRequestDtos = equipmentSearchRequestDtos != null ? equipmentSearchRequestDtos : new ArrayList<>();
    }
}
