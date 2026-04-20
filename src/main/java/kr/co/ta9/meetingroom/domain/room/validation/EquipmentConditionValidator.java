package kr.co.ta9.meetingroom.domain.room.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kr.co.ta9.meetingroom.domain.room.dto.EquipmentSearchRequestDto;

/*
 * 회의실 목록 비품 필터 한 건에 대한 규칙.
 * 
 *     - 비품 ID 없이 최소 수량·상태만 있으면 실패
 *     - 비품 ID가 있을 때 최소 수량이 1 미만이면 실패
 *     - 비품 ID·수량·상태 모두 없으면 성공(바인딩 빈 슬롯 무시)
 * 
 */
public class EquipmentConditionValidator implements ConstraintValidator<EquipmentCondition, EquipmentSearchRequestDto> {

    @Override
    public boolean isValid(EquipmentSearchRequestDto equipmentSearchRequestDto, ConstraintValidatorContext context) {
        if (equipmentSearchRequestDto == null) {
            return true;
        }
        boolean hasOptional = equipmentSearchRequestDto.getMinQuantity() != null || equipmentSearchRequestDto.getStatus() != null;
        if (equipmentSearchRequestDto.getEquipmentId() == null) {
            return !hasOptional;
        }
        if (equipmentSearchRequestDto.getMinQuantity() != null && equipmentSearchRequestDto.getMinQuantity() < 1) {
            return false;
        }
        return true;
    }
}
