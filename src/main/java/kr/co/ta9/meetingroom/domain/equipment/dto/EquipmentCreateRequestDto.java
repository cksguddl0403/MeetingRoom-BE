package kr.co.ta9.meetingroom.domain.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EquipmentCreateRequestDto {

    @NotBlank(message = "비품 이름을 입력해 주세요.")
    @Size(max = 50, message = "비품 이름은 최대 50자까지 입력할 수 있습니다.")
    private String name;

    @Builder
    private EquipmentCreateRequestDto(String name) {
        this.name = name;
    }
}
