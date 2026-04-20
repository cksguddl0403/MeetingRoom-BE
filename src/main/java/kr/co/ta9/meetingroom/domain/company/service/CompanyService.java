package kr.co.ta9.meetingroom.domain.company.service;

import kr.co.ta9.meetingroom.domain.company.dto.CompanyDto;
import kr.co.ta9.meetingroom.domain.company.dto.CompanyMemberListDto;
import kr.co.ta9.meetingroom.domain.company.dto.CopanyMemberUserDto;
import kr.co.ta9.meetingroom.domain.company.exception.CompanyException;
import kr.co.ta9.meetingroom.domain.company.mapper.CompanyMapper;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyMemberRepository;
import kr.co.ta9.meetingroom.domain.company.repository.CompanyRepository;
import kr.co.ta9.meetingroom.global.error.code.CompanyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final CompanyMapper companyMapper;

    // 회사 상세 조회
    public CompanyDto getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .map(companyMapper::toDto)
                .orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));
    }

    // 회사 구성원 목록 전체 조회
    public List<CompanyMemberListDto> getAllCompanyMembers(Long companyId) {
        return companyMemberRepository.findAllByCompany_Id(companyId).stream()
                .map(companyMember -> CompanyMemberListDto.builder()
                        .id(companyMember.getId())
                        .user(CopanyMemberUserDto.builder()
                                .id(companyMember.getUser().getId())
                                .nickname(companyMember.getUser().getNickname())
                                .build())
                        .role(companyMember.getRole())
                        .build())
                .toList();
    }
}
