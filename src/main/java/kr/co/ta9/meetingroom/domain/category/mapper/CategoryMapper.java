package kr.co.ta9.meetingroom.domain.category.mapper;

import kr.co.ta9.meetingroom.domain.category.dto.CategoryListDto;
import kr.co.ta9.meetingroom.domain.category.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    default CategoryListDto toListDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryListDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
