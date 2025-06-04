package dev.bookmark.api.bookmark.controller; // 1. 패키지 선언

import dev.bookmark.api.bookmark.dto.BookmarkCreateRequestDto; // 2. DTO 및 서비스 임포트
import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
// import dev.bookmark.api.bookmark.dto.BookmarkUpdateRequestDto; // 나중에 북마크 수정 시 필요
import dev.bookmark.api.bookmark.dto.BookmarkUpdateRequestDto;
import dev.bookmark.api.bookmark.service.BookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // 페이징 처리
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault; // 기본 페이징 값 설정
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// import java.util.List; // 만약 페이징 없이 전체 목록을 가져오는 API가 있다면

@RestController
@RequestMapping("/api/v1") // 3. API 버전 관리를 위한 공통 경로
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 새 북마크 생성 API
     * @param requestDto 북마크 생성 요청 데이터 (JSON)
     * @return 생성된 북마크 정보 및 HTTP 201 Created
     */
    @PostMapping("/bookmarks") // 4. POST /api/v1/bookmarks
    public ResponseEntity<BookmarkResponseDto> createBookmark(@Valid @RequestBody BookmarkCreateRequestDto requestDto) {
        BookmarkResponseDto createdBookmark = bookmarkService.createBookmark(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBookmark);
    }

    /**
     * 특정 ID의 북마크 정보 조회 API
     * @param bookmarkId 조회할 북마크의 ID (URL 경로 변수)
     * @return 조회된 북마크 정보 및 HTTP 200 OK
     */
    @GetMapping("/bookmarks/{bookmarkId}") // 5. GET /api/v1/bookmarks/{bookmarkId}
    public ResponseEntity<BookmarkResponseDto> getBookmarkById(@PathVariable("bookmarkId") Long bookmarkId) {
        BookmarkResponseDto bookmark = bookmarkService.getBookmarkById(bookmarkId);
        return ResponseEntity.ok(bookmark);
    }

    /**
     * 특정 폴더에 속한 북마크 목록 조회 API (페이징 처리)
     * @param folderId 북마크를 조회할 폴더의 ID (URL 경로 변수)
     * @param pageable 페이징 정보 (예: ?page=0&size=10&sort=createdAt,desc)
     * @return 페이징 처리된 북마크 정보 목록 및 HTTP 200 OK
     */
    @GetMapping("/folders/{folderId}/bookmarks") // 6. GET /api/v1/folders/{folderId}/bookmarks
    public ResponseEntity<Page<BookmarkResponseDto>> getBookmarksByFolder(
            @PathVariable("folderId") Long folderId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        // 7. @PageableDefault: page, size, sort 파라미터가 요청에 없을 경우 사용할 기본값을 설정합니다.
        Page<BookmarkResponseDto> bookmarks = bookmarkService.getBookmarksByFolder(folderId, pageable);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * 특정 ID의 북마크 정보 수정 API
     * @param bookmarkId 수정할 북마크의 ID
     * @param requestDto 수정할 북마크 정보 (JSON)
     * @return 수정된 북마크 정보 및 HTTP 200 OK
     */
    @PutMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<BookmarkResponseDto> updateBookmark(
            @PathVariable("bookmarkId") Long bookmarkId,
            @Valid @RequestBody BookmarkUpdateRequestDto requestDto) { // 방금 만든 DTO 사용
        BookmarkResponseDto updatedBookmark = bookmarkService.updateBookmark(bookmarkId, requestDto); // 서비스 메소드 호출
        return ResponseEntity.ok(updatedBookmark);
    }

    /**
     * 특정 ID의 북마크 삭제 API
     * @param bookmarkId 삭제할 북마크의 ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable("bookmarkId") Long bookmarkId) {
        bookmarkService.deleteBookmark(bookmarkId); // 서비스 메소드 호출
        return ResponseEntity.noContent().build();
    }
}