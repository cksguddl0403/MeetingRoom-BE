package kr.co.ta9.meetingroom.global.error.code;

import kr.co.ta9.meetingroom.domain.company.entity.CompanyMember;
import kr.co.ta9.meetingroom.domain.company.enums.Role;
import kr.co.ta9.meetingroom.domain.equipment.exception.EquipmentException;
import kr.co.ta9.meetingroom.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum RoomErrorCode implements ErrorCode {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM-01", "회의실을 찾을 수 없습니다."),
    ROOM_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "ROOM-04", "회의실 관리 권한이 없습니다."),
    ROOM_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ROOM-02", "이미 존재하는 회의실입니다."),
    ROOM_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ROOM-03", "사용할 수 없는 회의실입니다."),
    ROOM_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "ROOM-07", "동일한 이름의 회의실이 이미 등록되어 있습니다."),
    ROOM_NOT_IN_COMPANY(HttpStatus.BAD_REQUEST, "ROOM-08" , "회사가 소속된 회의실이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

