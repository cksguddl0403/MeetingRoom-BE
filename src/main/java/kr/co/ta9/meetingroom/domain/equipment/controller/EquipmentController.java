package kr.co.ta9.meetingroom.domain.equipment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentCreateRequestDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentListDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentUpdateRequestDto;
import kr.co.ta9.meetingroom.domain.equipment.service.EquipmentService;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.annotation.LoginUser;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company/{companyId}/equipments")
@RequiredArgsConstructor
@Validated
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 비품 등록
    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentDto>> createEquipment(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @Valid @RequestBody EquipmentCreateRequestDto equipmentCreateRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                equipmentService.createEquipment(currentUser, companyId, equipmentCreateRequestDto)));
    }

    // 비품 수정
    @PatchMapping("/{equipmentId}")
    public ResponseEntity<ApiResponse<EquipmentDto>> updateEquipment(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @PathVariable Long equipmentId,
            @Valid @RequestBody EquipmentUpdateRequestDto equipmentUpdateRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                equipmentService.updateEquipment(currentUser, companyId, equipmentId, equipmentUpdateRequestDto)));
    }

    // 비품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<OffsetPageResponseDto<EquipmentListDto>>> getEquipments(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @RequestParam(required = false) @Size(max = 20) String name,
            @PageableDefault(size = 10, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                equipmentService.getEquipments(currentUser, companyId, name, pageable)));
    }

    // 비품 전체 목록 조회
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EquipmentListDto>>> getAllEquipments(
            @LoginUser User currentUser,
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                equipmentService.getAllEquipments(currentUser, companyId)));
    }

    // 비품 삭제
    @DeleteMapping("/{equipmentId}")
    public ResponseEntity<Void> deleteEquipment(
            @LoginUser User currentUser,
            @PathVariable Long companyId,
            @PathVariable Long equipmentId
    ) {
        equipmentService.deleteEquipment(currentUser, companyId, equipmentId);
        return ResponseEntity.noContent().build();
    }
}
