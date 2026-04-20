package kr.co.ta9.meetingroom.domain.room.dto;

import kr.co.ta9.meetingroom.domain.equipment.enums.RoomEquipmentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RoomEquipmentDto {
    private Long roomId;
    private Long equipmentId;
    private String name;
    private int quantity;
    private String status;

    @Builder
    private RoomEquipmentDto(Long roomId, Long equipmentId, String name, int quantity, String status) {
        this.roomId = roomId;
        this.equipmentId = equipmentId;
        this.name = name;
        this.quantity = quantity;
        this.status = status;
    }
}
