package kr.co.ta9.meetingroom.domain.notice.controller;

import kr.co.ta9.meetingroom.domain.notice.dto.NoticeDto;
import kr.co.ta9.meetingroom.domain.notice.dto.NoticeListDto;
import kr.co.ta9.meetingroom.domain.notice.dto.NoticeListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.notice.service.NoticeService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;


    // 공지사항 상세 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDto>> getNotice(@LoginUser User currentUser, @PathVariable Long noticeId) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotice(currentUser, noticeId)));
    }

    // 공지사항 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<OffsetPageResponseDto<NoticeListDto>>> getNotices(
            @LoginUser User currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            NoticeListSearchRequestDto noticeListSearchRequestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotices(currentUser, pageable, noticeListSearchRequestDto)));
    }
}
