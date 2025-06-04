package dev.bookmark.api.bookmark.dto; // 실제 패키지 경로로 수정해주세요.

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL; // URL 유효성 검사

import java.util.List;

@Getter
@Setter // JSON 요청 바인딩 및 테스트 편의를 위해 Setter 추가
@NoArgsConstructor
public class BookmarkUpdateRequestDto {

    @Size(max = 255, message = "북마크 제목은 255자를 초과할 수 없습니다.")
    // 이름은 선택적으로 수정 가능하도록 @NotBlank는 제외합니다.
    // 만약 값이 넘어왔는데 비어있는 것을 막고 싶다면, 서비스단에서 추가 검증하거나
    // @Pattern(regexp = "^$|^\\S.*$", message = "제목은 공백으로만 이루어질 수 없습니다.") 등을 사용할 수 있습니다.
    private String title; // 변경할 북마크 제목 (선택 사항)

    @URL(message = "유효한 URL 형식이 아닙니다.")
    @Size(max = 2083, message = "URL은 2083자를 초과할 수 없습니다.")
    // URL도 선택적으로 수정 가능하도록 @NotBlank는 제외합니다.
    private String url; // 변경할 북마크 URL (선택 사항)

    @Size(max = 10000, message = "설명은 10000자를 초과할 수 없습니다.")
    private String description; // 변경할 북마크 설명 (선택 사항)

    // 폴더 이동을 위한 필드 (선택 사항)
    // @NotNull을 사용하지 않아, 이 필드가 요청에 없으면 폴더는 변경하지 않도록 합니다.
    private Long folderId;

    // 태그 변경을 위한 필드 (선택 사항)
    // 이 필드가 요청에 포함되면, 기존 태그는 모두 지우고 새로운 태그 목록으로 대체하는 방식을 고려할 수 있습니다.
    // 또는 추가할 태그, 삭제할 태그를 따로 받는 방식도 있습니다.
    // 여기서는 간단히 새 태그 목록으로 교체하는 시나리오를 가정합니다.
    private List<String> tagNames; // 변경할 태그 이름 목록 (선택 사항)

    // 모든 필드를 받는 생성자 (테스트 등에 사용)
    public BookmarkUpdateRequestDto(String title, String url, String description, Long folderId, List<String> tagNames) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.folderId = folderId;
        this.tagNames = tagNames;
    }
}
