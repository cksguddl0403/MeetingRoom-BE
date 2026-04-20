package kr.co.ta9.meetingroom.domain.inspection.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.room.entity.Room;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inspection extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startAt;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Builder(access = AccessLevel.PRIVATE)
    private Inspection(String name, LocalDateTime startAt, LocalDateTime endAt, Room room) {
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.room = room;
    }

    public static Inspection createInspection(String name, LocalDateTime startAt, LocalDateTime endAt, Room room) {
        return new Inspection(name, startAt, endAt, room);
    }

    public void update(String name, LocalDateTime startAt, LocalDateTime endAt, Room room) {
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.room = room;
    }
}
