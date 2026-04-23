package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EquipmentErrorCode implements ErrorCode {
    EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EQUIPMENT-01", "비품를 찾을 수 없습니다."),
    EQUIPMENT_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "EQUIPMENT-02", "비품 관리 권한이 없습니다."),
    EQUIPMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "EQUIPMENT-03", "이미 존재하는 비품입니다."),
    EQUIPMENT_DUPLICATE_IN_ROOM(HttpStatus.BAD_REQUEST, "EQUIPMENT-04", "동일한 이름의 비품이 이미 해당 회의실에 등록되어 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

