package kr.co.ta9.meetingroom.domain.equipment.service;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyRepository;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentCreateRequestDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentListDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentQueryDto;
import kr.co.ta9.meetingroom.domain.equipment.dto.EquipmentUpdateRequestDto;
import kr.co.ta9.meetingroom.domain.equipment.entity.Equipment;
import kr.co.ta9.meetingroom.domain.equipment.exception.EquipmentException;
import kr.co.ta9.meetingroom.domain.equipment.mapper.EquipmentMapper;
import kr.co.ta9.meetingroom.domain.equipment.repository.EquipmentRepository;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import kr.co.ta9.meetingroom.global.common.response.OffsetPageResponseDto;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import kr.co.ta9.meetingroom.global.error.code.EquipmentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final EquipmentMapper equipmentMapper;

    // 비품 등록
    @Transactional
    public EquipmentDto createEquipment(
            User currentUser,
            Long companyId,
            EquipmentCreateRequestDto equipmentCreateRequestDto
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_REGISTER_MEMBERSHIP_REQUIRED);
        }
        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), companyId, Role.ADMIN)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_CREATE_ADMIN_REQUIRED);
        }

        if (equipmentRepository.existsByNameAndCompanyIdAndDeletedFalse(equipmentCreateRequestDto.getName(), companyId)) {
            throw new EquipmentException(EquipmentErrorCode.EQUIPMENT_ALREADY_EXISTS);
        }

        Equipment equipment = equipmentRepository.save(
                Equipment.createEquipment(equipmentCreateRequestDto.getName(), company));

        return equipmentMapper.toDto(equipment);
    }

    // 비품 목록 조회
    public OffsetPageResponseDto<EquipmentListDto> getEquipments(User currentUser, Long companyId, String name, Pageable pageable) {
        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_ACCESS_DENIED);
        }

        Page<EquipmentQueryDto> page = equipmentRepository.getEquipments(companyId, name, pageable);

        List<EquipmentQueryDto> rows = page.getContent();
        List<EquipmentListDto> equipmentDtos = rows.stream()
                .map(equipmentMapper::toListDto)
                .toList();

        return OffsetPageResponseDto.<EquipmentListDto>builder()
                .totalCount(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                    .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .content(equipmentDtos)
                .build();
    }

    // 비품 목록 전체 조회
    public List<EquipmentListDto> getAllEquipments(User currentUser, Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_REGISTER_MEMBERSHIP_REQUIRED);
        }

        return equipmentRepository.getAllEquipments(companyId).stream()
                .map(equipmentMapper::toListDto)
                .toList();
    }

    // 비품 수정
    @Transactional
    public EquipmentDto updateEquipment(
            User currentUser,
            Long companyId,
            Long equipmentId,
            EquipmentUpdateRequestDto equipmentUpdateRequestDto
    ) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_REGISTER_MEMBERSHIP_REQUIRED);
        }
        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), companyId, Role.ADMIN)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_CREATE_ADMIN_REQUIRED);
        }

        Equipment equipment = equipmentRepository.findByIdAndDeletedFalse(equipmentId)
                        .orElseThrow(() -> new EquipmentException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));

        equipmentRepository.findByNameAndCompanyIdAndDeletedFalse(equipmentUpdateRequestDto.getName(), companyId)
                .filter(found -> !found.getId().equals(equipmentId))
                .ifPresent(found -> {
                    throw new EquipmentException(EquipmentErrorCode.EQUIPMENT_ALREADY_EXISTS);
                });

        equipment.updateName(equipmentUpdateRequestDto.getName());
        EquipmentQueryDto equipmentQueryDto = equipmentRepository.getByEquipmentId(equipmentId)
                .orElseThrow(() -> new EquipmentException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));
        return equipmentMapper.toDto(equipmentQueryDto);
    }

    // 비품 삭제
    @Transactional
    public void deleteEquipment(User currentUser, Long companyId, Long equipmentId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));

        if (!companyMemberRepository.existsByUser_IdAndCompany_Id(currentUser.getId(), companyId)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_REGISTER_MEMBERSHIP_REQUIRED);
        }
        if (!companyMemberRepository.existsByUser_IdAndCompany_IdAndRole(
                currentUser.getId(), companyId, Role.ADMIN)) {
            throw new CompanyException(CompanyErrorCode.COMPANY_EQUIPMENT_CREATE_ADMIN_REQUIRED);
        }

        Equipment equipment = equipmentRepository.findByIdAndDeletedFalse(equipmentId)
                .orElseThrow(() -> new EquipmentException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));

        equipment.softDelete();
    }
}
