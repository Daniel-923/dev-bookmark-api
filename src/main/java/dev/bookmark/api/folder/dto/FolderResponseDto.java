package dev.bookmark.api.folder.dto; // 1. 패키지 선언

import dev.bookmark.api.folder.domain.Folder; // 2. Folder 엔티티 임포트
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FolderResponseDto {

    private Long id; // 폴더의 고유 ID
    private String name; // 폴더 이름
    private Long parentFolderId; // 부모 폴더의 ID (최상위 폴더는 null)
    private LocalDateTime createdAt; // 폴더 생성 시간
    private LocalDateTime updatedAt; // 폴더 마지막 수정 시간

    @Builder
    public FolderResponseDto(Long id, String name, Long parentFolderId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.parentFolderId = parentFolderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Folder 엔티티 객체를 FolderResponseDto 객체로 변환하는 정적 팩토리 메소드
    public static FolderResponseDto fromEntity(Folder folder) {
        return FolderResponseDto.builder()
                .id(folder.getId())
                .name(folder.getName())
                // folder.getParentFolder()가 null일 수 있으므로, null 체크 후 ID를 가져옵니다.
                .parentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}