package kr.co.ta9.meetingroom.domain.room.mapper;

import kr.co.ta9.meetingroom.domain.room.dto.*;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.domain.room.enums.RoomStatus;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    default RoomDto toDto(RoomQueryDto roomQueryDto) {
        return toDto(roomQueryDto, List.of());
    }

    default RoomDto toDto(Room room, String roomStatus, List<RoomEquipmentDto> equipments) {
        if (room == null) {
            return null;
        }
        return RoomDto.builder()
                .id(room.getId())
                .name(room.getName())
                .maxCapacity(room.getMaxCapacity())
                .companyId(room.getCompany() == null ? null : room.getCompany().getId())
                .status(roomStatus)
                .equipments(equipments == null ? List.of() : equipments)
                .build();
    }

    default RoomDto toDto(RoomQueryDto roomQueryDto, List<RoomEquipmentQueryDto> equipmentQueryDtos) {
        if (roomQueryDto == null) {
            return null;
        }
        return RoomDto.builder()
                .id(roomQueryDto.getId())
                .name(roomQueryDto.getName())
                .maxCapacity(roomQueryDto.getMaxCapacity())
                .companyId(roomQueryDto.getCompanyId())
                .status(roomQueryDto.getStatus())
                .equipments(equipmentQueryDtos == null || equipmentQueryDtos.isEmpty()
                        ? List.of()
                        : equipmentQueryDtos.stream()
                        .filter(Objects::nonNull)
                        .map(dto -> RoomEquipmentDto.builder()
                                .roomId(dto.getRoomId())
                                .equipmentId(dto.getEquipmentId())
                                .name(dto.getName())
                                .quantity(dto.getQuantity())
                                .status(dto.getStatus() == null ? null : dto.getStatus().getName())
                                .build())
                        .toList())
                .build();
    }

    default RoomListDto toListDto(RoomQueryDto roomQueryDto, List<RoomEquipmentQueryDto> equipmentQueryDtos) {
        if (roomQueryDto == null) {
            return null;
        }
        return RoomListDto.builder()
                .id(roomQueryDto.getId())
                .name(roomQueryDto.getName())
                .maxCapacity(roomQueryDto.getMaxCapacity())
                .companyId(roomQueryDto.getCompanyId())
                .status(roomQueryDto.getStatus())
                .equipments(equipmentQueryDtos == null || equipmentQueryDtos.isEmpty()
                        ? List.of()
                        : equipmentQueryDtos.stream()
                        .filter(Objects::nonNull)
                        .map(dto -> RoomEquipmentDto.builder()
                                .roomId(dto.getRoomId())
                                .equipmentId(dto.getEquipmentId())
                                .name(dto.getName())
                                .quantity(dto.getQuantity())
                                .status(dto.getStatus() == null ? null : dto.getStatus().getName())
                                .build())
                        .toList())
                .build();
    }
}
