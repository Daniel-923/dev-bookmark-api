package dev.bookmark.api.folder.dto; // 1. 패키지 선언

import jakarta.validation.constraints.NotBlank; // 2. 유효성 검사 어노테이션
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // JSON 요청 바인딩 및 테스트 편의를 위해 Setter 추가
@NoArgsConstructor // 기본 생성자
public class FolderCreateRequestDto {

    @NotBlank(message = "폴더 이름은 비어 있을 수 없습니다.") // 3. 이름은 필수
    @Size(max = 100, message = "폴더 이름은 100자를 초과할 수 없습니다.") // 4. 이름 길이 제한
    private String name; // 생성 요청 시 받을 폴더 이름

    // 5. 부모 폴더 ID (선택 사항).
    // 이 값이 null이거나 전송되지 않으면 최상위 폴더로 간주합니다.
    // 유효한 Long 타입이어야 하지만, 필수는 아니므로 @NotNull 등은 붙이지 않습니다.
    // (서비스 계층에서 이 ID의 유효성(존재 여부)을 검사합니다.)
    private Long parentFolderId;

    // 테스트나 특정 상황을 위한 생성자 (선택적)
    public FolderCreateRequestDto(String name, Long parentFolderId) {
        this.name = name;
        this.parentFolderId = parentFolderId;
    }

    public FolderCreateRequestDto(String name) {
        this.name = name;
        this.parentFolderId = null; // 최상위 폴더 생성 시
    }
}