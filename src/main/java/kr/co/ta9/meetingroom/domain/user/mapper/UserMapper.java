package kr.co.ta9.meetingroom.domain.user.mapper;

import kr.co.ta9.meetingroom.domain.company.entity.Company;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.user.dto.UserDto;
import kr.co.ta9.meetingroom.domain.user.dto.UserCompanyDto;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    default UserDto toDto(User user, Company company, Role role) {
        if (user == null) {
            return null;
        }
        UserCompanyDto userCompanyDto = null;
        if (company != null) {
            userCompanyDto = UserCompanyDto.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .build();
        }
        return UserDto.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .email(user.getEmail())
                .company(userCompanyDto)
                .role(role)
                .certificated(user.isCertificated())
                .build();
    }
}
