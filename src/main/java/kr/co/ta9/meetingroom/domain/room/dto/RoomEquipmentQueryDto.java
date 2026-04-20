package kr.co.ta9.meetingroom.domain.room.dto;

import kr.co.ta9.meetingroom.domain.equipment.enums.RoomEquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomEquipmentQueryDto {
    private Long roomId;
    private Long equipmentId;
    private String name;
    private int quantity;
    private RoomEquipmentStatus status;
}
