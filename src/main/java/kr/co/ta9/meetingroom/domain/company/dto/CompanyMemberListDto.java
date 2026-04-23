package kr.co.ta9.meetingroom.domain.company.dto;

import kr.co.ta9.meetingroom.domain.company.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompanyMemberListDto {
    private Long id;
    private CopanyMemberUserDto user;
    private Role role;

    @Builder
    private CompanyMemberListDto(Long id, CopanyMemberUserDto user, Role role) {
        this.id = id;
        this.user = user;
        this.role = role;
    }
}
