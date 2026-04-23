package kr.co.ta9.meetingroom.domain.inspection.repository;

import kr.co.ta9.meetingroom.domain.inspection.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InspectionRepository extends JpaRepository<Inspection, Long>, InspectionRepositoryCustom {

    /*
     * 신규 점검 생성 시 시간 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM inspection i
     * WHERE i.room_id = ?
     *   AND i.start_at < ?
     *   AND i.end_at > ?
     */
    boolean existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
            Long roomId, LocalDateTime endAt, LocalDateTime startAt);

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

    void deleteByRoom_Id(Long roomId);

}
