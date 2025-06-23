package dev.bookmark.api.folder.controller; // 1. 패키지 선언

import dev.bookmark.api.folder.domain.Folder;
import dev.bookmark.api.folder.dto.FolderCreateRequestDto; // 2. DTO 및 서비스 임포트
import dev.bookmark.api.folder.dto.FolderResponseDto;
import dev.bookmark.api.folder.dto.FolderTreeResponseDto;
import dev.bookmark.api.folder.dto.FolderUpdateRequestDto;
import dev.bookmark.api.folder.service.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // 3. @Valid 어노테이션 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Folder API", description = "폴더 생성, 조회, 수정, 삭제를 위한 API") // 1. 컨트롤러 그룹 이름 설정
@RestController // 4. REST API 컨트롤러임을 선언
@RequestMapping("/api/v1/folders") // 5. 이 컨트롤러의 기본 URL 경로 설정
@RequiredArgsConstructor // 6. final 필드 생성자 자동 주입
public class FolderController {

    private final FolderService folderService; // 7. FolderService 주입

    /**
     * 새 폴더 생성 API
     * @param requestDto 폴더 생성 요청 데이터 (JSON)
     * @return 생성된 폴더 정보 및 HTTP 201 Created
     */
    @Operation(summary = "새 폴더 생성", description = "새로운 폴더를 생성합니다. parentFolderId를 지정하면 하위 폴더로, 지정하지 않으면 최상위 폴더로 생성됩니다.")
    @PostMapping
    public ResponseEntity<FolderResponseDto> createFolder(@Valid @RequestBody FolderCreateRequestDto requestDto) {
        // 8. @Valid 어노테이션으로 requestDto의 유효성 검사 자동 수행
        FolderResponseDto createdFolder = folderService.createFolder(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFolder);
    }

    /**
     * 특정 ID의 폴더 정보 조회 API
     * @param folderId 조회할 폴더의 ID (URL 경로 변수)
     * @return 조회된 폴더 정보 및 HTTP 200 OK
     */
    @Operation(summary = "특정 폴더 정보 조회", description = "ID를 사용하여 특정 폴더의 정보를 조회합니다.")
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponseDto> getFolderById(
            @Parameter(description = "조회할 폴더의 ID", required = true, example = "1")
            @PathVariable("folderId") Long folderId) {
        FolderResponseDto folder = folderService.getFolderById(folderId);
        return ResponseEntity.ok(folder);
    }

    /**
     * 최상위 폴더 목록 조회 API
     * @return 최상위 폴더 정보 목록 및 HTTP 200 OK
     */
    @Operation(summary = "최상위 폴더 목록 조회", description = "부모가 없는 모든 최상위 폴더의 목록을 조회합니다.")
    @GetMapping("/top") // 9. 최상위 폴더 조회를 위한 별도 경로 (예시)
    public ResponseEntity<List<FolderResponseDto>> getTopLevelFolders() {
        List<FolderResponseDto> folders = folderService.getTopLevelFolders();
        return ResponseEntity.ok(folders);
    }

    /**
     * 특정 부모 폴더의 하위 폴더 목록 조회 API
     * @param parentId 하위 폴더를 조회할 부모 폴더의 ID (URL 경로 변수)
     * @return 해당 부모 폴더의 하위 폴더 정보 목록 및 HTTP 200 OK
     */
    @Operation(summary = "특정 폴더의 하위 폴더 목록 조회", description = "지정한 부모 폴더 ID에 속한 모든 직속 하위 폴더의 목록을 조회합니다.")
    @GetMapping("/{parentId}/children") // 10. 하위 폴더 조회를 위한 경로 (예시)
    public ResponseEntity<List<FolderResponseDto>> getChildFolders(
            @Parameter(description = "하위 폴더를 조회할 부모 폴더의 ID", required = true, example = "1")
            @PathVariable("parentId") Long parentId) {
        List<FolderResponseDto> folders = folderService.getChildFolders(parentId);
        return ResponseEntity.ok(folders);
    }


    /**
     * 전체 폴더 계층 구조(트리)를 조회하는 API
     * @return 계층 구조를 가진 폴더 정보 목록 및 HTTP 200 OK
     */
    @Operation(summary = "전체 폴더 계층 구조(트리) 조회", description = "모든 폴더와 북마크의 전체 계층 구조를 조회합니다.")
    @GetMapping("/tree")
    public ResponseEntity<List<FolderTreeResponseDto>> getFolderTree() {
        List<FolderTreeResponseDto> folderTree = folderService.getFolderTree();
        return ResponseEntity.ok(folderTree);
    }


    /**
     * 특정 ID의 폴더 정보 수정 API
     * @param folderId 수정할 폴더의 ID (URL 경로 변수)
     * @param requestDto 수정할 폴더 정보 (JSON)
     * @return 수정된 폴더 정보 및 HTTP 200 OK
     */
    @Operation(summary = "폴더 정보 수정", description = "특정 ID의 폴더 이름 또는 소속 부모 폴더를 변경합니다.")
    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponseDto> updateFolder(
            @Parameter(description = "수정할 폴더의 ID", required = true) @PathVariable("folderId") Long folderId,
            @Valid @RequestBody FolderUpdateRequestDto requestDto) {
        // 8. @Valid 어노테이션으로 requestDto의 유효성 검사 자동 수행
        FolderResponseDto updatedFolder = folderService.updateFolder(folderId, requestDto);
        return ResponseEntity.ok(updatedFolder);
    }

    /**
     * 특정 ID의 폴더 삭제 API
     * @param folderId 삭제할 폴더의 ID (URL 경로 변수)
     * @return HTTP 204 No Content
     */
    @Operation(summary = "폴더 삭제", description = "특정 ID의 폴더를 삭제합니다. 'force=true' 쿼리 파라미터를 사용하면 하위 폴더까지 함께 삭제됩니다.")
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @Parameter(description = "삭제할 폴더의 ID", required = true) @PathVariable("folderId") Long folderId,
            @Parameter(description = "하위 폴더 강제 삭제 여부", required = false)
            @RequestParam(name = "force", required = false, defaultValue = "false") boolean forceDelete) {
        // 1. @RequestParam으로 "force"라는 이름의 쿼리 파라미터를 받습니다.
        //    - required = false: 이 파라미터는 필수가 아닙니다.
        //    - defaultValue = "false": 만약 "force" 파라미터가 요청에 없으면 기본값으로 false를 사용합니다.
        //    - 예: DELETE /api/v1/folders/2?force=true  => forceDelete는 true
        //    - 예: DELETE /api/v1/folders/2             => forceDelete는 false (기본값)

        folderService.deleteFolder(folderId, forceDelete); // 2. 서비스 메소드에 forceDelete 값을 전달합니다.
        return ResponseEntity.noContent().build();
    }


}