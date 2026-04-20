package kr.co.ta9.meetingroom.domain.room.dto;

import kr.co.ta9.meetingroom.domain.room.enums.RoomStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class RoomDto {
    private Long id;
    private String name;
    private int maxCapacity;
    private Long companyId;
    private String status;
    private List<RoomEquipmentDto> equipments;

    @Builder
    private RoomDto(
            Long id,
            String name,
            int maxCapacity,
            Long companyId,
            String status,
            List<RoomEquipmentDto> equipments
    ) {
        this.id = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.companyId = companyId;
        this.status = status;
        this.equipments = equipments;
    }
}
