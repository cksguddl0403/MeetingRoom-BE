package kr.co.ta9.meetingroom.domain.file.entity;

import jakarta.persistence.*;
import kr.co.ta9.meetingroom.domain.file.enums.FileType;
import kr.co.ta9.meetingroom.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false, length = 2083)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileType type;

    @Column( nullable = false)
    private Long targetId;

    @Builder(access = AccessLevel.PRIVATE)
    private File(String name, long size, String extension, String url, FileType type, Long targetId) {
        this.name = name;
        this.size = size;
        this.extension = extension;
        this.url = url;
        this.type = type;
        this.targetId = targetId;
    }

    public static File createFile(String name, long size, String extension, String url, FileType type, Long targetId) {
        return File.builder()
                .name(name)
                .size(size)
                .extension(extension)
                .url(url)
                .type(type)
                .targetId(targetId)
                .build();
    }

    public void updateImage(String name, long size, String extension, String url) {
        this.name = name;
        this.size = size;
        this.extension = extension;
        this.url = url;
    }
}
