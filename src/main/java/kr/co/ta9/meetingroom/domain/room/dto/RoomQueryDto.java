package kr.co.ta9.meetingroom.domain.room.dto;

import kr.co.ta9.meetingroom.domain.room.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomQueryDto {
    private Long id;
    private String name;
    private int maxCapacity;
    private Long companyId;
    private String status;
}
