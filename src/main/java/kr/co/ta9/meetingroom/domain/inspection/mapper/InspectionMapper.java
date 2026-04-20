package kr.co.ta9.meetingroom.domain.inspection.mapper;

import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionListDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionQueryDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionRoomQueryDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionRoomDto;
import kr.co.ta9.meetingroom.domain.inspection.entity.Inspection;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InspectionMapper {

    default InspectionDto toDto(Inspection inspection) {
        if (inspection == null) {
            return null;
        }
        return InspectionDto.builder()
                .id(inspection.getId())
                .name(inspection.getName())
                .startAt(inspection.getStartAt())
                .endAt(inspection.getEndAt())
                .createdAt(inspection.getCreatedAt())
                .room(toRoomDto(inspection.getRoom()))
                .build();
    }

    default InspectionListDto toListDto(Inspection inspection) {
        if (inspection == null) {
            return null;
        }
        return InspectionListDto.builder()
                .id(inspection.getId())
                .name(inspection.getName())
                .startAt(inspection.getStartAt())
                .endAt(inspection.getEndAt())
                .createdAt(inspection.getCreatedAt())
                .room(toRoomDto(inspection.getRoom()))
                .build();
    }

    default InspectionDto toDto(InspectionQueryDto inspectionQueryDto) {
        if (inspectionQueryDto == null) {
            return null;
        }
        return InspectionDto.builder()
                .id(inspectionQueryDto.getId())
                .name(inspectionQueryDto.getName())
                .startAt(inspectionQueryDto.getStartAt())
                .endAt(inspectionQueryDto.getEndAt())
                .createdAt(inspectionQueryDto.getCreatedAt())
                .room(toRoomDto(inspectionQueryDto.getRoom()))
                .build();
    }

    default InspectionListDto toListDto(InspectionQueryDto inspectionQueryDto) {
        if (inspectionQueryDto == null) {
            return null;
        }
        return InspectionListDto.builder()
                .id(inspectionQueryDto.getId())
                .name(inspectionQueryDto.getName())
                .startAt(inspectionQueryDto.getStartAt())
                .endAt(inspectionQueryDto.getEndAt())
                .createdAt(inspectionQueryDto.getCreatedAt())
                .room(toRoomDto(inspectionQueryDto.getRoom()))
                .build();
    }

    default InspectionRoomDto toRoomDto(Room room) {
        if (room == null) {
            return null;
        }
        return InspectionRoomDto.builder()
                .id(room.getId())
                .name(room.getName())
                .build();
    }

    default InspectionRoomDto toRoomDto(InspectionRoomQueryDto inspectionRoomQueryDto) {
        if (inspectionRoomQueryDto == null) {
            return null;
        }
        return InspectionRoomDto.builder()
                .id(inspectionRoomQueryDto.getId())
                .name(inspectionRoomQueryDto.getName())
                .build();
    }
}
