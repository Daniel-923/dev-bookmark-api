package dev.bookmark.api.bookmark.controller; // 1. 패키지 선언

import dev.bookmark.api.bookmark.dto.BookmarkCreateRequestDto; // 2. DTO 및 서비스 임포트
import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
// import dev.bookmark.api.bookmark.dto.BookmarkUpdateRequestDto; // 나중에 북마크 수정 시 필요
import dev.bookmark.api.bookmark.dto.BookmarkUpdateRequestDto;
import dev.bookmark.api.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // 페이징 처리
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault; // 기본 페이징 값 설정
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// import java.util.List; // 만약 페이징 없이 전체 목록을 가져오는 API가 있다면

@Tag(name = "Bookmark API", description = "북마크 생성, 조회, 수정, 삭제 및 검색을 위한 API")
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
    @Operation(summary = "새 북마크 생성", description = "새로운 북마크를 특정 폴더에 생성하고 태그를 연결합니다.")
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
    @Operation(summary = "특정 ID의 북마크 정보 조회", description = "ID를 사용하여 특정 북마크의 상세 정보를 조회합니다.")
    @GetMapping("/bookmarks/{bookmarkId}") // 5. GET /api/v1/bookmarks/{bookmarkId}
    public ResponseEntity<BookmarkResponseDto> getBookmarkById(
            @Parameter(description = "조회할 북마크의 ID", required = true, example = "1")
            @PathVariable("bookmarkId") Long bookmarkId) {
        BookmarkResponseDto bookmark = bookmarkService.getBookmarkById(bookmarkId);
        return ResponseEntity.ok(bookmark);
    }

    /**
     * 특정 폴더에 속한 북마크 목록 조회 API (페이징 처리)
     * @param folderId 북마크를 조회할 폴더의 ID (URL 경로 변수)
     * @param pageable 페이징 정보 (예: ?page=0&size=10&sort=createdAt,desc)
     * @return 페이징 처리된 북마크 정보 목록 및 HTTP 200 OK
     */
    @Operation(summary = "특정 폴더 내 북마크 목록 조회 (페이징)", description = "지정한 폴더 ID에 속한 모든 북마크의 목록을 페이징 처리하여 조회합니다.")
    @GetMapping("/folders/{folderId}/bookmarks") // 6. GET /api/v1/folders/{folderId}/bookmarks
    public ResponseEntity<Page<BookmarkResponseDto>> getBookmarksByFolder(
            @Parameter(description = "북마크를 조회할 부모 폴더의 ID", required = true, example = "1")
            @PathVariable("folderId") Long folderId,
            @Parameter(hidden = true) // Swagger UI에서 직접 파라미터를 보여주는 대신, 설명으로 안내
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        // 7. @PageableDefault: page, size, sort 파라미터가 요청에 없을 경우 사용할 기본값을 설정합니다.
        Page<BookmarkResponseDto> bookmarks = bookmarkService.getBookmarksByFolder(folderId, pageable);
        return ResponseEntity.ok(bookmarks);
    }


    /**
     * 키워드 및/또는 태그로 북마크를 검색하는 API (페이징 및 정렬 지원)
     * @param keyword 검색할 키워드 (제목/설명, 선택 사항)
     * @param tagNames 검색할 태그 이름 목록 (쉼표로 구분된 문자열, 선택 사항)
     * @param pageable 페이징 및 정렬 정보 (예: ?page=0&size=10&sort=createdAt,desc)
     * @return 페이징 및 우선순위 정렬이 적용된 북마크 목록
     */
    @Operation(summary = "북마크 검색 (키워드/태그)", description = "키워드(제목,설명) 또는 태그 목록으로 북마크를 검색합니다. 검색 결과는 관련도 높은 순으로 정렬됩니다.")
    @GetMapping("/bookmarks/search")    // GET /api/v1/bookmarks/search
    public ResponseEntity<Page<BookmarkResponseDto>> searchBookmarks(
            @Parameter(description = "검색할 키워드 (선택 사항)", example = "JPA")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "검색할 태그 이름 목록 (쉼표로 구분, 선택 사항)", example = "Java,Spring")
            @RequestParam(name = "tags", required = false)List<String> tagNames,
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        // 1. @RequestParam으로 쿼리 파라미터를 받습니다.
        //    - required = false: 해당 파라미터는 필수가 아님을 의미합니다.
        //    - tagNames의 경우, Spring이 쉼표로 구분된 문자열(예: tags=Java,Spring)을 자동으로 List<String>으로 변환해줍니다.
        // 2. @PageableDefault: 페이징 파라미터가 없을 경우 기본값을 설정합니다.
        //    - direction = Sort.Direction.DESC: 기본 정렬 방향을 내림차순(최신순)으로 설정합니다.

        Page<BookmarkResponseDto> searchResult = bookmarkService.searchBookmarks(keyword, tagNames, pageable);
        return ResponseEntity.ok(searchResult);
    }




    /**
     * 특정 ID의 북마크 정보 수정 API
     * @param bookmarkId 수정할 북마크의 ID
     * @param requestDto 수정할 북마크 정보 (JSON)
     * @return 수정된 북마크 정보 및 HTTP 200 OK
     */
    @Operation(summary = "북마크 정보 수정", description = "특정 ID의 북마크 정보를 수정합니다. 제목, URL, 설명, 소속 폴더, 태그 등을 변경할 수 있습니다.")
    @PutMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<BookmarkResponseDto> updateBookmark(
            @Parameter(description = "수정할 북마크의 ID", required = true)
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
    @Operation(summary = "북마크 삭제", description = "특정 ID의 북마크를 삭제합니다.")
    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @Parameter(description = "삭제할 북마크의 ID", required = true)
            @PathVariable("bookmarkId") Long bookmarkId) {
        bookmarkService.deleteBookmark(bookmarkId); // 서비스 메소드 호출
        return ResponseEntity.noContent().build();
    }
}