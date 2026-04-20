package kr.co.ta9.meetingroom.domain.room.dto;

import jakarta.validation.constraints.AssertTrue;
import kr.co.ta9.meetingroom.domain.equipment.enums.RoomEquipmentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EquipmentSearchRequestDto {

    private Long equipmentId;

    private Integer minQuantity;

    private RoomEquipmentStatus status;

    @Builder
    private EquipmentSearchRequestDto(Long equipmentId, Integer minQuantity, RoomEquipmentStatus status) {
        this.equipmentId = equipmentId;
        this.minQuantity = minQuantity;
        this.status = status;
    }

    /*
     * 비품 ID 없이 최소 수량·상태만 있으면 실패.
     * 비품 ID가 있을 때 최소 수량이 1 미만이면 실패.
     * 비품 ID·수량·상태 모두 없으면 성공(바인딩 빈 슬롯 무시).
     */
    @AssertTrue(message = "비품 조건에 비품 ID가 없거나 최소 수량이 1 미만입니다.")
    public boolean isEquipmentConditionConsistent() {
        boolean hasOptional = minQuantity != null || status != null;
        if (equipmentId == null) {
            return !hasOptional;
        }
        if (minQuantity != null && minQuantity < 1) {
            return false;
        }
        return true;
    }
}
