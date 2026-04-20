package kr.co.ta9.meetingroom.domain.notice.enums;

import java.util.Optional;

public enum NoticeSortBy {
    CREATED_AT("createdAt"),
    VIEW_COUNT("viewCount");

    private final String requestProperty;

    NoticeSortBy(String requestProperty) {
        this.requestProperty = requestProperty;
    }

    public String requestProperty() {
        return requestProperty;
    }

    public static Optional<NoticeSortBy> fromRequest(String property) {
        if (property == null || property.isBlank()) {
            return Optional.empty();
        }
        String p = property.strip();
        for (NoticeSortBy v : values()) {
            if (v.name().equalsIgnoreCase(p) || v.requestProperty.equalsIgnoreCase(p)) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    public static String defaultRequestProperty() {
        return CREATED_AT.requestProperty;
    }
}
