package kr.co.ta9.meetingroom.domain.inspection.repository;

import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionListSearchRequestDto;
import kr.co.ta9.meetingroom.domain.inspection.dto.InspectionQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InspectionRepositoryCustom {
    Optional<InspectionQueryDto> getInspectionById(Long inspectionId);
    Page<InspectionQueryDto> getInspections(Long companyId, Pageable pageable, InspectionListSearchRequestDto inspectionListSearchRequestDto);
    List<InspectionQueryDto> getAllInspections(Long companyId);
}
