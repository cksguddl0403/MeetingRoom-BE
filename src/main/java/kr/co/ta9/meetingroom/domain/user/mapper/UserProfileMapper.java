package kr.co.ta9.meetingroom.domain.user.mapper;

import kr.co.ta9.meetingroom.domain.user.dto.UserProfileDto;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    default UserProfileDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserProfileDto.builder()
                .nickname(user.getNickname())
                .profileImageUrl(null)
                .build();
    }
}
