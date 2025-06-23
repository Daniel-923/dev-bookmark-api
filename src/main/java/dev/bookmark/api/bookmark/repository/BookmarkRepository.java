package dev.bookmark.api.bookmark.repository; // 1. 패키지 선언

import dev.bookmark.api.bookmark.domain.Bookmark; // 2. Bookmark 엔티티 임포트
import dev.bookmark.api.folder.domain.Folder;   // 3. Folder 엔티티 임포트 (폴더별 검색 등)
// import dev.bookmark.api.tag.domain.Tag;      // (나중에 태그별 검색 시 필요할 수 있음)
import org.springframework.data.domain.Page;     // 4. 페이징 처리를 위한 Page 임포트
import org.springframework.data.domain.Pageable; // 4. 페이징 처리를 위한 Pageable 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 5. JPQL 사용을 위한 @Query 임포트
import org.springframework.data.repository.query.Param; // 5. @Query 파라미터 바인딩을 위한 @Param 임포트

import java.util.List;

// JpaRepository<엔티티 클래스, 엔티티의 ID 필드 타입> 인터페이스를 상속받습니다.
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 특정 폴더 ID(folderId)에 속한 모든 북마크를 페이징 처리하여 조회하는 쿼리 메소드
    Page<Bookmark> findByFolder_Id(Long folderId, Pageable pageable);

    // 특정 폴더에 북마크가 하나라도 존재하는지 확인하는 메소드 (폴더 삭제 시 사용 가능)
    boolean existsByFolder(Folder folder);
    // 또는 ID로도 가능
    // boolean existsByFolder_Id(Long folderId);


    /**
     * 키워드(제목/설명) 또는 태그 이름 목록으로 북마크를 검색합니다. (수정된 쿼리)
     * 이 쿼리는 keyword 또는 tagNames 중 하나 이상이 제공되었을 때만 호출되는 것을 가정합니다.
     * @param keyword 검색할 키워드 (null 가능)
     * @param tagNames 검색할 태그 이름 목록 (null 또는 비어있을 수 있음)
     * @return 조건에 맞는 북마크 목록
     */
    @Query("SELECT DISTINCT b FROM Bookmark b LEFT JOIN b.tags t " +
            "WHERE (:keyword IS NOT NULL AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "   OR (:tagNames IS NOT NULL AND t.name IN :tagNames)")
    List<Bookmark> findByKeywordOrTags(
            @Param("keyword") String keyword,
            @Param("tagNames") List<String> tagNames);



    // JpaRepository를 상속받았으므로, 기본적인 CRUD 메소드들은 이미 사용 가능합니다.
    // 예: save(Bookmark bookmark), findById(Long id), findAll(), deleteById(Long id) 등
}