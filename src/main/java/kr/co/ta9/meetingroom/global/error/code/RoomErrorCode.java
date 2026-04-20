package kr.co.ta9.meetingroom.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoomErrorCode implements ErrorCode {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM-01", "회의실을 찾을 수 없습니다."),
    ROOM_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ROOM-02", "이미 존재하는 회의실입니다."),
    ROOM_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ROOM-03", "사용할 수 없는 회의실입니다."),
    ROOM_LIST_INVALID_EQUIPMENT_FILTER(HttpStatus.BAD_REQUEST, "ROOM-04", "비품 조건에 비품 ID가 없거나 최소 수량이 1 미만입니다."),
    ROOM_LIST_INVALID_SORT(HttpStatus.BAD_REQUEST, "ROOM-05", "정렬 방향은 ASC 또는 DESC만 허용됩니다."),
    ROOM_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "ROOM-07", "동일한 이름의 회의실이 이미 등록되어 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

