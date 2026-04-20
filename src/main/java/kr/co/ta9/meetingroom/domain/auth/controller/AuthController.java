package kr.co.ta9.meetingroom.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.ta9.meetingroom.domain.auth.dto.AvailabilityResponseDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.LoginIdAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.NicknameAvailabilityRequestDto;
import kr.co.ta9.meetingroom.domain.auth.service.AuthService;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 엑세스 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        authService.refreshAccessToken(request, response);
        return ResponseEntity.noContent().build();
    }

    // 로그아웃
    @PostMapping("/sign-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    // 아이디 중복 체크
    @PostMapping("/login-id/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> checkLoginIdAvailability(
            @RequestBody @Valid LoginIdAvailabilityRequestDto loginIdAvailabilityRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.checkLoginIdAvailability(loginIdAvailabilityRequestDto)));
    }

    // 닉네임 중복 체크
    @PostMapping("/nickname/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> checkNicknameAvailability(
            @RequestBody @Valid NicknameAvailabilityRequestDto nicknameAvailabilityRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.checkNicknameAvailability(nicknameAvailabilityRequestDto)));
    }

    // 이메일 중복 체크
    @PostMapping("/email/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> checkEmailAvailability(
            @RequestBody @Valid EmailAvailabilityRequestDto emailAvailabilityRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.checkEmailAvailability(emailAvailabilityRequestDto)));
    }
}