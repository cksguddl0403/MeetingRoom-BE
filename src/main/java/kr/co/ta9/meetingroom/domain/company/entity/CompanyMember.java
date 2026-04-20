package kr.co.ta9.meetingroom.domain.company.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.company.enums.Status;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDate joinedDate;

    @Column(nullable = true)
    private LocalDate resignedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private CompanyMember(Role role, User user, Company company, LocalDate joinedDate, Status status) {
        this.role = role;
        this.user = user;
        this.company = company;
        this.joinedDate = joinedDate;
        this.status = status;
    }

    public static CompanyMember createCompanyMember(Role role, User user, Company company) {
        return CompanyMember.builder()
                .role(role)
                .user(user)
                .company(company)
                .joinedDate(LocalDate.now())
                .status(Status.ACTIVE)
                .build();
    }
}
