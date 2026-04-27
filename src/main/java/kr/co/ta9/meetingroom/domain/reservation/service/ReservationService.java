package kr.co.ta9.meetingroom.domain.reservation.service;

import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.inspection.repository.InspectionRepository;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationCreateRequestDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationListDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationParticipantQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationQueryDto;
import kr.co.ta9.meetingroom.domain.reservation.dto.ReservationUpdateRequestDto;
import kr.co.ta9.meetingroom.domain.reservation.mapper.ReservationMapper;
import kr.co.ta9.meetingroom.domain.reservation.entity.Reservation;
import kr.co.ta9.meetingroom.domain.reservation.entity.ReservationParticipant;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import kr.co.ta9.meetingroom.domain.reservation.exception.ReservationException;
import kr.co.ta9.meetingroom.domain.reservation.repository.ReservationParticipantRepository;
import kr.co.ta9.meetingroom.domain.reservation.repository.ReservationRepository;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.domain.room.exception.RoomException;
import kr.co.ta9.meetingroom.domain.room.repository.RoomRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.exception.UserException;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.ReservationErrorCode;
import kr.co.ta9.meetingroom.global.error.code.RoomErrorCode;
import kr.co.ta9.meetingroom.global.error.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationParticipantRepository reservationParticipantRepository;
    private final RoomRepository roomRepository;
    private final InspectionRepository inspectionRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ReservationMapper reservationMapper;

    // 예약 등록
    @Transactional
    public ReservationDto createReservation(User currentUser, Long companyId, ReservationCreateRequestDto reservationCreateRequestDto) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 현재 사용자 회사 소속 확인
        CompanyMember applicantMember = validateCurrentUserBelongsToCompany(currentUser, companyId);

        Long roomId = reservationCreateRequestDto.getRoomId();

        // 회의실이 해당 회사 소속인지 확인
        Room room = validateRoomBelongingToCompany(roomId, companyId);

        List<Long> distinctParticipantCompanyMemberIds = reservationCreateRequestDto.getParticipantCompanyMemberIds().stream().distinct().toList();

        // 회의실 수용 인원 초과하는지 확인
        validateAttendeeCountWithinRoomCapacity(room, distinctParticipantCompanyMemberIds.size());

        // 예약 참가자들이 같은 회사 소속인지 확인
        List<CompanyMember> participantMembers = validateParticipantCompanyMember(companyId, distinctParticipantCompanyMemberIds);

        LocalDateTime startAt = reservationCreateRequestDto.getStartAt();
        LocalDateTime endAt = reservationCreateRequestDto.getEndAt();

        // 요청 일시에 회의실이 점검 중인지 확인
        validateRoomNotUnderInspection(room, startAt, endAt);

        // 요청 일시에 회의실에 이미 확정된 예약이 존재하는지 확인
        validateNoOverlappingReservationOnRoom(room, startAt, endAt, null);

        // 요청 일시에 참가자들이 다른 예약에 참여 중인지 확인
        validateParticipantsHaveNoScheduleConflict(distinctParticipantCompanyMemberIds, startAt, endAt, null);

        // 예약 생성
        Reservation reservation = Reservation.createReservation(applicantMember, room, reservationCreateRequestDto.getTitle(), startAt, endAt);

        // 에약 저장
        reservationRepository.save(reservation);

        // 예약 참가자 생성
        List<ReservationParticipant> participants = participantMembers.stream()
                .map(cm -> ReservationParticipant.createReservationParticipant(reservation, cm))
                .toList();

        // 예약 참가자 저장
        reservationParticipantRepository.saveAll(participants);

        return reservationMapper.toDto(reservation, participants);
    }

    // 예약 목록 조회
    public OffsetPageResponseDto<ReservationListDto> getReservations(
            User currentUser,
            Long companyId,
            Pageable pageable,
            ReservationListSearchRequestDto reservationListSearchRequestDto
    ) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 현재 사용자 회사 소속 확인
        validateCurrentUserBelongsToCompany(currentUser, companyId);

        Page<ReservationQueryDto> page = reservationRepository.getReservations(
                currentUser.getId(), companyId, reservationListSearchRequestDto, pageable);

        List<ReservationQueryDto> rows = page.getContent();
        List<Long> reservationIds = rows.stream().map(ReservationQueryDto::getId).toList();

        List<ReservationParticipantQueryDto> allParticipantRows =
                reservationParticipantRepository.getReservationParticipantsByReservationIds(reservationIds);

        Map<Long, List<ReservationParticipantQueryDto>> participantsByReservationId = allParticipantRows.stream()
                .collect(Collectors.groupingBy(ReservationParticipantQueryDto::getReservationId));

        List<ReservationListDto> reservationDtos = rows.stream()
                .map(q -> reservationMapper.toListDto(
                        q,
                        participantsByReservationId.getOrDefault(q.getId(), List.of())))
                .toList();

        return OffsetPageResponseDto.<ReservationListDto>builder()
                .totalCount(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .content(reservationDtos)
                .build();
    }
    // 예약 수정
    @Transactional
    public ReservationDto updateReservation(
            User currentUser,
            Long companyId,
            Long reservationId,
            ReservationUpdateRequestDto reservationUpdateRequestDto
    ) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 예약 신청자와 현재 사용자가 같은지 확인
        if (!reservation.getCompanyMember().getUser().getId().equals(currentUser.getId())) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_AUTHORIZED);
        }

        // 이미 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_MODIFIABLE_CANCELED);
        }

        // 예약이 이미 시작되지 않았는지 확인
        validateReservationNotStarted(reservation.getStartAt(), ReservationErrorCode.RESERVATION_NOT_MODIFIABLE_STARTED);

        // 현재 사용자 회사 소속 확인
        validateCurrentUserBelongsToCompany(currentUser, companyId);

        Long roomId = reservationUpdateRequestDto.getRoomId();

        // 회의실이 해당 회사 소속인지 확인
        Room room = validateRoomBelongingToCompany(roomId, companyId);

        List<Long> distinctParticipantCompanyMemberIds =  reservationUpdateRequestDto.getParticipantCompanyMemberIds().stream().distinct().toList();

        // 회의실 수용 인원 초과하는지 확인
        validateAttendeeCountWithinRoomCapacity(room, distinctParticipantCompanyMemberIds.size());

        // 예약 참가자들이 같은 회사 소속인지 확인
        List<CompanyMember> participantMembers = validateParticipantCompanyMember(companyId, distinctParticipantCompanyMemberIds);

        LocalDateTime startAt = reservationUpdateRequestDto.getStartAt();
        LocalDateTime endAt = reservationUpdateRequestDto.getEndAt();

        // 요청 일시에 회의실이 점검 중인지 확인
        validateRoomNotUnderInspection(room, startAt, endAt);

        // 요청 일시에 회의실에 이미 확정된 예약이 존재하는지 확인
        validateNoOverlappingReservationOnRoom(room, startAt, endAt, reservationId);

        // 요청 일시에 참가자들이 다른 예약에 참여 중인지 확인
        validateParticipantsHaveNoScheduleConflict(distinctParticipantCompanyMemberIds, startAt, endAt, reservationId);

        // 예약 업데이트
        reservation.update(room, reservationUpdateRequestDto.getTitle(), startAt, endAt);

        // 기존 예약 참가자 삭제
        List<Long> participantRowIds = reservationParticipantRepository.findIdsByReservationId(reservationId);
        if (!participantRowIds.isEmpty()) {
            reservationParticipantRepository.deleteByIds(participantRowIds);
        }

        // 예약 참가자 생성
        List<ReservationParticipant> participants = participantMembers.stream()
                .map(cm -> ReservationParticipant.createReservationParticipant(reservation, cm))
                .toList();

        // 예약 참가자 저장
        reservationParticipantRepository.saveAll(participants);

        return reservationMapper.toDto(reservation, participants);
    }

    // 예약 취소
    @Transactional
    public ReservationDto cancelReservation(User currentUser, Long companyId, Long reservationId) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 예약 신청자와 현재 사용자가 같은지 확인
        if (!reservation.getCompanyMember().getUser().getId().equals(currentUser.getId())) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_AUTHORIZED);
        }

        // 현재 사용자 회사 소속 확인
        validateCurrentUserBelongsToCompany(currentUser, companyId);

        // 이미 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_CANCELLABLE_ALREADY_CANCELED);
        }

        // 예약이 이미 시작되지 않았는지 확인
        validateReservationNotStarted(reservation.getStartAt(), ReservationErrorCode.RESERVATION_NOT_CANCELLABLE_STARTED);

        // 예약 취소
        reservation.cancel();

        // 예약 조회
        ReservationQueryDto reservationQueryDto
                = reservationRepository.getReservationById(currentUser.getId(), reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 예약 참가자 조회
        List<ReservationParticipantQueryDto> participantQueryDtos =
                reservationParticipantRepository.getReservationParticipantsByReservationIds(List.of(reservation.getId()));

        return reservationMapper.toDto(reservationQueryDto, participantQueryDtos);
    }

    // 현재 사용자 회사 소속 확인
    private CompanyMember validateCurrentUserBelongsToCompany(User currentUser, Long companyId) {
        Optional<CompanyMember> companyMember = companyMemberRepository.findByUser_IdAndCompany_Id(currentUser.getId(), companyId);

        if (companyMember.isEmpty()) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_AUTHORIZED);
        }

        return companyMember.get();
    }

    // 회사 소속 회의실 조회
    private Room validateRoomBelongingToCompany(Long roomId, Long companyId) {
        Optional<Room> room = roomRepository.findByIdAndCompany_Id(roomId, companyId);

        if(room.isEmpty()) {
            throw new RoomException(RoomErrorCode.ROOM_NOT_IN_COMPANY);
        }

        return room.get();
    }

    // 회의실 수용 인원 확인
    private void validateAttendeeCountWithinRoomCapacity(Room room, int participantCount) {
        if (participantCount > room.getMaxCapacity()) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_CAPACITY_EXCEEDED);
        }
    }

    // 참가자 회사 소속 확인
    private List<CompanyMember> validateParticipantCompanyMember(Long companyId, List<Long> participantComapnyIds) {
        List<CompanyMember> participantMembers = companyMemberRepository.findAllByIdInAndCompany_Id(participantComapnyIds, companyId);

        if (participantMembers.size() != participantComapnyIds.size()) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_PARTICIPANT_NOT_IN_COMPANY);
        }

        return participantMembers;
    }

    // 회의실 점검 시간 확인
    private void validateRoomNotUnderInspection(Room room, LocalDateTime startAt, LocalDateTime endAt) {
        if (inspectionRepository.existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(room.getId(), endAt, startAt)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_ROOM_UNDER_INSPECTION);
        }
    }

    // 회의실 예약 중복 확인
    private void validateNoOverlappingReservationOnRoom(
            Room room,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludeReservationId
    ) {
        boolean hasOverlappingReservation = excludeReservationId == null
                ? reservationRepository.existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(), ReservationStatus.CONFIRMED, endAt, startAt)
                : reservationRepository.existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                        room.getId(), ReservationStatus.CONFIRMED, endAt, startAt, excludeReservationId);
        if (hasOverlappingReservation) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    // 참가자 일정 충돌 확인
    private void validateParticipantsHaveNoScheduleConflict(
            List<Long> participantCompanyIds,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludeReservationId
    ) {
        if (participantCompanyIds == null || participantCompanyIds.isEmpty()) {
            return;
        }

        Set<Long> busy = new HashSet<>(reservationRepository.findCompanyMemberIdsWithOverlappingReservationAsParticipant(
                startAt, endAt, participantCompanyIds, excludeReservationId));

        if (!busy.isEmpty()) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_PARTICIPANT_UNAVAILABLE);
        }
    }

    // 예약이 시작되지 않았는지 확인
    private void validateReservationNotStarted(LocalDateTime startAt, ReservationErrorCode reservationErrorCode) {
        if (!LocalDateTime.now().isBefore(startAt)) {
            throw new ReservationException(reservationErrorCode);
        }
    }

}
