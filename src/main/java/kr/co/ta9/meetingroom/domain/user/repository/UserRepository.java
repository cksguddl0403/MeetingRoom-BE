package kr.co.ta9.meetingroom.domain.user.repository;

import kr.co.ta9.meetingroom.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /*
     * 이메일 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM user u
     * WHERE u.email = ?
     */
    boolean existsByEmail(String email);

    /*
     * 로그인 ID 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM user u
     * WHERE u.login_id = ?
     */
    boolean existsByLoginId(String loginId);

    /*
     * 닉네임 중복 여부를 확인합니다.
     *
     * SELECT COUNT(1) > 0
     * FROM user u
     * WHERE u.nickname = ?
     */
    boolean existsByNickname(String nickname);

    /*
     * 로그인 ID로 사용자 단건을 조회합니다.
     *
     * SELECT u.*
     * FROM user u
     * WHERE u.login_id = ?
     * LIMIT 1
     */
    Optional<User> findByLoginId(String loginId);

    /*
     * 이름과 이메일로 사용자 단건을 조회합니다.
     *
     * SELECT u.*
     * FROM user u
     * WHERE u.name = ?
     *   AND u.email = ?
     * LIMIT 1
     */
    @Query("SELECT u FROM User u WHERE u.name = :name AND u.email = :email")
    Optional<User> findByNameAndEmail(String name, String email);
}
