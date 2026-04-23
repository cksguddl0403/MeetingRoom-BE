package kr.co.ta9.meetingroom.domain.reservation.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.ta9.meetingroom.domain.file.entity.QFile;
import kr.co.ta9.meetingroom.domain.file.enums.FileType;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantUserCompanyMemberQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantUserCompanyMemberUserQueryDto;
import kr.co.ta9.meetingroom.domain.company.entity.QCompanyMember;
import kr.co.ta9.meetingroom.domain.reservation.entity.QReservation;
import kr.co.ta9.meetingroom.domain.reservation.entity.QReservationParticipant;
import kr.co.ta9.meetingroom.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ReservationParticipantRepositoryImpl implements ReservationParticipantRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReservationParticipant reservationParticipant = QReservationParticipant.reservationParticipant;
    private final QReservation reservation = QReservation.reservation;
    private final QCompanyMember participantMember = new QCompanyMember("participantMember");
    private final QUser user = QUser.user;
    private final QFile file = QFile.file;

    /*
     * 예약 ID 목록 기준 참가자 상세 정보를 조회합니다.
     *
     * SELECT r.id, rp.id, u.id,
     *        CASE WHEN cm.status = 'RESIGNED' THEN CONCAT(u.nickname, ' (전 직원)') ELSE u.nickname END AS participant_nickname,
     *        ( SELECT f.url FROM file f
     *          WHERE f.type = 'PROFILE' AND f.target_id = u.id
     *          ORDER BY f.id DESC LIMIT 1 )
     * FROM reservation_participant rp
     * LEFT JOIN reservation r ON rp.reservation_id = r.id
     * LEFT JOIN company_member cm ON rp.company_member_id = cm.id
     * LEFT JOIN user u ON cm.user_id = u.id
     * WHERE r.id IN (...)
     */
    @Override
    public List<ReservationParticipantQueryDto> getReservationParticipantsByReservationIds(
            Collection<Long> reservationIds
    ) {
        if (reservationIds == null || reservationIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(Projections.constructor(ReservationParticipantQueryDto.class,
                        reservation.id,
                        reservationParticipant.id,
                        Projections.constructor(ReservationParticipantUserCompanyMemberQueryDto.class,
                                participantMember.id,
                                Projections.constructor(ReservationParticipantUserCompanyMemberUserQueryDto.class,
                                        user.id,
                                        participantDisplayNameExpr(),
                                        JPAExpressions.select(file.url)
                                                .from(file)
                                                .where(
                                                        file.type.eq(FileType.PROFILE),
                                                        file.targetId.eq(user.id)
                                                )
                                                .orderBy(file.id.desc())
                                                .limit(1)
                                )
                        )
                ))
                .from(reservationParticipant)
                .leftJoin(reservationParticipant.reservation, reservation)
                .leftJoin(reservationParticipant.companyMember, participantMember)
                .leftJoin(participantMember.user, user)
                .where(reservation.id.in(reservationIds))
                .fetch();
    }

    /*
     * 참가자 표시명을 생성합니다.
     *
     * CASE WHEN cm.status = 'RESIGNED' THEN CONCAT(u.nickname, ' (전 직원)') ELSE u.nickname END
     */
    private StringExpression participantDisplayNameExpr() {
        return Expressions.stringTemplate(
                "case when {0} = 'RESIGNED' then concat({1}, ' (전 직원)') else {1} end",
                participantMember.status.stringValue(),
                user.nickname
        );
    }
}
