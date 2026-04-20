package kr.co.ta9.meetingroom.domain.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.co.ta9.meetingroom.domain.category.enums.CategoryType;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_category_name_type",
                columnNames = {"name", "type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String name, CategoryType type) {
        this.name = name;
        this.type = type;
    }

    public static Category createCategory(String name, CategoryType type) {
        return Category.builder()
                .name(name)
                .type(type)
                .build();
    }
}
