package kr.co.ta9.meetingroom.domain.room.enums;

import java.util.Optional;

public enum RoomSortBy {
    MAX_CAPACITY("maxCapacity");

    private final String requestProperty;

    RoomSortBy(String requestProperty) {
        this.requestProperty = requestProperty;
    }

    public String requestProperty() {
        return requestProperty;
    }

    /*
     * Sort 속성이 enum 상수명 또는 API용 #requestProperty 와 대소문자 무시로 일치하면 해당 상수.
     */
    public static Optional<RoomSortBy> fromRequest(String property) {
        if (property == null || property.isBlank()) {
            return Optional.empty();
        }
        String p = property.strip();
        for (RoomSortBy v : values()) {
            if (v.name().equalsIgnoreCase(p) || v.requestProperty.equalsIgnoreCase(p)) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    public static String defaultRequestProperty() {
        return MAX_CAPACITY.requestProperty;
    }
}
