package kr.co.ta9.meetingroom.domain.company.controller;

import kr.co.ta9.meetingroom.domain.company.dto.CompanyDto;
import kr.co.ta9.meetingroom.domain.company.dto.CompanyListDto;
import kr.co.ta9.meetingroom.domain.company.service.CompanyService;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.annotation.LoginUser;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // 회사 상세 조회
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getCompany(companyId)));
    }
}
