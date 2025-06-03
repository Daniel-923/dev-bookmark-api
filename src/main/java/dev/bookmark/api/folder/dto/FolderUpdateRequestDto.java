package dev.bookmark.api.folder.dto; // 본인의 패키지 경로와 일치하는지 확인

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 클라이언트가 기존 폴더의 정보를 수정하려고 할 때 보낼 데이터를 담는 객체
// 주로 이름이나 부모 폴더를 변경하는 경우

@Getter
@Setter // JSON 요청 바인딩 및 테스트 편의를 위해 Setter 추가
@NoArgsConstructor // 기본 생성자
public class FolderUpdateRequestDto {

    // 이름을 수정할 경우에만 값을 보내도록 합니다.
    // @NotBlank를 사용하면 항상 이름을 보내야 하므로, 수정 시에는 선택적으로 만들기 위해 사용하지 않거나,
    // 별도의 로직으로 비어있는 문자열("")이 들어왔을 때 무시하도록 처리할 수 있습니다.
    // 여기서는 간단하게 @Size만으로 길이 제한을 둡니다.
    @Size(min = 1, max = 100, message = "폴더 이름은 100자를 초과할 수 없습니다.")
    private String name; // 변경할 폴더 이름 (선택 사항)

    // 부모 폴더를 변경할 경우에만 값을 보내도록 합니다.
    // 최상위 폴더로 변경하고 싶다면 null을 보낼 수 있도록 합니다.
    private Long parentFolderId; // 변경할 부모 폴더의 ID (선택 사항, null 가능)

    // 모든 필드를 받는 생성자 (테스트 등에 사용)
    public FolderUpdateRequestDto(String name, Long parentFolderId) {
        this.name = name;
        this.parentFolderId = parentFolderId;
    }
}