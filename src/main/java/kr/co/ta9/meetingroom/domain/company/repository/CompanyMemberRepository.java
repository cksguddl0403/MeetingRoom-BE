package kr.co.ta9.meetingroom.domain.company.repository;

import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.company.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {

    /*
     * 특정 화사 멤버 단건을 조회합니다.
     *
     * SELECT cm.*
     * FROM company_member cm
     * WHERE cm.user_id = ?
     *   AND cm.company_id = ?
     * LIMIT 1
     */
    Optional<CompanyMember> findByUser_IdAndCompany_Id(Long userId, Long companyId);

    /*
     * 특정 회사의 회사 멤버 목록을 조회한다
     *
     * SELECT cm.*
     * FROM company_member cm
     * WHERE cm.id IN (...)
     *   AND cm.company_id = ?
     */

    List<CompanyMember> findAllByIdInAndCompany_Id(Collection<Long> companyMemberIds, Long companyId);

    /*
     * 회사의 회사 멤버 목록을 조회합니다.
     *
     * SELECT cm.*, u.*
     * FROM company_member cm
     * LEFT JOIN user u ON cm.user_id = u.id
     * WHERE cm.company_id = ?
     */
    @Query("""
            SELECT cm
            FROM CompanyMember cm
            LEFT JOIN FETCH cm.user
            WHERE cm.company.id = :companyId
            """)
    List<CompanyMember> findAllByCompany_Id(@Param("companyId") Long companyId);

    /*
     * 사용자 기준 회사 소속 단건과 회사 정보를 조회합니다.
     *
     * SELECT cm.*, c.*
     * FROM company_member cm
     * LEFT JOIN company c ON cm.company_id = c.id
     * WHERE cm.user_id = ?
     * LIMIT 1
     */
    @Query("SELECT cm FROM CompanyMember cm LEFT JOIN FETCH cm.company WHERE cm.user.id = :userId")
    Optional<CompanyMember> findWithCompanyByUser_Id(Long userId);
}
