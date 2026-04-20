package kr.co.ta9.meetingroom.domain.equipment.repository;

import kr.co.ta9.meetingroom.domain.equipment.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, EquipmentRepositoryCustom {

    /*
     * 회사 내 비품 이름 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM equipment e
     * WHERE e.name = ?
     *   AND e.company_id = ?
     *   AND e.is_deleted = FALSE
     */
    boolean existsByNameAndCompanyIdAndDeletedFalse(String name, Long companyId);

    /*
     * 회사 범위에서 비품 이름으로 단건을 조회합니다.
     *
     * SELECT e.*
     * FROM equipment e
     * WHERE e.name = ?
     *   AND e.company_id = ?
     *   AND e.is_deleted = FALSE
     * LIMIT 1
     */
    Optional<Equipment> findByNameAndCompanyIdAndDeletedFalse(String name, Long companyId);

    /*
     * 비품 ID로 단건을 조회합니다.
     *
     * SELECT e.*
     * FROM equipment e
     * WHERE e.id = ?
     *   AND e.is_deleted = FALSE
     * LIMIT 1
     */
    Optional<Equipment> findByIdAndDeletedFalse(Long equipmentId);
}

