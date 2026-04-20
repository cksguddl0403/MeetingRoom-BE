package kr.co.ta9.meetingroom.domain.equipment.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.equipment.enums.RoomEquipmentStatus;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_room_equipment_equipment_room",
                columnNames = {"equipment_id", "room_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomEquipment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomEquipmentStatus status = RoomEquipmentStatus.AVAILABLE;

    @Builder(access = AccessLevel.PRIVATE)
    private RoomEquipment(Equipment equipment, Room room, int quantity, RoomEquipmentStatus status) {
        this.equipment = equipment;
        this.room = room;
        this.quantity = quantity;
        this.status = status;
    }

    public static RoomEquipment createRoomEquipment(Equipment equipment, Room room, int quantity, RoomEquipmentStatus status) {
        return RoomEquipment.builder()
                .equipment(equipment)
                .room(room)
                .quantity(quantity)
                .status(status != null ? status : RoomEquipmentStatus.AVAILABLE)
                .build();
    }
}
