package kr.co.ta9.meetingroom.domain.room.service;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyRepository;
import kr.co.ta9.meetingroom.domain.equipment.dto.RoomEquipmentItemDto;
import kr.co.ta9.meetingroom.domain.equipment.entity.Equipment;
import kr.co.ta9.meetingroom.domain.equipment.entity.RoomEquipment;
import kr.co.ta9.meetingroom.domain.equipment.exception.EquipmentException;
import kr.co.ta9.meetingroom.domain.equipment.repository.EquipmentRepository;
import kr.co.ta9.meetingroom.domain.equipment.repository.RoomEquipmentRepository;
import kr.co.ta9.meetingroom.domain.room.dto.*;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.domain.room.exception.RoomException;
import kr.co.ta9.meetingroom.domain.room.mapper.RoomMapper;
import kr.co.ta9.meetingroom.domain.room.repository.RoomRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import kr.co.ta9.meetingroom.global.error.code.EquipmentErrorCode;
import kr.co.ta9.meetingroom.global.error.code.RoomErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final RoomMapper roomMapper;

    // 회의실 등록
    @Transactional
    public RoomDto createRoom(User currentUser, Long companyId, RoomCreateRequestDto roomCreateRequestDto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ROOM_REGISTER_MEMBERSHIP_REQUIRED);
        }
        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), companyId, Role.ADMIN)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ROOM_CREATE_ADMIN_REQUIRED);
        }

        if (roomRepository.existsByNameAndCompany_Id(roomCreateRequestDto.getName(), companyId)) {
            throw new RoomException(RoomErrorCode.ROOM_NAME_DUPLICATE);
        }

        Room room = Room.createRoom(roomCreateRequestDto.getName(), roomCreateRequestDto.getMaxCapacity(), company);
        roomRepository.save(room);

        attachRoomEquipments(room, roomCreateRequestDto.getItems());

        List<Long> roomIds = List.of(room.getId());
        List<RoomEquipmentQueryDto> equipmentRows = roomEquipmentRepository.findAllRoomIdIn(roomIds);
        RoomQueryDto roomQueryDto = roomRepository.getRoomById(room.getId(), LocalDateTime.now())
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        Map<Long, List<RoomEquipmentQueryDto>> equipmentByRoom = equipmentRows.stream()
                .collect(Collectors.groupingBy(RoomEquipmentQueryDto::getRoomId));
        return roomMapper.toDto(roomQueryDto, equipmentByRoom.getOrDefault(room.getId(), List.of()));
    }

    // 회의실 목록 조회
    public OffsetPageResponseDto<RoomListDto> getRooms(User currentUser, Long companyId, Pageable pageable, RoomSearchRequestDto roomSearchRequestDto
    ) {

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ACCESS_DENIED);
        }

        LocalDateTime at = LocalDateTime.now();

        Page<RoomQueryDto> roomPage = roomRepository.getRooms(
                companyId,
                roomSearchRequestDto,
                at,
                pageable
        );

        List<Long> roomIds = roomPage.getContent().stream().map(RoomQueryDto::getId).toList();
        List<RoomEquipmentQueryDto> equipmentRows = roomIds.isEmpty()
                ? List.of()
                : roomEquipmentRepository.findAllRoomIdIn(roomIds);

        Map<Long, List<RoomEquipmentQueryDto>> equipmentByRoom = equipmentRows.stream()
                .collect(Collectors.groupingBy(RoomEquipmentQueryDto::getRoomId));

        List<RoomListDto> roomDtos = roomPage.getContent().stream()
                .map(roomQueryDto -> roomMapper.toListDto(roomQueryDto, equipmentByRoom.getOrDefault(roomQueryDto.getId(), List.of())))
                .toList();

        return OffsetPageResponseDto.<RoomListDto>builder()
                .totalCount(roomPage.getTotalElements())
                .page(roomPage.getNumber())
                .size(roomPage.getSize())
                .totalPages(roomPage.getTotalPages())
                .first(roomPage.isFirst())
                .last(roomPage.isLast())
                .hasPrevious(roomPage.hasPrevious())
                .hasNext(roomPage.hasNext())
                .content(roomDtos)
                .build();
    }

    // 회의실 전체 목록 조회
    public List<RoomListDto> getAllRooms(User currentUser, Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_REGISTER_MEMBERSHIP_REQUIRED);
        }

        List<RoomQueryDto> rooms = roomRepository.getAllRooms(companyId, LocalDateTime.now());

        List<Long> roomIds = rooms.stream().map(RoomQueryDto::getId).toList();

        List<RoomEquipmentQueryDto> equipmentRows = roomIds.isEmpty()
                ? List.of()
                : roomEquipmentRepository.findAllRoomIdIn(roomIds);

        Map<Long, List<RoomEquipmentQueryDto>> equipmentByRoom = equipmentRows.stream()
                .collect(Collectors.groupingBy(RoomEquipmentQueryDto::getRoomId));

        return rooms.stream()
                .map(roomQueryDto -> roomMapper.toListDto(roomQueryDto, equipmentByRoom.getOrDefault(roomQueryDto.getId(), List.of())))
                .toList();
    }

    // 회의실 수정
    @Transactional
    public RoomDto updateRoom(User currentUser, Long companyId, Long roomId, RoomUpdateRequestDto roomUpdateRequestDto) {
        // 회사 존재 여부 확인
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        // 회사 멤버인지 확인
        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ROOM_REGISTER_MEMBERSHIP_REQUIRED);
        }

        // 회사 관리자인지 확인
        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), companyId, Role.ADMIN)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ROOM_CREATE_ADMIN_REQUIRED);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        if (roomUpdateRequestDto.getName() != null
                && roomRepository.existsByNameAndCompany_IdAndIdNot(roomUpdateRequestDto.getName(), companyId, roomId)) {
            throw new RoomException(RoomErrorCode.ROOM_NAME_DUPLICATE);
        }

        room.update(roomUpdateRequestDto.getName(), roomUpdateRequestDto.getMaxCapacity());

        if (roomUpdateRequestDto.getItems() != null) {
            roomEquipmentRepository.deleteByRoom_Id(room.getId());
            attachRoomEquipments(room, roomUpdateRequestDto.getItems());
        }

        List<Long> roomIds = List.of(room.getId());
        List<RoomEquipmentQueryDto> equipmentRows = roomEquipmentRepository.findAllRoomIdIn(roomIds);
        RoomQueryDto roomQueryDto = roomRepository.getRoomById(room.getId(), LocalDateTime.now())
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

        Map<Long, List<RoomEquipmentQueryDto>> equipmentByRoom = equipmentRows.stream()
                .collect(Collectors.groupingBy(RoomEquipmentQueryDto::getRoomId));
        return roomMapper.toDto(roomQueryDto, equipmentByRoom.getOrDefault(room.getId(), List.of()));
    }

    // 회의실 삭제
    @Transactional
    public void deleteRoom(User currentUser, Long companyId, Long roomId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ROOM_REGISTER_MEMBERSHIP_REQUIRED);
        }
        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), companyId, Role.ADMIN)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ROOM_CREATE_ADMIN_REQUIRED);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));

//        roomEquipmentRepository.deleteByRoom_Id(room.getId());
        room.softDelete();
    }

    // 회의실 비품 연결
    private void attachRoomEquipments(Room room, List<RoomEquipmentItemDto> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (RoomEquipmentItemDto roomEquipmentItemDto : items) {
            Equipment equipment = equipmentRepository.findById(roomEquipmentItemDto.getEquipmentId())
                    .orElseThrow(() -> new EquipmentException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));

            if (roomEquipmentRepository.existsByRoom_IdAndEquipment_IdAndEquipment_DeletedFalse(room.getId(), equipment.getId())) {
                throw new EquipmentException(EquipmentErrorCode.EQUIPMENT_DUPLICATE_IN_ROOM);
            }

            roomEquipmentRepository.save(
                    RoomEquipment.createRoomEquipment(equipment, room, roomEquipmentItemDto.getQuantity(), roomEquipmentItemDto.getStatus())
            );
        }
    }
}
