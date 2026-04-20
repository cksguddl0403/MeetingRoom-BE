package kr.co.ta9.meetingroom.domain.inspection.repository;

import kr.co.ta9.meetingroom.domain.inspection.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InspectionRepository extends JpaRepository<Inspection, Long>, InspectionRepositoryCustom {

    /*
     * 회사 범위에서 점검 단건을 조회합니다.
     *
     * SELECT i.*
     * FROM inspection i
     * JOIN room rm ON i.room_id = rm.id
     * WHERE i.id = ?
     *   AND rm.company_id = ?
     * LIMIT 1
     */
    Optional<Inspection> findByIdAndRoom_Company_Id(Long id, Long companyId);
    /*
     * 신규 점검 생성 시 시간 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM inspection i
     * WHERE i.room_id = ?
     *   AND i.start_at < ?
     *   AND i.end_at > ?
     */
    boolean existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(Long roomId, LocalDateTime endAt, LocalDateTime startAt);

    /*
     * 점검 수정 시 자기 자신을 제외한 시간 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM inspection i
     * WHERE i.room_id = ?
     *   AND i.start_at < ?
     *   AND i.end_at > ?
     *   AND i.id <> ?
     */
    boolean existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            Long roomId, LocalDateTime endAt, LocalDateTime startAt, Long id);

}
