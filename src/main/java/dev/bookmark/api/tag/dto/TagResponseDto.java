package dev.bookmark.api.tag.dto; // 1. 패키지 선언

import dev.bookmark.api.tag.domain.Tag; // 2. Tag 엔티티를 임포트합니다.
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter // 3. 외부에서 필드 값을 읽을 수 있도록 Getter만 추가합니다. (응답 DTO는 보통 불변으로 만듭니다)
public class TagResponseDto {

    private Long id; // 태그의 고유 ID
    private String name; // 태그 이름
    private LocalDateTime createdAt; // 태그 생성 시간

    @Builder // 4. 빌더 패턴으로 객체를 생성할 수 있게 합니다.
    public TagResponseDto(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    // 5. Tag 엔티티 객체를 TagResponseDto 객체로 변환하는 정적 팩토리 메소드입니다.
    // 서비스 계층 등에서 엔티티를 DTO로 변환할 때 유용하게 사용됩니다.
    public static TagResponseDto fromEntity(Tag tag) {
        return TagResponseDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}