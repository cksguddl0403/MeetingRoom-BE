package kr.co.ta9.meetingroom.domain.equipment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomEquipmentStatus {
    AVAILABLE("정상"), // 정상
    BROKEN("고장"), // 고장
    LOST("분실"), // 분실
    IN_REPAIR("수리 중"); // 수리 중

    private final String name;
}
