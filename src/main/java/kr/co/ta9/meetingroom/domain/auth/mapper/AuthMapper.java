package kr.co.ta9.meetingroom.domain.auth.mapper;

import kr.co.ta9.meetingroom.domain.auth.dto.JwtTokenDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    default JwtTokenDto toDto(String accessToken) {
        return JwtTokenDto.builder()
                .accessToken(accessToken)
                .build();
    }
}
