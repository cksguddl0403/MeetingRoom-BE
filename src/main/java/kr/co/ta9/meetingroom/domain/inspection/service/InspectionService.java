package kr.co.ta9.meetingroom.domain.inspection.service;

import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.inspection.dto.*;
import kr.co.ta9.meetingroom.domain.inspection.entity.Inspection;
import kr.co.ta9.meetingroom.domain.inspection.exception.InspectionException;
import kr.co.ta9.meetingroom.domain.inspection.mapper.InspectionMapper;
import kr.co.ta9.meetingroom.domain.inspection.repository.InspectionRepository;
import kr.co.ta9.meetingroom.domain.reservation.enums.ReservationStatus;
import kr.co.ta9.meetingroom.domain.reservation.repository.ReservationRepository;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.domain.room.exception.RoomException;
import kr.co.ta9.meetingroom.domain.room.repository.RoomRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.domain.user.exception.UserException;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InspectionService {
    private final InspectionRepository inspectionRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final InspectionMapper inspectionMapper;

    // 점검 등록
    @Transactional
    public InspectionDto createInspection(User currentUser, Long companyId, InspectionCreateRequestDto inspectionCreateRequestDto) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 현재 사용자 회사 소속 확인
        CompanyMember companyMember = validateCurrentUserBelongsToCompany(currentUser, companyId);

        // 관리자 권한 확인
        validateAdminRole(companyMember);

        // 회의실 조회
        Room room = roomRepository.findByIdAndDeletedFalse(inspectionCreateRequestDto.getRoomId())
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        // 점검 시간과 겹치는 다른 점검이 있는지 확인
        boolean hasOverlappingInspection =
                inspectionRepository.existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(), inspectionCreateRequestDto.getEndAt(), inspectionCreateRequestDto.getStartAt());

        // 점검 시간과 겹치는 다른 점검이 있으면 예외 발생
        if (hasOverlappingInspection) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_OVERLAP);
        }

        // 점검 시간과 겹치는 확정된 예약이 있는지 확인
        boolean hasOverlappingConfirmedReservation =
                reservationRepository.existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(), ReservationStatus.CONFIRMED, inspectionCreateRequestDto.getEndAt(), inspectionCreateRequestDto.getStartAt());

        // 점검 시간과 겹치는 확정된 예약이 있으면 예외 발생
        if (hasOverlappingConfirmedReservation) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_RESERVATION_CONFLICT);
        }

        // 점검 생성
        Inspection inspection = Inspection.createInspection(
                inspectionCreateRequestDto.getName(),
                inspectionCreateRequestDto.getStartAt(),
                inspectionCreateRequestDto.getEndAt(),
                room
        );

        // 점검 저장
        inspectionRepository.save(inspection);

        return inspectionMapper.toDto(inspection);
    }

    // 점검 목록 조회
    public OffsetPageResponseDto<InspectionListDto> getInspections(User currentUser, Long companyId, Pageable pageable, InspectionListSearchRequestDto InspectionListSearchRequestDto) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 현재 사용자 회사 소속 확인
        CompanyMember companyMember = validateCurrentUserBelongsToCompany(currentUser, companyId);

        // 관리자 권한 확인
        validateAdminRole(companyMember);

        // 점검 목록 조회
        Page<InspectionQueryDto> page = inspectionRepository.getInspections(companyId, pageable, InspectionListSearchRequestDto);
        List<InspectionListDto> inspectionListDtos = page.stream()
                .map(inspectionMapper::toListDto)
                .toList();

        return OffsetPageResponseDto.<InspectionListDto>builder()
                .totalCount(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .content(inspectionListDtos)
                .build();
    }

    // 점검 수정
    @Transactional
    public InspectionDto updateInspection(
            User currentUser,
            Long companyId,
            Long inspectionId,
            InspectionUpdateRequestDto inspectionUpdateRequestDto
    ) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 현재 사용자 회사 소속 확인
        CompanyMember companyMember = validateCurrentUserBelongsToCompany(currentUser, companyId);

        // 관리자 권한 확인
        validateAdminRole(companyMember);

        // 점검 조회
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new InspectionException(InspectionErrorCode.INSPECTION_NOT_FOUND));

        // 회의실 조회
        Room room = roomRepository.findByIdAndDeletedFalse(inspectionUpdateRequestDto.getRoomId())
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        // 점검 시간과 겹치는 다른 점검이 있는지 확인
        boolean hasOverlappingInspection =
                inspectionRepository.existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                        room.getId(), inspectionUpdateRequestDto.getEndAt(), inspectionUpdateRequestDto.getStartAt(), inspectionId);

        // 점검 시간과 겹치는 다른 점검이 있으면 예외 발생
        if (hasOverlappingInspection) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_OVERLAP);
        }

        // 점검 시간과 겹치는 확정된 예약이 있는지 확인
        boolean hasOverlappingConfirmedReservation =
                reservationRepository.existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(), ReservationStatus.CONFIRMED, inspectionUpdateRequestDto.getEndAt(), inspectionUpdateRequestDto.getStartAt());

        // 점검 시간과 겹치는 확정된 예약이 있으면 예외 발생
        if (hasOverlappingConfirmedReservation) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_RESERVATION_CONFLICT);
        }

        // 점검 수정
        inspection.update(
                inspectionUpdateRequestDto.getName(),
                inspectionUpdateRequestDto.getStartAt(),
                inspectionUpdateRequestDto.getEndAt(),
                room
        );

        // 점검 조회
        InspectionQueryDto inspectionQueryDto = inspectionRepository.getInspectionById(inspectionId)
                .orElseThrow(() -> new InspectionException(InspectionErrorCode.INSPECTION_NOT_FOUND));
        return inspectionMapper.toDto(inspectionQueryDto);
    }

    // 점검 삭제
    @Transactional
    public void deleteInspection(User currentUser, Long companyId, Long inspectionId) {
        // 인증 여부 확인
        if (!currentUser.isCertificated()) {
            throw new UserException(UserErrorCode.NOT_CERTIFICATED_USER);
        }

        // 현재 사용자 회사 소속 확인
        CompanyMember companyMember = validateCurrentUserBelongsToCompany(currentUser, companyId);

        // 관리자 권한 확인
        validateAdminRole(companyMember);

        // 점검 조회
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new InspectionException(InspectionErrorCode.INSPECTION_NOT_FOUND));

        // 점검 삭제
        inspectionRepository.delete(inspection);
    }

    // 현재 사용자 회사 소속 확인
    private CompanyMember validateCurrentUserBelongsToCompany(User currentUser, Long companyId) {
        Optional<CompanyMember> companyMember = companyMemberRepository.findByUser_IdAndCompany_Id(currentUser.getId(), companyId);

        if (companyMember.isEmpty()) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_NOT_AUTHORIZED);
        }

        return companyMember.get();
    }

    // 관리자 권한 확인
    private void validateAdminRole(CompanyMember companyMember) {
        if (companyMember.getRole() != Role.ADMIN) {
            throw new RoomException(RoomErrorCode.ROOM_NOT_AUTHORIZED);
        }
    }
}
