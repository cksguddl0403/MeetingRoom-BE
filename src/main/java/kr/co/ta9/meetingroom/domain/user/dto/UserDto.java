package kr.co.ta9.meetingroom.domain.user.dto;

import kr.co.ta9.meetingroom.domain.company.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {
    private Long id;
    private String loginId;
    private String name;
    private String email;
    private UserCompanyDto company;
    private Role role;
    private boolean certificated;

    @Builder
    private UserDto(Long id, String loginId, String name, String email, UserCompanyDto company, Role role, boolean certificated) {
        this.id = id;
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.company = company;
        this.role = role;
        this.certificated = certificated;
    }
}
