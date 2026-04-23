package kr.co.ta9.meetingroom.domain.equipment.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 49)
    private String name;

    @Column(nullable = false , name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted; // 삭제 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Builder(access = AccessLevel.PRIVATE)
    private Equipment(String name, Company company) {
        this.name = name;
        this.company = company;
    }

    public static Equipment createEquipment(String name, Company company) {
        return new Equipment(name, company);
    }

    public void update(String name) {
        this.name = name;
    }

    public void softDelete() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS");

        String timestamp = LocalDateTime.now().format(formatter);

        String suffix = "_deleted_" + timestamp;

        this.name += suffix;

        this.deleted = true;
    }
}
