package kr.co.ta9.meetingroom.domain.room.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.co.ta9.meetingroom.domain.equipment.dto.RoomEquipmentItemDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RoomUpdateRequestDto {

    @Size(max = 30, message = "회의실 이름은 최대 30자까지 입력할 수 있습니다.")
    private String name;

    @Positive(message = "최대 수용 인원 수는 1 이상이어야 합니다.")
    private Integer maxCapacity;

    @Valid
    private List<RoomEquipmentItemDto> items = new ArrayList<>();

    @Builder
    private RoomUpdateRequestDto(String name, Integer maxCapacity, List<RoomEquipmentItemDto> items) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.items = items;
    }
}
