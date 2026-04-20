package kr.co.ta9.meetingroom.domain.inspection.service;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyRepository;
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
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import kr.co.ta9.meetingroom.global.error.code.InspectionErrorCode;
import kr.co.ta9.meetingroom.global.error.code.RoomErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final InspectionMapper inspectionMapper;

    // 점검 등록
    @Transactional
    public InspectionDto createInspection(User currentUser, Long companyId, InspectionCreateRequestDto inspectionCreateRequestDto) {
        if (!inspectionCreateRequestDto.getEndAt().isAfter(inspectionCreateRequestDto.getStartAt())) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_TIME_RANGE_INVALID);
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), company.getId(), Role.ADMIN)) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_CREATE_ADMIN_REQUIRED);
        }

        Room room = roomRepository.findById(inspectionCreateRequestDto.getRoomId())
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        boolean hasOverlappingConfirmedReservation =
                reservationRepository.existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(), ReservationStatus.CONFIRMED, inspectionCreateRequestDto.getEndAt(), inspectionCreateRequestDto.getStartAt());
        if (hasOverlappingConfirmedReservation) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_RESERVATION_CONFLICT);
        }

        boolean hasOverlappingInspection =
                inspectionRepository.existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(), inspectionCreateRequestDto.getEndAt(), inspectionCreateRequestDto.getStartAt());
        if (hasOverlappingInspection) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_OVERLAP);
        }

        Inspection inspection = Inspection.createInspection(
                inspectionCreateRequestDto.getName(),
                inspectionCreateRequestDto.getStartAt(),
                inspectionCreateRequestDto.getEndAt(),
                room
        );
        inspectionRepository.save(inspection);

        return inspectionMapper.toDto(inspection);
    }

    // 점검 목록 조회
    public OffsetPageResponseDto<InspectionListDto> getInspections(User currentUser, Long companyId, Pageable pageable, InspectionListSearchRequestDto InspectionListSearchRequestDto) {
        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ACCESS_DENIED);
        }

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
        if (!inspectionUpdateRequestDto.getEndAt().isAfter(inspectionUpdateRequestDto.getStartAt())) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_TIME_RANGE_INVALID);
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), company.getId(), Role.ADMIN)) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_CREATE_ADMIN_REQUIRED);
        }

        Inspection inspection = inspectionRepository.findByIdAndRoom_Company_Id(inspectionId, company.getId())
                .orElseThrow(() -> new InspectionException(InspectionErrorCode.INSPECTION_NOT_FOUND));

        Room room = roomRepository.findById(inspectionUpdateRequestDto.getRoomId())
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        boolean hasOverlappingConfirmedReservation =
                reservationRepository.existsByRoom_IdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(), ReservationStatus.CONFIRMED, inspectionUpdateRequestDto.getEndAt(), inspectionUpdateRequestDto.getStartAt());
        if (hasOverlappingConfirmedReservation) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_RESERVATION_CONFLICT);
        }

        boolean hasOverlappingInspection =
                inspectionRepository.existsByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                        room.getId(), inspectionUpdateRequestDto.getEndAt(), inspectionUpdateRequestDto.getStartAt(), inspectionId);
        if (hasOverlappingInspection) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_OVERLAP);
        }

        inspection.update(
                inspectionUpdateRequestDto.getName(),
                inspectionUpdateRequestDto.getStartAt(),
                inspectionUpdateRequestDto.getEndAt(),
                room
        );
        InspectionQueryDto inspectionQueryDto = inspectionRepository.getInspectionById(inspectionId)
                .orElseThrow(() -> new InspectionException(InspectionErrorCode.INSPECTION_NOT_FOUND));
        return inspectionMapper.toDto(inspectionQueryDto);
    }

    // 점검 삭제
    @Transactional
    public void deleteInspection(User currentUser, Long companyId, Long inspectionId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), company.getId(), Role.ADMIN)) {
            throw new InspectionException(InspectionErrorCode.INSPECTION_CREATE_ADMIN_REQUIRED);
        }

        Inspection inspection = inspectionRepository.findByIdAndRoom_Company_Id(inspectionId, company.getId())
                .orElseThrow(() -> new InspectionException(InspectionErrorCode.INSPECTION_NOT_FOUND));

        inspectionRepository.delete(inspection);
    }
}
