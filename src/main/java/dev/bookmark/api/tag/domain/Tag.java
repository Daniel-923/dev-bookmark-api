package dev.bookmark.api.tag.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100) // 11. JPA: "name" 컬럼 설정
    private String name;
    // nullable = false: null 값을 허용하지 않음 (필수 입력)
    // unique = true: 이 컬럼의 값은 테이블 내에서 유일해야 함 (중복 불가)
    // length = 100: 문자열의 최대 길이를 100으로 제한

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Tag(String name) {
        this.name = name;
    }

    
    /**
     * 태그의 이름을 변경합니다.
     * @param newName 새로운 태그 이름
     */
    public void updateName(String newName) {
        // 여기서 필요하다면 추가적인 비즈니스 유효성 검사를 수행할 수 있습니다.
        // 예를 들어, newName이 특정 패턴을 만족해야 한다거나, 특정 길이를 넘지 않아야 한다는 등의 규칙입니다.
        // (단순히 비어있지 않은지 등의 검사는 보통 요청 DTO에서 @NotBlank 등으로 처리합니다.)
        if (newName != null && !newName.isBlank()) { // 새로운 이름이 유효한 경우에만 변경
            this.name = newName;
            // 만약 Tag 엔티티에 updatedAt 필드가 있고, @UpdateTimestamp를 사용하지 않는다면
            // 여기서 this.updatedAt = LocalDateTime.now(); 와 같이 수동으로 갱신할 수 있습니다.
        }
    }




}
