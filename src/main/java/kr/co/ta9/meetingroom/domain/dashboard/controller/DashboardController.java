package kr.co.ta9.meetingroom.domain.dashboard.controller;

import jakarta.validation.Valid;
import kr.co.ta9.meetingroom.domain.dashboard.dto.DashboardDto;
import kr.co.ta9.meetingroom.domain.dashboard.service.DashboardService;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.annotation.LoginUser;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/{companyId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    // 대시보드 조회
    @GetMapping
    public ApiResponse<DashboardDto> getDashboard(
            @LoginUser User currentUser,
            @PathVariable Long companyId
    ) {
        return ApiResponse.success(dashboardService.getDashboard(currentUser, companyId));
    }
}
