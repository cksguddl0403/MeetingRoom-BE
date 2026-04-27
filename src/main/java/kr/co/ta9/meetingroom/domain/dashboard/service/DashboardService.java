package kr.co.ta9.meetingroom.domain.dashboard.service;
import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardDto;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardQueryDto;
import kr.co.ta9.meetingroom.domain.dashboard.mapper.DashboardMapper;
import kr.co.ta9.meetingroom.domain.dashboard.repository.DashboardRepository;
import kr.co.ta9.meetingroom.domain.equipment.exception.EquipmentException;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.error.code.EquipmentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final CompanyMemberRepository  companyMemberRepository;
    private final DashboardMapper dashboardMapper;

    // 대시보드 조회
    public DashboardDto getDashboard(User currentUser, Long companyId) {
        validateCurrentUserBelongsToCompany(currentUser, companyId);

        List<DashboardQueryDto> rows = dashboardRepository.getDashboard(companyId, LocalDateTime.now());
        return dashboardMapper.toDto(rows);
    }

    // 현재 사용자 회사 소속 확인
    private void validateCurrentUserBelongsToCompany(User currentUser, Long companyId) {
        Optional<CompanyMember> companyMember = companyMemberRepository.findByUser_IdAndCompany_Id(currentUser.getId(), companyId);

        if (companyMember.isEmpty()) {
            throw new EquipmentException(EquipmentErrorCode.EQUIPMENT_NOT_AUTHORIZED);
        }
    }
}
