package kr.co.ta9.meetingroom.domain.dashboard.repository;

import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardQueryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository {
    List<DashboardQueryDto> getDashboard(Long companyId, LocalDateTime at);
}
