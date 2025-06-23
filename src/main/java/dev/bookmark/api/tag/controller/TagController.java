package dev.bookmark.api.tag.controller; // 1. 패키지 선언

import dev.bookmark.api.tag.dto.TagCreateRequestDto; // 2. DTO 임포트
import dev.bookmark.api.tag.dto.TagResponseDto;
import dev.bookmark.api.tag.service.TagService; // 3. 서비스 임포트
import io.swagger.v3.oas.annotations.Operation; // Operation 어노테이션 임포트
import io.swagger.v3.oas.annotations.Parameter; // Parameter 어노테이션 임포트
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger의 Tag 어노테이션 임포트
import jakarta.validation.Valid; // 4. 유효성 검사 어노테이션 임포트
import lombok.RequiredArgsConstructor; // 5. Lombok 임포트
import org.springframework.http.HttpStatus; // 6. HTTP 상태 코드 임포트
import org.springframework.http.ResponseEntity; // 7. HTTP 응답 객체 임포트
import org.springframework.web.bind.annotation.*; // 8. Spring Web 어노테이션 임포트

import java.util.List;

@Tag(name = "Tag API", description = "태그 생성, 조회, 수정, 삭제를 위한 API") // 1. 컨트롤러 그룹 이름 설정
@RestController // 9. 이 클래스가 RESTful API의 컨트롤러임을 나타냅니다. 각 메소드는 @ResponseBody를 기본으로 가집니다.
@RequestMapping("/api/v1/tags") // 10. 이 컨트롤러의 모든 API 요청 경로는 "/api/v1/tags"로 시작합니다.
@RequiredArgsConstructor // 11. final 필드에 대한 생성자를 자동으로 만들어줍니다 (생성자 주입).
public class TagController {

    private final TagService tagService; // 12. TagService를 주입받습니다.

    /**
     * 새로운 태그를 생성하는 API 엔드포인트
     * HTTP POST 요청을 "/api/v1/tags" 경로로 받습니다.
     * @param requestDto 태그 생성 정보가 담긴 DTO (요청 본문에서 JSON 형태로 받음)
     * @return 생성된 태그 정보와 HTTP 상태 코드 201 (Created)
     */
    @Operation(summary = "새 태그 생성", description = "새로운 태그를 생성합니다. 태그 이름은 중복될 수 없습니다.") // 2. API 기능 요약 설명
    @PostMapping // 13. HTTP POST 요청을 이 메소드와 매핑합니다.
    public ResponseEntity<TagResponseDto> createTag(@Valid @RequestBody TagCreateRequestDto requestDto) {
        // 14. @Valid: requestDto에 대해 유효성 검사를 수행합니다 (예: TagCreateRequestDto의 @NotBlank).
        // 15. @RequestBody: HTTP 요청의 본문(body)에 담긴 JSON 데이터를 TagCreateRequestDto 객체로 변환합니다.

        TagResponseDto createdTag = tagService.createTag(requestDto);

        // 16. ResponseEntity를 사용하여 HTTP 응답 상태 코드(201 Created)와 응답 본문을 함께 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    /**
     * 특정 ID의 태그를 조회하는 API 엔드포인트
     * HTTP GET 요청을 "/api/v1/tags/{tagId}" 경로로 받습니다.
     * @param tagId URL 경로에서 추출한 태그 ID
     * @return 조회된 태그 정보와 HTTP 상태 코드 200 (OK)
     */
    @Operation(summary = "특정 태그 조회", description = "ID를 사용하여 특정 태그의 정보를 조회합니다.")
    @GetMapping("/{tagId}") // 17. HTTP GET 요청을 이 메소드와 매핑합니다. {tagId}는 경로 변수입니다.
    public ResponseEntity<TagResponseDto> getTagById(
            @Parameter(description = "조회할 태그의 ID", required = true, example = "1") // 3. 파라미터에 대한 설명 추가
            @PathVariable("tagId") Long tagId) {
        // 18. @PathVariable: URL 경로에 있는 값(여기서는 tagId)을 메소드 파라미터로 가져옵니다.
        TagResponseDto tag = tagService.getTagById(tagId);
        return ResponseEntity.ok(tag); // ResponseEntity.ok()는 상태 코드 200 OK와 본문을 설정합니다.
    }

    /**
     * 모든 태그 목록을 조회하는 API 엔드포인트
     * HTTP GET 요청을 "/api/v1/tags" 경로로 받습니다.
     * @return 모든 태그 정보 목록과 HTTP 상태 코드 200 (OK)
     */
    @Operation(summary = "모든 태그 목록 조회", description = "시스템에 등록된 모든 태그의 목록을 조회합니다.")
    @GetMapping // 19. HTTP GET 요청을 이 메소드와 매핑합니다. (경로 변수 없음)
    public ResponseEntity<List<TagResponseDto>> getAllTags() {
        List<TagResponseDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 특정 ID의 태그를 수정하는 API 엔드포인트
     * HTTP PUT 요청을 "/api/v1/tags/{tagId}" 경로로 받습니다.
     * @param tagId 수정할 태그의 ID (URL 경로에서 추출)
     * @param requestDto 수정할 태그 정보가 담긴 DTO (요청 본문에서 JSON 형태로 받음)
     * @return 수정된 태그 정보와 HTTP 상태 코드 200 (OK)
     */
    @Operation(summary = "태그 정보 수정", description = "특정 ID의 태그 이름을 수정합니다.")
    @PutMapping("/{tagId}") // 20. HTTP PUT 요청을 이 메소드와 매핑합니다.
    public ResponseEntity<TagResponseDto> updateTag(
            @Parameter(description = "수정할 태그의 ID", required = true) @PathVariable("tagId") Long tagId,
            @Valid @RequestBody TagCreateRequestDto requestDto) {
        // TagCreateRequestDto를 수정 요청에 재사용하는 예시
        // 실제로는 TagUpdateRequestDto를 만드는 것이 더 명확할 수 있습니다.
        TagResponseDto updatedTag = tagService.updateTag(tagId, requestDto);
        return ResponseEntity.ok(updatedTag);
    }

    /**
     * 특정 ID의 태그를 삭제하는 API 엔드포인트
     * HTTP DELETE 요청을 "/api/v1/tags/{tagId}" 경로로 받습니다.
     * @param tagId 삭제할 태그의 ID (URL 경로에서 추출)
     * @return HTTP 상태 코드 204 (No Content) - 성공적으로 처리했으나 응답 본문은 없음을 의미
     */
    @Operation(summary = "태그 삭제", description = "특정 ID의 태그를 삭제합니다.")
    @DeleteMapping("/{tagId}") // 21. HTTP DELETE 요청을 이 메소드와 매핑합니다.
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "삭제할 태그의 ID", required = true) @PathVariable("tagId") Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}