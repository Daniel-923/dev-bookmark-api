package dev.bookmark.api.folder.dto;

import dev.bookmark.api.bookmark.dto.BookmarkResponseDto; // 북마크 정보를 담기 위해 임포트
import lombok.Builder;
import lombok.Getter;
import lombok.Setter; // Setter는 필요에 따라 추가 (주로 빌더 사용 시에는 Getter만)

import java.util.List;
import java.util.ArrayList;


/**
 * 폴더의 계층 구조(트리)를 표현하기 위한 응답 DTO 입니다.
 * 각 폴더는 자신의 정보와 함께 하위 폴더 목록(children)과 포함된 북마크 목록(bookmarks)을 가집니다.
 */
@Getter
// @Setter // 보통 응답 DTO는 불변성을 위해 Setter를 잘 사용하지 않지만, 필요시 추가
public class FolderTreeResponseDto {

    private final Long id;          // 폴더의 고유 ID
    private final String name;        // 폴더 이름

    // 이 폴더의 바로 아래에 있는 하위 폴더들의 목록
    // FolderTreeResponseDto 자신과 같은 타입의 리스트를 사용하여 재귀적인 트리 구조를 표현합니다.
    private final List<FolderTreeResponseDto> children;

    // 이 폴더에 직접 속해 있는 북마크들의 목록
    // 기존에 만든 BookmarkResponseDto를 재활용하거나, 트리용으로 더 간략한 북마크 DTO를 만들 수도 있습니다.
    private final List<BookmarkResponseDto> bookmarks;

    @Builder
    public FolderTreeResponseDto(Long id, String name, List<FolderTreeResponseDto> children, List<BookmarkResponseDto> bookmarks) {
        this.id = id;
        this.name = name;
        // 생성 시 null 값이 들어오면 NullPointerException을 방지하기 위해 빈 리스트로 초기화합니다.
        this.children = (children != null) ? children : new ArrayList<>();
        this.bookmarks = (bookmarks != null) ? bookmarks : new ArrayList<>();
    }
}