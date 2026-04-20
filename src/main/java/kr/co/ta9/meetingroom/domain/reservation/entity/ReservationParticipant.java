package kr.co.ta9.meetingroom.domain.reservation.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_participant_reservation_company_member",
                columnNames = {"reservation_id", "company_member_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_member_id", nullable = false)
    private CompanyMember companyMember;

    @Builder(access = AccessLevel.PRIVATE)
    private ReservationParticipant(Reservation reservation, CompanyMember companyMember) {
        this.reservation = reservation;
        this.companyMember = companyMember;
    }

    public static ReservationParticipant createReservationParticipant(Reservation reservation, CompanyMember companyMember) {
        return ReservationParticipant.builder()
                .reservation(reservation)
                .companyMember(companyMember)
                .build();
    }
}
