package kr.co.ta9.meetingroom.domain.user.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String loginId; // 로그인 아이디

    @Column(nullable = false, length = 60)
    private String password; // 비밀번호

    @Column(nullable = false, length = 20)
    private String name; // 이름

    @Column(nullable = false, unique = true, length = 16)
    private String nickname; // 닉네임

    @Column(nullable = false, unique = true, length = 255)
    private String email; // 이메일

    @Column(nullable = false , name = "is_certificated", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean certificated; // 인증 여부

    @Builder(access = AccessLevel.PRIVATE)
    private User(String loginId, String password, String name, String nickname, String email, String employmentCertificateFile) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
    }

    public static User createUser(String loginId, String password, String name, String nickname, String email) {
        return User.builder()
                .loginId(loginId)
                .password(password)
                .name(name)
                .nickname(nickname)
                .email(email)
                .build();
    }

    public void updateInfo(String password, String name) {
        if (password != null) {
            this.password = password;
        }
        if (name != null) {
            this.name = name;
        }
    }

    public void updateProfile(String nickname) {
        if(nickname != null) {
            this.nickname = nickname;
        }
    }
}
