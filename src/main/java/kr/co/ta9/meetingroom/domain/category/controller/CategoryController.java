package kr.co.ta9.meetingroom.domain.category.controller;

import kr.co.ta9.meetingroom.domain.category.dto.CategoryListDto;
import kr.co.ta9.meetingroom.domain.category.enums.CategoryType;
import kr.co.ta9.meetingroom.domain.category.service.CategoryService;
import kr.co.ta9.meetingroom.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 목록 전체 조회
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryListDto>>> getAllCategories(
            @RequestParam("type") CategoryType categoryType
    ) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(categoryType)));
    }
}
