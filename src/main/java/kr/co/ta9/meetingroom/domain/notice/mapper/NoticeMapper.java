package kr.co.ta9.meetingroom.domain.notice.mapper;

import kr.co.ta9.meetingroom.domain.notice.dto.NoticeDto;
import kr.co.ta9.meetingroom.domain.notice.dto.NoticeListDto;
import kr.co.ta9.meetingroom.domain.notice.dto.NoticeCategoryDto;
import kr.co.ta9.meetingroom.domain.notice.entity.Notice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoticeMapper {

    default NoticeDto toDto(Notice notice) {
        if (notice == null) {
            return null;
        }
        return NoticeDto.builder()
                .id(notice.getId())
                .category(NoticeCategoryDto.builder()
                        .id(notice.getNoticeCategory().getId())
                        .name(notice.getNoticeCategory().getName())
                        .build())
                .title(notice.getTitle())
                .content(notice.getContent())
                .author(null)
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    default NoticeListDto toListDto(Notice notice) {
        if (notice == null) {
            return null;
        }
        return NoticeListDto.builder()
                .id(notice.getId())
                .category(NoticeCategoryDto.builder()
                        .id(notice.getNoticeCategory().getId())
                        .name(notice.getNoticeCategory().getName())
                        .build())
                .title(notice.getTitle())
                .author(null)
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}

