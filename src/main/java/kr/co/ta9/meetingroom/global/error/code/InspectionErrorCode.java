package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InspectionErrorCode implements ErrorCode {
    INSPECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "INSPECTION-01", "점검을 찾을 수 없습니다."),
    INSPECTION_TIME_RANGE_INVALID(HttpStatus.BAD_REQUEST, "INSPECTION-02", "종료 일시는 시작 일시보다 이후여야 합니다."),
    INSPECTION_RESERVATION_CONFLICT(HttpStatus.BAD_REQUEST, "INSPECTION-03", "해당 시간에 이미 확정된 예약이 있어 점검을 등록할 수 없습니다."),
    INSPECTION_CREATE_ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "INSPECTION-04", "점검을 등록할 수 있는 권한이 없습니다. 관리자만 등록할 수 있습니다."),
    INSPECTION_OVERLAP(HttpStatus.BAD_REQUEST, "INSPECTION-05", "해당 시간에 겹치는 점검이 있어 등록할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
