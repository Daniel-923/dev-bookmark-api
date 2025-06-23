package dev.bookmark.api.folder.service;

import dev.bookmark.api.bookmark.domain.Bookmark;
import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
import dev.bookmark.api.bookmark.repository.BookmarkRepository;
import dev.bookmark.api.folder.domain.Folder;
import dev.bookmark.api.folder.dto.FolderCreateRequestDto;
import dev.bookmark.api.folder.dto.FolderResponseDto;
import dev.bookmark.api.folder.dto.FolderTreeResponseDto;
import dev.bookmark.api.folder.dto.FolderUpdateRequestDto;
import dev.bookmark.api.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository; // 북마크 조회를 위해 필요

    @Transactional
    public FolderResponseDto createFolder(FolderCreateRequestDto requestDto) {
        // FolderCreateRequestDto의 name 필드에 대한 @NotBlank, @Size 등의 기본 유효성 검사는
        // Controller의 @Valid 어노테이션에 의해 이미 처리되었습니다.
        // 따라서 여기서는 해당 검사를 반복할 필요가 없습니다.

        log.info("Creating new folder with name: {}, parentFolderId: {}", requestDto.getName(), requestDto.getParentFolderId());

        // 부모 폴더 ID가 요청에 포함되어 있는지 확인하고, 있다면 해당 부모 폴더를 조회
        Folder parentFolder = null;
        if (requestDto.getParentFolderId() != null) {
            parentFolder = folderRepository.findById(requestDto.getParentFolderId())
                    .orElseThrow(() -> {
                        log.warn("Parent folder not found for ID: {}", requestDto.getParentFolderId());
                        return new IllegalArgumentException("지정한 부모 폴더를 찾을 수 없습니다. ID: " + requestDto.getParentFolderId());
                    });
            log.debug("Found parent folder: {}", parentFolder.getName());
        }

        // 이름 중복 체크 (이것은 DB 조회와 비즈니스 로직이므로 서비스에서 처리)
        Optional<Folder> existingFolder;
        if (parentFolder != null) {
            existingFolder = folderRepository.findByNameAndParentFolder(requestDto.getName(), parentFolder);
        } else {
            existingFolder = folderRepository.findByNameAndParentFolderIsNull(requestDto.getName());
        }

        if (existingFolder.isPresent()) {
            log.warn("Folder name duplication for name: {} under parent: {}", requestDto.getName(), parentFolder != null ? parentFolder.getName() : "root");
            throw new IllegalArgumentException("같은 위치에 이미 동일한 이름의 폴더가 존재합니다: " + requestDto.getName());
        }

        Folder newFolder = Folder.builder()
                .name(requestDto.getName()) // @Valid를 통과한 안전한 name 값
                .parentFolder(parentFolder)
                .build();

        Folder savedFolder = folderRepository.save(newFolder);
        log.info("Folder created successfully with ID: {}", savedFolder.getId());
        return FolderResponseDto.fromEntity(savedFolder);
    }

    /**
     * ID로 특정 폴더를 조회합니다.
     *
     * @param folderId 조회할 폴더의 ID
     * @return 조회된 폴더 정보
     */
    @Transactional(readOnly = true)
    public FolderResponseDto getFolderById(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 폴더를 찾을 수 없습니다. ID: " + folderId));
        return FolderResponseDto.fromEntity(folder);
    }

    /**
     * 모든 최상위 폴더 목록을 조회합니다.
     *
     * @return 모든 최상위 폴더 정보 목록
     */
    @Transactional(readOnly = true)
    public List<FolderResponseDto> getTopLevelFolders() {
        List<Folder> folders = folderRepository.findByParentFolderIsNull();
        return folders.stream()
                .map(FolderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 부모 폴더의 모든 하위 폴더 목록을 조회합니다.
     *
     * @param parentFolderId 부모 폴더의 ID
     * @return 해당 부모 폴더의 하위 폴더 정보 목록
     */
    @Transactional(readOnly = true)
    public List<FolderResponseDto> getChildFolders(Long parentFolderId) {
        if (!folderRepository.existsById(parentFolderId)) {
            throw new IllegalArgumentException("지정한 부모 폴더를 찾을 수 없습니다. ID: " + parentFolderId);
        }
        List<Folder> folders = folderRepository.findByParentFolder_Id(parentFolderId);
        return folders.stream()
                .map(FolderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public FolderResponseDto updateFolder(Long folderId, FolderUpdateRequestDto requestDto) {
        Folder folderToUpdate = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 폴더를 찾을 수 없습니다. ID: " + folderId));

        // 이름 변경 처리
        // FolderUpdateRequestDto의 name 필드에 대한 @Size 등의 기본 유효성 검사는
        // Controller의 @Valid 어노테이션에 의해 이미 처리되었습니다.
        // 여기서는 name 필드가 null이 아니고, 실제 변경이 필요한 경우에만 로직을 수행합니다.
        if (requestDto.getName() != null && !requestDto.getName().isBlank()) { // 이름이 제공되었고, 공백이 아닌 경우
            String newName = requestDto.getName();
            if (!folderToUpdate.getName().equals(newName)) { // 현재 이름과 다른 경우에만 중복 체크 및 업데이트
                Optional<Folder> existingFolderWithName;
                if (folderToUpdate.getParentFolder() != null) {
                    existingFolderWithName = folderRepository.findByNameAndParentFolder(newName, folderToUpdate.getParentFolder());
                } else {
                    existingFolderWithName = folderRepository.findByNameAndParentFolderIsNull(newName);
                }

                if (existingFolderWithName.isPresent() && !existingFolderWithName.get().getId().equals(folderToUpdate.getId())) {
                    throw new IllegalArgumentException("같은 위치에 이미 동일한 이름의 폴더가 존재합니다: " + newName);
                }
                folderToUpdate.updateName(newName); // Folder 엔티티 내부 메소드 호출
            }
        }

        // 부모 폴더 변경 처리
        // parentFolderId가 null로 올 수도 있고(최상위로 변경), 특정 ID로 올 수도 있습니다.
        // 이 부분은 DTO에서 @Valid로 검증할 수 있는 부분이 아니므로 서비스 로직에서 처리합니다.
        if (requestDto.getParentFolderId() != null || (requestDto.getParentFolderId() == null && folderToUpdate.getParentFolder() != null)) {
            // 위 조건은:
            // 1. 새로운 parentFolderId가 제공되었거나 (null이 아닌 값)
            // 2. 새로운 parentFolderId가 null로 제공되었고, 기존에 부모가 있었던 경우 (최상위로 이동)
            // 이 두 경우 모두 부모 폴더 변경 로직을 타도록 합니다.

            Folder newParentFolder = null;
            if (requestDto.getParentFolderId() != null) { // 새로운 부모 ID가 명시적으로 제공된 경우
                // 자기 자신을 부모로 설정하려는 경우 방지
                if (requestDto.getParentFolderId().equals(folderToUpdate.getId())) {
                    throw new IllegalArgumentException("자기 자신을 부모 폴더로 지정할 수 없습니다.");
                }
                newParentFolder = folderRepository.findById(requestDto.getParentFolderId())
                        .orElseThrow(() -> new IllegalArgumentException("새로운 부모 폴더를 찾을 수 없습니다. ID: " + requestDto.getParentFolderId()));
                // TODO: 순환 참조 방지 로직 (새로운 부모가 현재 폴더의 하위 폴더인지 체크)은 여기에 추가해야 합니다.
                //       이 로직은 조금 복잡할 수 있으므로 Phase 2에서 다루거나 지금은 생략할 수 있습니다.
            }
            // newParentFolder가 null이면 최상위로 이동하는 것을 의미합니다.
            folderToUpdate.changeParentFolder(newParentFolder);
        }
        return FolderResponseDto.fromEntity(folderToUpdate);
    }

    /**
     * 특정 폴더를 삭제합니다.
     * (주의: 현재는 하위 폴더나 포함된 북마크에 대한 처리는 하지 않고 단순 삭제합니다.
     * 실제 서비스에서는 하위 항목 처리 정책(함께 삭제, 이동 등)이 필요합니다.)
     *
     * @param folderId 삭제할 폴더의 ID
     */
    @Transactional
    public void deleteFolder(Long folderId, boolean forceDelete) {
        Folder folderToDelete = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("삭제하려는 폴더를 찾을 수 없습니다. ID: " + folderId));

        if (forceDelete) {
            //  forcible delete: 하위 폴더 및 북마크도 함께 삭제
            deleteFolderContentsRecursively(folderToDelete); // 3. 하위 내용 삭제를 위한 헬퍼 메소드 호출
            folderRepository.delete(folderToDelete); // 4. 현재 폴더 삭제
        } else {
            // safe delete: 하위 내용이 있으면 삭제 불가
            if (folderRepository.existsByParentFolder(folderToDelete)) {
                throw new IllegalStateException("하위 폴더가 존재하여 이 폴더를 삭제할 수 없습니다. 강제로 삭제하려면 'force=true' 옵션을 사용하세요.");
            }
            // 5. (나중에 Bookmark 기능 추가 시) 해당 폴더에 속한 북마크 존재 여부 확인
            // if (bookmarkRepository.existsByFolder(folderToDelete)) { // BookmarkRepository 필요
            //     throw new IllegalStateException("폴더 내에 북마크가 존재하여 삭제할 수 없습니다. 강제로 삭제하려면 'force=true' 옵션을 사용하세요.");
            // }
            folderRepository.delete(folderToDelete); // 하위 내용이 없으면 현재 폴더만 삭제
        }
    }

    // 3-1. (헬퍼 메소드) 폴더 내용물 재귀적 삭제
    private void deleteFolderContentsRecursively(Folder parentFolder) {
        // a. 이 폴더에 속한 모든 북마크 삭제 (BookmarkRepository 필요)
        // bookmarkRepository.deleteAllByFolder(parentFolder); // 예시 메소드, 실제 구현 필요

        // b. 이 폴더의 모든 하위 폴더들에 대해 재귀적으로 이 메소드를 호출하여 내용물 삭제 후 폴더 자체 삭제
        List<Folder> childFolders = folderRepository.findByParentFolder_Id(parentFolder.getId());
        for (Folder child : childFolders) {
            deleteFolderContentsRecursively(child); // 재귀 호출
            folderRepository.delete(child);         // 하위 폴더 삭제
        }
    }

    /**
     * 전체 폴더 및 북마크의 계층 구조(트리)를 조회합니다.
     * 데이터베이스 조회를 최소화하기 위해 모든 폴더와 북마크를 한번에 가져와 메모리에서 조립합니다.
     * @return 최상위 폴더들로 구성된 트리 구조 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FolderTreeResponseDto> getFolderTree() {
        log.info("Fetching the entire folder tree structure.");

        // 1. 모든 폴더와 북마크를 데이터베이스에서 한 번에 조회합니다.
        List<Folder> allFolders = folderRepository.findAll();
        List<Bookmark> allBookmarks = bookmarkRepository.findAll();

        // 2. 북마크들을 폴더 ID를 기준으로 그룹핑하여 Map으로 만듭니다. (효율적인 조회를 위해)
        Map<Long, List<BookmarkResponseDto>> bookmarsByFolderId = allBookmarks.stream()
                .map(BookmarkResponseDto::fromEntity) // Bookmark -> BookmarkResponseDto로 변환
                .collect(Collectors.groupingBy(BookmarkResponseDto::getFolderId));

        // 3. 모든 폴더를 FolderTreeResponseDto로 변환하고, Map에 ID를 키로 하여 저장합니다.
        Map<Long, FolderTreeResponseDto> folderDtoMap = allFolders.stream()
                .map(folder -> {
                    // 해당 폴더에 속한 북마크 목록을 가져옵니다 (없으면 빈 리스트).
                    List<BookmarkResponseDto> bookmarksInFolder = bookmarsByFolderId.getOrDefault(folder.getId(), new ArrayList<>());
                    return FolderTreeResponseDto.builder()
                            .id(folder.getId())
                            .name(folder.getName())
                            .bookmarks(bookmarksInFolder)
                            .children(new ArrayList<>())
                            .build();
                })
                .collect(Collectors.toMap(FolderTreeResponseDto::getId, dto -> dto));

        // 4. 부모-자식 관계를 설정하고, 최상위 폴더(루트) 목록을 찾습니다.
        List<FolderTreeResponseDto> rootFolders = new ArrayList<>();
        allFolders.forEach(folder -> {
            FolderTreeResponseDto currentDto = folderDtoMap.get(folder.getId());
            if (folder.getParentFolder() != null) {
                // 부모 폴더가 있는 경우, Map에서 부모 DTO를 찾아 children 리스트에 현재 DTO를 추가합니다.
                FolderTreeResponseDto parentDto = folderDtoMap.get(folder.getParentFolder().getId());
                if (parentDto != null) {  // 부모가 맵에 존재하는 경우 (정상적인 경우 항상 존재)
                    parentDto.getChildren().add(currentDto);
                }
            } else {
                // 부모 폴더가 없는 경우, 최상위 폴더이므로 결과 리스트에 추가합니다.
                rootFolders.add(currentDto);
            }
        });
        log.info("Successfully constructed folder tree with {} root folders.", rootFolders.size());
        return rootFolders;
    }

}

















