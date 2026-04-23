package kr.co.ta9.meetingroom.domain.reservation.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startAt;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_member_id", nullable = false)
    private CompanyMember companyMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Builder(access = AccessLevel.PRIVATE)
    private Reservation(CompanyMember companyMember, Room room, String title, LocalDateTime startAt, LocalDateTime endAt, ReservationStatus status) {
        this.companyMember = companyMember;
        this.room = room;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status == null ? ReservationStatus.CONFIRMED : status;
    }

    public static Reservation createReservation(CompanyMember companyMember, Room room, String title, LocalDateTime startAt, LocalDateTime endAt) {
        return Reservation.builder()
                .companyMember(companyMember)
                .room(room)
                .title(title)
                .startAt(startAt)
                .endAt(endAt)
                .status(ReservationStatus.CONFIRMED)
                .build();
    }

    public void update(Room room, String title, LocalDateTime startAt, LocalDateTime endAt) {
        this.room = room;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }
}
