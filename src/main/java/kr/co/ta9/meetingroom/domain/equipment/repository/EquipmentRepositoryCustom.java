package kr.co.ta9.meetingroom.domain.equipment.repository;


import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepositoryCustom {
    Optional<EquipmentQueryDto> getByEquipmentId(Long equipmentId);
    Page<EquipmentQueryDto> getEquipments(Long companyId, String name, Pageable pageable);
    List<EquipmentQueryDto> getAllEquipments(Long companyId);

}
