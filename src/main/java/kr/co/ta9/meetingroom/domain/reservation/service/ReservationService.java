package kr.co.ta9.meetingroom.domain.reservation.service;

import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
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
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import kr.co.ta9.meetingroom.global.error.code.ReservationErrorCode;
import kr.co.ta9.meetingroom.global.error.code.RoomErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        LocalDateTime startAt = reservationCreateRequestDto.getStartAt();
        LocalDateTime endAt = reservationCreateRequestDto.getEndAt();
        Long roomId = reservationCreateRequestDto.getRoomId();
        List<Long> participantIds = reservationCreateRequestDto.getParticipantUserIds() == null
                ? List.of()
                : reservationCreateRequestDto.getParticipantUserIds();
        List<Long> distinctParticipantUserIds = participantIds.stream().distinct().toList();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        validateAttendeeCountWithinRoomCapacity(room, distinctParticipantUserIds.size());
        validateReservationApplicantBelongsToCompany(currentUser, companyId);
        CompanyMember applicantMember = companyMemberRepository.findByUser_IdAndCompany_Id(currentUser.getId(), companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_ACCESS_DENIED));
        room = loadRoomBelongingToCompany(roomId, companyId);
        validateParticipantCompanyMembership(companyId, distinctParticipantUserIds);
        validateRoomNotUnderInspection(room, startAt, endAt);
        validateNoOverlappingReservationOnRoom(room, startAt, endAt, null);
        validateParticipantsHaveNoScheduleConflict(distinctParticipantUserIds, startAt, endAt, null);

        List<CompanyMember> participantMembers = distinctParticipantUserIds.isEmpty()
                ? List.of()
                : companyMemberRepository.findAllByUser_IdInAndCompany_Id(distinctParticipantUserIds, companyId);
        if (participantMembers.size() != distinctParticipantUserIds.size()) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_PARTICIPANT_NOT_IN_COMPANY);
        }
        Map<Long, CompanyMember> participantMemberByUserId = participantMembers.stream()
                .collect(Collectors.toMap(cm -> cm.getUser().getId(), cm -> cm, (a, b) -> a));

        Reservation reservation = Reservation.createReservation(
                applicantMember, room, reservationCreateRequestDto.getTitle(), startAt, endAt);

        reservationRepository.save(reservation);

        List<ReservationParticipant> participants = new ArrayList<>(distinctParticipantUserIds.size());
        for (Long participantUserId : distinctParticipantUserIds) {
            CompanyMember participantMember = participantMemberByUserId.get(participantUserId);
            participants.add(reservationParticipantRepository.save(
                    ReservationParticipant.createReservationParticipant(reservation, participantMember)));
        }

        return reservationMapper.toDto(reservation, participants);
    }

    // 예약 목록 조회
    public OffsetPageResponseDto<ReservationListDto> getReservations(
            User currentUser,
            Long companyId,
            Pageable pageable,
            ReservationListSearchRequestDto reservationListSearchRequestDto
    ) {
        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ACCESS_DENIED);
        }

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
        validateReservationApplicantBelongsToCompany(currentUser, companyId);

        Reservation reservation = reservationRepository.findByIdAndRoom_Company_Id(reservationId, companyId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getCompanyMember().getUser().getId().equals(currentUser.getId())) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_AUTHORIZED);
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_MODIFIABLE_CANCELED);
        }
        validateReservationNotStarted(reservation.getStartAt(), ReservationErrorCode.RESERVATION_NOT_MODIFIABLE_STARTED);

        LocalDateTime startAt = reservationUpdateRequestDto.getStartAt();
        LocalDateTime endAt = reservationUpdateRequestDto.getEndAt();

        Room room = loadRoomBelongingToCompany(reservationUpdateRequestDto.getRoomId(), companyId);
        List<Long> participantIds = reservationUpdateRequestDto.getParticipantUserIds() == null
                ? List.of()
                : reservationUpdateRequestDto.getParticipantUserIds();
        List<Long> distinctParticipantUserIds = participantIds.stream().distinct().toList();

        validateParticipantCompanyMembership(companyId, distinctParticipantUserIds);
        validateRoomNotUnderInspection(room, startAt, endAt);
        validateNoOverlappingReservationOnRoom(room, startAt, endAt, reservationId);
        validateParticipantsHaveNoScheduleConflict(distinctParticipantUserIds, startAt, endAt, reservationId);
        validateAttendeeCountWithinRoomCapacity(room, distinctParticipantUserIds.size());

        List<CompanyMember> participantMembers = distinctParticipantUserIds.isEmpty()
                ? List.of()
                : companyMemberRepository.findAllByUser_IdInAndCompany_Id(distinctParticipantUserIds, companyId);
        if (participantMembers.size() != distinctParticipantUserIds.size()) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_PARTICIPANT_NOT_IN_COMPANY);
        }
        Map<Long, CompanyMember> participantMemberByUserId = participantMembers.stream()
                .collect(Collectors.toMap(cm -> cm.getUser().getId(), cm -> cm, (a, b) -> a));

        List<Long> participantRowIds = reservationParticipantRepository.findIdsByReservationId(reservationId);
        if (!participantRowIds.isEmpty()) {
            reservationParticipantRepository.deleteByIds(participantRowIds);
        }

        reservation.update(room, reservationUpdateRequestDto.getTitle(), startAt, endAt);

        List<ReservationParticipant> participants = new ArrayList<>(distinctParticipantUserIds.size());

        for (Long participantUserId : distinctParticipantUserIds) {
            CompanyMember participantMember = participantMemberByUserId.get(participantUserId);
            participants.add(reservationParticipantRepository.save(
                    ReservationParticipant.createReservationParticipant(reservation, participantMember)));
        }

        List<ReservationParticipantQueryDto> participantQueryDtos =
                reservationParticipantRepository.getReservationParticipantsByReservationIds(List.of(reservation.getId()));

        ReservationQueryDto reservationQueryDto
                = reservationRepository.getReservationById(currentUser.getId(), reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        return reservationMapper.toDto(reservationQueryDto, participantQueryDtos);
    }

    // 예약 취소
    @Transactional
    public ReservationDto cancelReservation(User currentUser, Long companyId, Long reservationId) {
        validateReservationApplicantBelongsToCompany(currentUser, companyId);

        Reservation reservation = reservationRepository.findByIdAndRoom_Company_Id(reservationId, companyId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getCompanyMember().getUser().getId().equals(currentUser.getId())) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_AUTHORIZED);
        }

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_CANCELLABLE_ALREADY_CANCELED);
        }
        validateReservationNotStarted(reservation.getStartAt(), ReservationErrorCode.RESERVATION_NOT_CANCELLABLE_STARTED);
        reservation.markCanceled();

        List<ReservationParticipantQueryDto> participantQueryDtos =
                reservationParticipantRepository.getReservationParticipantsByReservationIds(List.of(reservation.getId()));

        ReservationQueryDto reservationQueryDto
                = reservationRepository.getReservationById(currentUser.getId(), reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        return reservationMapper.toDto(reservationQueryDto, participantQueryDtos);
    }

    private void validateReservationNotStarted(LocalDateTime startAt, ReservationErrorCode reservationErrorCode) {
        if (!LocalDateTime.now().isBefore(startAt)) {
            throw new ReservationException(reservationErrorCode);
        }
    }

    // 예약 신청자 회사 소속 확인
    private void validateReservationApplicantBelongsToCompany(User applicant, Long companyId) {
        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(applicant.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ACCESS_DENIED);
        }
    }

    // 회사 소속 회의실 조회
    private Room loadRoomBelongingToCompany(Long roomId, Long companyId) {
        return roomRepository.findByIdAndCompany_Id(roomId, companyId)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    // 참가자 회사 소속 확인
    private void validateParticipantCompanyMembership(Long companyId, List<Long> participantUserIds) {
        for (Long pid : participantUserIds) {
            if (!companyMemberRepository.existsByUser_IdAndCompany_Id(pid, companyId)) {
                throw new ReservationException(ReservationErrorCode.RESERVATION_PARTICIPANT_NOT_IN_COMPANY);
            }
        }
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
            List<Long> participantUserIds,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludeReservationId
    ) {
        if (participantUserIds == null || participantUserIds.isEmpty()) {
            return;
        }

        Set<Long> busy = new HashSet<>();

        busy.addAll(reservationRepository.findUserIdsWithOverlappingReservationAsParticipant(
                ReservationStatus.CONFIRMED, startAt, endAt, participantUserIds, excludeReservationId));

        for (Long pid : participantUserIds) {
            if (busy.contains(pid)) {
                throw new ReservationException(ReservationErrorCode.RESERVATION_PARTICIPANT_UNAVAILABLE);
            }
        }
    }

    // 회의실 수용 인원 확인 (예약 신청자 1명 + 참가자)
    private void validateAttendeeCountWithinRoomCapacity(Room room, int participantCount) {
        if (1 + participantCount > room.getMaxCapacity()) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_CAPACITY_EXCEEDED);
        }
    }
}
