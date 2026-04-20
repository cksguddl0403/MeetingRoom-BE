package kr.co.ta9.meetingroom.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AvailabilityResponseDto {
    private boolean available;

    @Builder
    private AvailabilityResponseDto(boolean available) {
        this.available = available;
    }
}
