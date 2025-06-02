package dev.bookmark.api.tag.dto; // 1. 패키지 선언

import jakarta.validation.constraints.NotBlank; // 2. 유효성 검사 어노테이션 (Spring Boot 3.x 이상)
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // 3. JSON 요청 바인딩을 위해 Setter가 필요할 수 있습니다.

@Getter
@Setter // JSON 직렬화/역직렬화를 위해 Jackson 라이브러리가 사용하며, Setter 또는 특정 생성자가 필요합니다.
@NoArgsConstructor // Jackson은 기본 생성자도 필요로 합니다.
public class TagCreateRequestDto {

    @NotBlank(message = "태그 이름은 비어 있을 수 없습니다.") // 4. 이 필드는 비어있으면 안 된다는 유효성 검사 규칙입니다.
    private String name; // 생성 요청 시 받을 태그 이름

    // 5. (필요하다면) 테스트나 특정 상황을 위한 생성자를 추가할 수 있습니다.
    // public TagCreateRequestDto(String name) {
    //     this.name = name;
    // }
}