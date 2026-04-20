package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.category.entity.Category;
import kr.co.ta9.meetingroom.domain.category.exception.CategoryException;
import kr.co.ta9.meetingroom.domain.category.repository.CategoryRepository;
import kr.co.ta9.meetingroom.domain.inquiry.entity.Inquiry;
import kr.co.ta9.meetingroom.domain.inquiry.repository.InquiryRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.exception.UserException;
import kr.co.ta9.meetingroom.domain.user.repository.UserRepository;
import kr.co.ta9.meetingroom.global.error.code.CategoryErrorCode;
import kr.co.ta9.meetingroom.global.error.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(10)
@RequiredArgsConstructor
public class InquiryInitializer implements ApplicationRunner {
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        User user = userRepository.findByLoginId("cksgud0403")
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(5L)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        inquiryRepository.save(Inquiry.createInquiry(
                user,
                category,
                "테스트 문의 샘플",
                "문의 내용입니다.",
                true
        ));
    }
}
