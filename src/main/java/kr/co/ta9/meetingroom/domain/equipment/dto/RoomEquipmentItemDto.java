package kr.co.ta9.meetingroom.domain.equipment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.co.ta9.meetingroom.domain.equipment.enums.RoomEquipmentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomEquipmentItemDto {

    @NotNull(message = "비품을 선택해 주세요.")
    private Long equipmentId;

    @NotNull(message = "수량을 입력해 주세요.")
    @Positive(message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @NotNull(message = "비품 상태를 선택해 주세요.")
    private RoomEquipmentStatus status;

    @Builder
    private RoomEquipmentItemDto(Long equipmentId, Integer quantity, RoomEquipmentStatus status) {
        this.equipmentId = equipmentId;
        this.quantity = quantity;
        this.status = status;
    }
}
