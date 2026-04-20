package kr.co.ta9.meetingroom.domain.room.repository;

import kr.co.ta9.meetingroom.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryCustom {

    /*
     * 회사 내 회의실 이름 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM room r
     * WHERE r.name = ?
     *   AND r.company_id = ?
     *   AND r.is_deleted = false
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM Room r
            WHERE r.name = :name
              AND r.company.id = :companyId
              AND r.deleted = false
            """)
    boolean existsByNameAndCompany_Id(@Param("name") String name, @Param("companyId") Long companyId);

    /*
     * 수정 대상 제외 후 회사 내 회의실 이름 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM room r
     * WHERE r.name = ?
     *   AND r.company_id = ?
     *   AND r.id <> ?
     *   AND r.is_deleted = false
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM Room r
            WHERE r.name = :name
              AND r.company.id = :companyId
              AND r.id <> :id
              AND r.deleted = false
            """)
    boolean existsByNameAndCompany_IdAndIdNot(
            @Param("name") String name,
            @Param("companyId") Long companyId,
            @Param("id") Long id
    );

    /*
     * 회사 범위에서 회의실 단건을 조회합니다.
     *
     * SELECT r.*
     * FROM room r
     * WHERE r.id = ?
     *   AND r.company_id = ?
     * LIMIT 1
     */
    Optional<Room> findByIdAndCompany_Id(Long id, Long companyId);
}
