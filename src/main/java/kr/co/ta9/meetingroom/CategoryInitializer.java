package kr.co.ta9.meetingroom;

import kr.co.ta9.meetingroom.domain.category.entity.Category;
import kr.co.ta9.meetingroom.domain.category.enums.CategoryType;
import kr.co.ta9.meetingroom.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(0)
@RequiredArgsConstructor
public class CategoryInitializer implements ApplicationRunner {
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        categoryRepository.saveAll(List.of(
                Category.createCategory("공지", CategoryType.NOTICE),
                Category.createCategory("업데이트", CategoryType.NOTICE)
//                Category.createCategory("점검", CategoryType.NOTICE)
        ));

        categoryRepository.saveAll(List.of(
                Category.createCategory("일반", CategoryType.INQUIRY),
                Category.createCategory("계정", CategoryType.INQUIRY)
//                Category.createCategory("기타", CategoryType.INQUIRY)
        ));
    }
}
