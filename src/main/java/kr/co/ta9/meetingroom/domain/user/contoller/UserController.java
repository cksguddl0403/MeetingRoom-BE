package kr.co.ta9.meetingroom.domain.user.contoller;

import jakarta.validation.Valid;
import kr.co.ta9.meetingroom.domain.user.dto.UserCreateRequestDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserProfileDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserProfileUpdateResponseDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserInfoUpdateResponseDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserUpdateRequestDto;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.dto.UserProfileUpdateRequestDto;
import kr.co.ta9.meetingroom.domain.user.service.UserService;
import kr.co.ta9.meetingroom.global.common.annotation.LoginUser;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @RequestPart("request") @Valid UserCreateRequestDto userCreateRequestDto,
            @RequestPart("employmentCertificateFile") MultipartFile employmentCertificateFile
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userService.join(userCreateRequestDto, employmentCertificateFile)));
    }

    // 사용자 정보 조회
    @GetMapping
    public ResponseEntity<ApiResponse<UserDto>> getUserInfo(@LoginUser User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserInfo(currentUser)));
    }

    // 사용자 정보 수정
    @PatchMapping
    public ResponseEntity<ApiResponse<UserInfoUpdateResponseDto>> updateUserInfo(@LoginUser User currentUser, @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserInfo(currentUser, userUpdateRequestDto)));
    }

    // 사용자 프로필 조회
    @GetMapping("/profiles")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@LoginUser User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(currentUser)));
    }

    // 사용자 프로필 수정
    @PatchMapping("/profiles")
    public ResponseEntity<ApiResponse<UserProfileUpdateResponseDto>> updateUserProfile(@LoginUser User currentUser,
                                                                                        @RequestPart("request") @Valid
                                                                                        UserProfileUpdateRequestDto userProfileUpdateRequestDto,
                                                                                        @RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserProfile(currentUser, userProfileUpdateRequestDto, profileImageFile)));
    }
}
