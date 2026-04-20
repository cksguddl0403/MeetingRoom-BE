package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY-01", "회사를 찾을 수 없습니다."),
    COMPANY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "COMPANY-02", "이미 존재하는 회사입니다."),
    COMPANY_MEMBERSHIP_NOT_FOUND(HttpStatus.FORBIDDEN, "COMPANY-03", "소속 회사 정보를 찾을 수 없습니다."),
    COMPANY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMPANY-04", "해당 회사에 소속된 사용자만 회의실 정보를 조회할 수 있습니다."),
    COMPANY_ROOM_CREATE_ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "COMPANY-05", "회의실을 등록할 수 있는 권한이 없습니다. 관리자만 등록할 수 있습니다."),
    COMPANY_ROOM_REGISTER_MEMBERSHIP_REQUIRED(HttpStatus.FORBIDDEN, "COMPANY-06", "해당 회사에 소속된 사용자만 회의실을 등록할 수 있습니다."),
    COMPANY_EQUIPMENT_REGISTER_MEMBERSHIP_REQUIRED(HttpStatus.FORBIDDEN, "COMPANY-07", "해당 회사에 소속된 사용자만 비품을 등록할 수 있습니다."),
    COMPANY_EQUIPMENT_CREATE_ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "COMPANY-08", "비품을 등록할 수 있는 권한이 없습니다. 관리자만 등록할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

