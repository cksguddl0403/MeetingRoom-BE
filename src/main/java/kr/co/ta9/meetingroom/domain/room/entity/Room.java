package kr.co.ta9.meetingroom.domain.room.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name; // 이름

    @Column(nullable = false)
    private int maxCapacity; // 최대 수용 인원

    @Column(nullable = false , name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted; // 삭제 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Builder(access = AccessLevel.PRIVATE)
    private Room(String name, int maxCapacity, Company company) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.company = company;
    }

    public static Room createRoom(String name, int maxCapacity, Company company) {
        return new Room(name, maxCapacity, company);
    }

    public void update(String name, Integer maxCapacity) {
        if (name != null) {
            this.name = name;
        }
        if (maxCapacity != null) {
            this.maxCapacity = maxCapacity;
        }
    }

    public void softDelete() {
        String suffix = "_" + System.currentTimeMillis();
        this.name = this.name.substring(0, Math.clamp(20 - suffix.length(), 0, this.name.length())) + suffix;
        this.deleted = true;
    }
}
