package kr.co.ta9.meetingroom.domain.dashboard.service;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardSearchRequestDto;
import kr.co.ta9.meetingroom.domain.dashboard.mapper.DashboardMapper;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionQueryDto;
import kr.co.ta9.meetingroom.domain.inspection.repository.InspectionRepository;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.repository.ReservationRepository;
import kr.co.ta9.meetingroom.domain.room.dto.RoomQueryDto;
import kr.co.ta9.meetingroom.domain.room.repository.RoomRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final InspectionRepository inspectionRepository;
    private final CompanyMemberRepository  companyMemberRepository;
    private final DashboardMapper dashboardMapper;

    public DashboardDto getDashboard(User currentUser, Long companyId, DashboardSearchRequestDto dashboardSearchRequestDto) {
        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ACCESS_DENIED);
        }

        List<RoomQueryDto> roomQueryDtos = roomRepository.getAllRooms(companyId, LocalDateTime.now());

        List<Long> roomIds = roomQueryDtos.stream().map(RoomQueryDto::getId).toList();

        List<ReservationQueryDto> reservationQueryDtos = reservationRepository.getReservations(currentUser.getId(), companyId, roomIds, dashboardSearchRequestDto.getReservationListSearchRequestDto());

        List<InspectionQueryDto> inspections = inspectionRepository.getAllInspections(companyId, dashboardSearchRequestDto.getInspectionListSearchRequestDto());

        return dashboardMapper.toDto(roomQueryDtos, reservationQueryDtos, inspections);

    }
}
