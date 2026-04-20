package kr.co.ta9.meetingroom.domain.auth.controller;

import jakarta.validation.Valid;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationSendRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationSendDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationVerifyRequestDto;
import kr.co.ta9.meetingroom.domain.auth.dto.EmailVerificationVerifyDto;
import kr.co.ta9.meetingroom.domain.auth.service.EmailVerificationService;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email-verification")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    // 이메일 인증 코드 발송
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<EmailVerificationSendDto>> send(@RequestBody @Valid EmailVerificationSendRequestDto emailVerificationSendRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(emailVerificationService.sendCode(emailVerificationSendRequestDto)));
    }

    // 이메일 인증 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<EmailVerificationVerifyDto>> verify(@RequestBody @Valid EmailVerificationVerifyRequestDto emailVerificationVerifyRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(emailVerificationService.verifyCode(emailVerificationVerifyRequestDto)));
    }
}

