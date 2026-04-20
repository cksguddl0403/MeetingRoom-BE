package kr.co.ta9.meetingroom.domain.notice.repository;

import kr.co.ta9.meetingroom.domain.notice.dto.NoticeListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

    Page<Notice> getNotices(NoticeListSearchRequestDto noticeListSearchRequestDto, Pageable pageable);
}
