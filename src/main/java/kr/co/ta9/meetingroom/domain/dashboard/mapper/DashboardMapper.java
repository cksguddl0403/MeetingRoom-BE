package kr.co.ta9.meetingroom.domain.dashboard.mapper;

import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardInspectionDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardInspectionQueryDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardQueryDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardReservationDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardReservationQueryDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardRoomDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DashboardMapper {

    public DashboardDto toDto(List<DashboardQueryDto> rows) {
        Map<Long, DashboardRoomDto> rooms = new LinkedHashMap<>();
        Map<Long, Set<Long>> reservationIdsByRoom = new LinkedHashMap<>();
        Map<Long, Set<Long>> inspectionIdsByRoom = new LinkedHashMap<>();

        for (DashboardQueryDto row : rows) {
            Long roomId = row.getRoom().getId();
            DashboardRoomDto room = rooms.computeIfAbsent(roomId, key ->
                    DashboardRoomDto.builder()
                            .id(row.getRoom().getId())
                            .name(row.getRoom().getName())
                            .maxCapacity(row.getRoom().getMaxCapacity() == null ? 0 : row.getRoom().getMaxCapacity())
                            .companyId(row.getRoom().getCompanyId())
                            .status(row.getRoom().getStatus())
                            .reservations(new ArrayList<>())
                            .inspections(new ArrayList<>())
                            .build()
            );

            Set<Long> reservationIds = reservationIdsByRoom.computeIfAbsent(roomId, key -> new HashSet<>());
            Set<Long> inspectionIds = inspectionIdsByRoom.computeIfAbsent(roomId, key -> new HashSet<>());

            DashboardReservationQueryDto reservation = row.getReservation();
            if (reservation != null && reservation.getId() != null && reservationIds.add(reservation.getId())) {
                room.getReservations().add(
                        DashboardReservationDto.builder()
                                .id(reservation.getId())
                                .title(reservation.getTitle())
                                .startAt(reservation.getStartAt())
                                .endAt(reservation.getEndAt())
                                .status(reservation.getStatus())
                                .build()
                );
            }

            DashboardInspectionQueryDto inspection = row.getInspection();
            if (inspection != null && inspection.getId() != null && inspectionIds.add(inspection.getId())) {
                room.getInspections().add(
                        DashboardInspectionDto.builder()
                                .id(inspection.getId())
                                .name(inspection.getName())
                                .startAt(inspection.getStartAt())
                                .endAt(inspection.getEndAt())
                                .createdAt(inspection.getCreatedAt())
                                .build()
                );
            }
        }

        return DashboardDto.builder()
                .rooms(rooms.values().stream().toList())
                .build();
    }
}
