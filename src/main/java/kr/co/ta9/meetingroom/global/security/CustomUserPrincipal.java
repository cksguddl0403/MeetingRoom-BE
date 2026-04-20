package kr.co.ta9.meetingroom.global.security;

import kr.co.ta9.meetingroom.domain.company.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
public class CustomUserPrincipal {
    private Long id;

    @Builder
    private CustomUserPrincipal(Long id) {
        this.id = id;
    }
}
