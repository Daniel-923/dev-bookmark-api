package dev.bookmark.api.bookmark.dto; // 1. 패키지 선언

import dev.bookmark.api.bookmark.domain.Bookmark; // 2. Bookmark 엔티티 임포트
import dev.bookmark.api.tag.dto.TagResponseDto;    // 3. Tag 응답 DTO 임포트
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class BookmarkResponseDto {

    private Long id;
    private String title;
    private String url;
    private String description;
    private Long folderId; // 북마크가 속한 폴더의 ID
    private String folderName; // 북마크가 속한 폴더의 이름 (선택적으로 추가 가능)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagResponseDto> tags; // 이 북마크에 연결된 태그 정보 목록

    @Builder
    public BookmarkResponseDto(Long id, String title, String url, String description,
                               Long folderId, String folderName,
                               LocalDateTime createdAt, LocalDateTime updatedAt, List<TagResponseDto> tags) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.description = description;
        this.folderId = folderId;
        this.folderName = folderName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = tags;
    }

    // Bookmark 엔티티 객체를 BookmarkResponseDto 객체로 변환하는 정적 팩토리 메소드
    public static BookmarkResponseDto fromEntity(Bookmark bookmark) {
        List<TagResponseDto> tagDtos = bookmark.getTags().stream()
                .map(TagResponseDto::fromEntity) // 각 Tag 엔티티를 TagResponseDto로 변환
                .collect(Collectors.toList());

        return BookmarkResponseDto.builder()
                .id(bookmark.getId())
                .title(bookmark.getTitle())
                .url(bookmark.getUrl())
                .description(bookmark.getDescription())
                .folderId(bookmark.getFolder() != null ? bookmark.getFolder().getId() : null) // Folder가 null일 수 있으므로 체크
                .folderName(bookmark.getFolder() != null ? bookmark.getFolder().getName() : null) // Folder 이름도 추가
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .tags(tagDtos)
                .build();
    }
}