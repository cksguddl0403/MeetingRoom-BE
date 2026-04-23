package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.category.entity.Category;
import kr.co.ta9.meetingroom.domain.category.exception.CategoryException;
import kr.co.ta9.meetingroom.domain.category.repository.CategoryRepository;
import kr.co.ta9.meetingroom.domain.inquiry.entity.Inquiry;
import kr.co.ta9.meetingroom.domain.notice.entity.Notice;
import kr.co.ta9.meetingroom.domain.notice.repository.NoticeRepository;
import kr.co.ta9.meetingroom.global.error.code.CategoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(9)
@RequiredArgsConstructor
public class NoticeInitializer implements ApplicationRunner {
    private final CategoryRepository categoryRepository;
    private final NoticeRepository noticeRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        Category category = categoryRepository.findById(3L)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        noticeRepository.save(
                Notice.createNotice(
                        category,
                        "테스트 공지 샘플",
                        "공지 내용입니다.",
                        0
                )
        );
    }
}
