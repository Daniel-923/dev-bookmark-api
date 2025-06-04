package dev.bookmark.api.bookmark.dto; // 1. 패키지 선언

import jakarta.validation.constraints.NotBlank; // 2. 유효성 검사 어노테이션
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL; // 3. URL 형식 검증 어노테이션

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter // JSON 요청 바인딩 및 테스트 편의를 위해 Setter 추가
@NoArgsConstructor // 기본 생성자
public class BookmarkCreateRequestDto {

    @NotBlank(message = "북마크 제목은 비어 있을 수 없습니다.")
    @Size(max = 255, message = "북마크 제목은 255자를 초과할 수 없습니다.")
    private String title; // 북마크 제목

    @NotBlank(message = "URL은 비어 있을 수 없습니다.")
    @URL(message = "유효한 URL 형식이 아닙니다.") // 4. URL 형식인지 검증
    @Size(max = 2083, message = "URL은 2083자를 초과할 수 없습니다.")
    private String url; // 북마크 URL

    @Size(max = 10000, message = "설명은 10000자를 초과할 수 없습니다.") // 예시 길이, 필요에 따라 조절
    private String description; // 북마크 설명 (선택 사항)

    @NotNull(message = "폴더 ID는 필수입니다.") // 5. 북마크가 속할 폴더 ID (필수)
    private Long folderId;

    // 6. 이 북마크에 추가할 태그 이름 목록 (선택 사항)
    // 사용자가 태그 이름을 문자열 리스트로 보내면, 서비스에서 기존 태그를 찾거나 새로 생성하여 연결합니다.
    private List<String> tagNames = new ArrayList<>();

    // 테스트나 특정 상황을 위한 생성자 (선택적)
    public BookmarkCreateRequestDto(String title, String url, String description, Long folderId, List<String> tagNames) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.folderId = folderId;
        if (tagNames != null) {
            this.tagNames = tagNames;
        }
    }
}