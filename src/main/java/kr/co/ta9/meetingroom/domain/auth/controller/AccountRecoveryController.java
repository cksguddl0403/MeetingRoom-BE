package kr.co.ta9.meetingroom.domain.auth.controller;

import jakarta.validation.Valid;
import kr.co.ta9.meetingroom.domain.auth.dto.AvailabilityResponseDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindLoginIdAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindLoginIdRevealRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindLoginIdRevealResponseDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindPasswordAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.FindPasswordResetRequestDto;
import kr.co.ta9.meetingroom.domain.auth.service.AccountRecoveryService;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/account-recovery")
@RequiredArgsConstructor
public class AccountRecoveryController {
    private final AccountRecoveryService accountRecoveryService;

    // 아이디 찾기 가능 여부
    @PostMapping("/login-id/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> checkFindLoginIdAvailability(
            @RequestBody @Valid FindLoginIdAvailabilityRequestDto findLoginIdAvailabilityRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(accountRecoveryService.checkFindLoginIdAvailability(findLoginIdAvailabilityRequestDto)));
    }

    // 아이디 찾기
    @PostMapping("/login-id/find")
    public ResponseEntity<ApiResponse<FindLoginIdRevealResponseDto>> revealLoginId(
            @RequestBody @Valid FindLoginIdRevealRequestDto findLoginIdRevealRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(accountRecoveryService.findLoginId(findLoginIdRevealRequestDto)));
    }

    // 비밀번호 찾기 가능 여부
    @PostMapping("/password/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> checkFindPasswordAvailability(
            @RequestBody @Valid FindPasswordAvailabilityRequestDto findPasswordAvailabilityRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(accountRecoveryService.checkFindPasswordAvailability(findPasswordAvailabilityRequestDto)));
    }

    // 비밀번호 초기화 요청
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid FindPasswordResetRequestDto findPasswordResetRequestDto) {
        accountRecoveryService.resetPassword(findPasswordResetRequestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
