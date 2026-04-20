package kr.co.ta9.meetingroom.domain.notice.service;

import kr.co.ta9.meetingroom.domain.notice.dto.NoticeDto;
import kr.co.ta9.meetingroom.domain.notice.dto.NoticeListDto;
import kr.co.ta9.meetingroom.domain.notice.dto.NoticeListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.notice.entity.Notice;
import kr.co.ta9.meetingroom.domain.notice.exception.NoticeException;
import kr.co.ta9.meetingroom.domain.notice.mapper.NoticeMapper;
import kr.co.ta9.meetingroom.domain.notice.repository.NoticeRepository;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.NoticeErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeMapper noticeMapper;

    // 공지사항 상세 조회
    @Transactional
    public NoticeDto getNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdWithCategory(noticeId)
                .orElseThrow(() -> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));

        notice.increaseViewCount();
        return noticeMapper.toDto(notice);
    }

    // 공지사항 목록 조회
    public OffsetPageResponseDto<NoticeListDto> getNotices(Pageable pageable, NoticeListSearchRequestDto noticeListSearchRequestDto) {
        Page<Notice> page = noticeRepository.getNotices(noticeListSearchRequestDto, pageable);

        List<NoticeListDto> items = page.getContent().stream()
                .map(noticeMapper::toListDto)
                .toList();

        return OffsetPageResponseDto.<NoticeListDto>builder()
                .totalCount(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .content(items)
                .build();
    }
}
