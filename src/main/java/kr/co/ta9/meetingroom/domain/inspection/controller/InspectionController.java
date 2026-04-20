package kr.co.ta9.meetingroom.domain.inspection.controller;

import jakarta.validation.Valid;
import kr.co.ta9.meetingroom.domain.inspection.dto.*;
import kr.co.ta9.meetingroom.domain.inspection.service.InspectionService;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.annotation.LoginUser;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/{companyId}/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    // 점검 등록
    @PostMapping
    public ResponseEntity<ApiResponse<InspectionDto>> createInspection(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @Valid @RequestBody InspectionCreateRequestDto inspectionCreateRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.createInspection(currentUser, companyId, inspectionCreateRequestDto)));
    }

    // 점검 수정
    @PatchMapping("/{inspectionId}")
    public ResponseEntity<ApiResponse<InspectionDto>> updateInspection(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionUpdateRequestDto inspectionUpdateRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.updateInspection(currentUser, companyId, inspectionId, inspectionUpdateRequestDto)));
    }

    // 점검 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<OffsetPageResponseDto<InspectionListDto>>> getInspections(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Valid @ModelAttribute InspectionListSearchRequestDto inspectionListDto

    ) {
        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.getInspections(currentUser, companyId, pageable, inspectionListDto)));
    }

    // 점검 삭제
    @DeleteMapping("/{inspectionId}")
    public ResponseEntity<Void> deleteInspection(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @PathVariable Long inspectionId
    ) {
        inspectionService.deleteInspection(currentUser, companyId, inspectionId);
        return ResponseEntity.noContent().build();
    }
}
