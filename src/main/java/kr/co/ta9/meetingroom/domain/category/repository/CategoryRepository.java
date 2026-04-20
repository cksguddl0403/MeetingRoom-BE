package kr.co.ta9.meetingroom.domain.category.repository;

import kr.co.ta9.meetingroom.domain.category.entity.Category;
import kr.co.ta9.meetingroom.domain.category.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /*
     * 카테고리 타입별 목록을 조회합니다.
     *
     * SELECT c.*
     * FROM category c
     * WHERE c.type = ?
     */
    List<Category> findByType(CategoryType type);
}
