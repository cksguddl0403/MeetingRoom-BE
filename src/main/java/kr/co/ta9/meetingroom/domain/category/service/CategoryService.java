package kr.co.ta9.meetingroom.domain.category.service;

import kr.co.ta9.meetingroom.domain.category.dto.CategoryListDto;
import kr.co.ta9.meetingroom.domain.category.enums.CategoryType;
import kr.co.ta9.meetingroom.domain.category.mapper.CategoryMapper;
import kr.co.ta9.meetingroom.domain.category.repository.CategoryRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.exception.UserException;
import kr.co.ta9.meetingroom.global.error.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // 카테고리 목록 전체 조회
    public List<CategoryListDto> getAllCategories(User currentUser, CategoryType categoryType) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 카테고리 유형에 따른 목록 조회
        return categoryRepository.findByType(categoryType).stream()
                .map(categoryMapper::toListDto)
                .toList();
    }
}
