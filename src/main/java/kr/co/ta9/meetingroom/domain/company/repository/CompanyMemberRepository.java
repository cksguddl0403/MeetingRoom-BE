package kr.co.ta9.meetingroom.domain.company.repository;

import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {

    /*
     * 특정 사용자-회사 소속 존재 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM company_member cm
     * WHERE cm.user_id = ?
     *   AND cm.company_id = ?
     */
    boolean existsByUser_IdAndCompany_Id(Long userId, Long companyId);

    /*
     * 특정 사용자-회사 소속 단건을 조회합니다.
     *
     * SELECT cm.*
     * FROM company_member cm
     * WHERE cm.user_id = ?
     *   AND cm.company_id = ?
     * LIMIT 1
     */
    Optional<CompanyMember> findByUser_IdAndCompany_Id(Long userId, Long companyId);

    /*
     * 사용자 목록 기준 회사 소속자와 사용자 정보를 조회합니다.
     *
     * SELECT cm.*, u.*
     * FROM company_member cm
     * LEFT JOIN user u ON cm.user_id = u.id
     * WHERE cm.user_id IN (...)
     *   AND cm.company_id = ?
     */
    @Query("SELECT cm FROM CompanyMember cm LEFT JOIN FETCH cm.user WHERE cm.user.id IN :userIds AND cm.company.id = :companyId")
    List<CompanyMember> findAllByUser_IdInAndCompany_Id(@Param("userIds") Collection<Long> userIds, @Param("companyId") Long companyId);

    /*
     * 특정 사용자의 회사 역할 존재 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM company_member cm
     * WHERE cm.user_id = ?
     *   AND cm.company_id = ?
     *   AND cm.role = ?
     */
    boolean existsByUser_IdAndCompany_IdAndRole(Long userId, Long companyId, Role role);

    /*
     * 회사의 전체 소속자와 사용자 정보를 조회합니다.
     *
     * SELECT cm.*, u.*
     * FROM company_member cm
     * LEFT JOIN user u ON cm.user_id = u.id
     * WHERE cm.company_id = ?
     */
    @Query("SELECT cm FROM CompanyMember cm LEFT JOIN FETCH cm.user WHERE cm.company.id = :companyId")
    List<CompanyMember> findAllByCompany_Id(Long companyId);

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
