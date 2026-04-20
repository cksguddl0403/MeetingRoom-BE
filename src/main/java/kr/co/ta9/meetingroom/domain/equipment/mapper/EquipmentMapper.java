package kr.co.ta9.meetingroom.domain.equipment.mapper;

import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentListDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentQueryDto;
import kr.co.ta9.meetingroom.domain.equipment.entity.Equipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {
    default EquipmentDto toDto(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        return EquipmentDto.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .createdAt(equipment.getCreatedAt())
                .build();
    }

    default EquipmentDto toDto(EquipmentQueryDto equipmentQueryDto) {
        if (equipmentQueryDto == null) {
            return null;
        }
        return EquipmentDto.builder()
                .id(equipmentQueryDto.getId())
                .name(equipmentQueryDto.getName())
                .createdAt(equipmentQueryDto.getCreatedAt())
                .build();
    }

    default EquipmentListDto toListDto(EquipmentQueryDto equipmentQueryDto) {
        if (equipmentQueryDto == null) {
            return null;
        }
        return EquipmentListDto.builder()
                .id(equipmentQueryDto.getId())
                .name(equipmentQueryDto.getName())
                .createdAt(equipmentQueryDto.getCreatedAt())
                .build();
    }
}

