package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InspectionErrorCode implements ErrorCode {
    INSPECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "INSPECTION-01", "점검을 찾을 수 없습니다."),
    INSPECTION_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "INSPECTION-02", "점검 관리 권한이 없습니다."),
    INSPECTION_RESERVATION_CONFLICT(HttpStatus.BAD_REQUEST, "INSPECTION-03", "해당 시간에 이미 확정된 예약이 있어 점검을 등록할 수 없습니다."),
    INSPECTION_OVERLAP(HttpStatus.BAD_REQUEST, "INSPECTION-04", "해당 시간에 겹치는 점검이 있어 등록할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
