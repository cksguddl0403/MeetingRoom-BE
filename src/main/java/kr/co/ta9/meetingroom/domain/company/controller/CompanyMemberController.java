package kr.co.ta9.meetingroom.domain.company.controller;

import kr.co.ta9.meetingroom.domain.company.dto.CompanyMemberListDto;
import kr.co.ta9.meetingroom.domain.company.service.CompanyService;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/members")
@RequiredArgsConstructor
public class CompanyMemberController {

    private final CompanyService companyService;

    // 회사 구성원 목록 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyMemberListDto>>> getAllCompanyMembers(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getAllCompanyMembers(companyId)));
    }
}
