package kr.co.ta9.meetingroom.domain.file.repository;

import kr.co.ta9.meetingroom.domain.file.entity.File;
import kr.co.ta9.meetingroom.domain.file.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    /*
     * 타입과 대상 ID로 파일 목록을 조회합니다.
     *
     * SELECT f.*
     * FROM file f
     * WHERE f.type = ?
     *   AND f.target_id = ?
     */
    List<File> findAllByTypeAndTargetId(FileType type, Long targetId);

    /*
     * 타입과 대상 ID 목록으로 파일 목록을 조회합니다.
     *
     * SELECT f.*
     * FROM file f
     * WHERE f.type = ?
     *   AND f.target_id IN (?, ?, ...)
     */
    List<File> findAllByTypeAndTargetIdIn(FileType type, Collection<Long> targetIds);

    /*
     * 타입과 대상 ID로 파일 단건을 조회합니다.
     *
     * SELECT f.*
     * FROM file f
     * WHERE f.type = ?
     *   AND f.target_id = ?
     * LIMIT 1
     */
    Optional<File> findByTypeAndTargetId(FileType fileType, Long id);
}
