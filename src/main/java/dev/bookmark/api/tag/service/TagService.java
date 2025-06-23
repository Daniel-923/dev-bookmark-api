package dev.bookmark.api.tag.service; // 1. 패키지 선언

import dev.bookmark.api.tag.domain.Tag; // 2. Tag 엔티티 임포트
import dev.bookmark.api.tag.dto.TagCreateRequestDto; // 3. Tag 생성 요청 DTO 임포트
import dev.bookmark.api.tag.dto.TagResponseDto; // 4. Tag 응답 DTO 임포트
import dev.bookmark.api.tag.repository.TagRepository; // 5. Tag 리포지토리 임포트
import lombok.RequiredArgsConstructor; // 6. Lombok: final 필드 생성자 자동 주입
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service; // 7. Spring: 이 클래스가 서비스 계층의 컴포넌트임을 선언
import org.springframework.transaction.annotation.Transactional; // 8. Spring: 트랜잭션 관리 어노테이션

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service // 9. 이 클래스가 비즈니스 로직을 담당하는 서비스 레이어의 스프링 빈(Bean)임을 나타냅니다.
@RequiredArgsConstructor // 10. final로 선언된 필드에 대한 생성자를 자동으로 만들어줍니다 (생성자 주입).
public class TagService {

    private final TagRepository tagRepository; // 11. TagRepository를 주입받습니다.

    /**
     * 새로운 태그를 생성합니다.
     * @param requestDto 태그 생성 요청 데이터
     * @return 생성된 태그 정보
     */
    @Transactional // 12. 이 메소드가 실행될 때 트랜잭션이 시작되고, 성공적으로 끝나면 커밋, 예외 발생 시 롤백됩니다.
    public TagResponseDto createTag(TagCreateRequestDto requestDto) {
        // 13. 이미 같은 이름의 태그가 있는지 확인 (중복 방지)
        tagRepository.findByName(requestDto.getName()).ifPresent(existingTag -> {
            throw new IllegalArgumentException("이미 존재하는 태그 이름입니다: " + requestDto.getName());
            // 추후에는 커스텀 예외(예: DuplicateTagNameException)를 만들어서 사용하는 것이 좋습니다.
        });

        // 14. DTO로부터 Tag 엔티티 생성
        Tag newTag = Tag.builder()
                .name(requestDto.getName())
                .build();

        // 15. Repository를 통해 엔티티를 데이터베이스에 저장
        Tag savedTag = tagRepository.save(newTag);

        // 16. 저장된 엔티티를 응답 DTO로 변환하여 반환
        return TagResponseDto.fromEntity(savedTag);
    }

    /**
     * ID로 특정 태그를 조회합니다.
     * @param tagId 조회할 태그의 ID
     * @return 조회된 태그 정보
     */
    @Transactional(readOnly = true) // 17. 읽기 전용 트랜잭션입니다. 데이터 변경이 없으므로 성능상 약간의 이점을 가집니다.
    public TagResponseDto getTagById(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 태그를 찾을 수 없습니다. ID: " + tagId));
        // 추후에는 커스텀 예외(예: TagNotFoundException)를 사용하는 것이 좋습니다.
        return TagResponseDto.fromEntity(tag);
    }

    /**
     * 모든 태그 목록을 조회합니다.
     * @return 모든 태그 정보 목록
     */
    @Transactional(readOnly = true)
    public List<TagResponseDto> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream() // Stream API를 사용하여 변환
                .map(TagResponseDto::fromEntity) // 각 Tag 엔티티를 TagResponseDto로 매핑
                .collect(Collectors.toList()); // List로 수집
    }

    /**
     * 기존 태그의 이름을 수정합니다.
     * @param tagId 수정할 태그의 ID
     * @param requestDto 새로운 태그 이름이 담긴 요청 데이터 (TagCreateRequestDto 재활용 또는 TagUpdateRequestDto 별도 생성)
     * @return 수정된 태그 정보
     */
    @Transactional // 데이터 변경이 있으므로 트랜잭션 처리
    public TagResponseDto updateTag(Long tagId, TagCreateRequestDto requestDto) {
        // 1. ID로 수정할 Tag 엔티티를 조회합니다.
        //    해당 ID의 태그가 없으면 ResourceNotFoundException 같은 예외를 발생시키는 것이 좋습니다.
        //    (지금은 간단히 IllegalArgumentException 사용)
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 태그를 찾을 수 없습니다. ID: " + tagId));

        String newName = requestDto.getName();

        // 2. 변경하려는 새 이름이 현재 이름과 실제로 다른 경우에만 업데이트 로직을 수행합니다.
        //    이름이 같다면 불필요한 중복 검사나 DB 업데이트 시도를 할 필요가 없습니다.
        if (tag.getName() != null && !tag.getName().equals(newName)) {
            // 3. 변경하려는 새 이름이 시스템 내 다른 태그의 이름과 중복되는지 확인합니다.
            //    단, 현재 수정 중인 태그 자기 자신과의 이름 중복은 허용해야 합니다.
            tagRepository.findByName(newName).ifPresent(existingTag -> {
                if (!existingTag.getId().equals(tag.getId())) { // 다른 ID를 가진 태그가 이미 새 이름을 사용 중이라면
                    throw new IllegalArgumentException("이미 존재하는 태그 이름으로 변경할 수 없습니다: " + newName);
                    // 역시 DuplicateTagNameException 같은 커스텀 예외 사용 권장
                }
            });

            // 4. Tag 엔티티에 직접 정의한 updateName 메소드를 호출하여 이름을 변경합니다.
            tag.updateName(newName);
            // 이 시점에서 tag 객체의 name 필드 값이 메모리상에서 변경됩니다.
            // @Transactional 어노테이션 덕분에, 이 서비스 메소드가 성공적으로 종료(커밋)될 때
            // JPA의 변경 감지(Dirty Checking) 기능이 작동하여,
            // 변경된 'tag' 엔티티를 감지하고 자동으로 UPDATE SQL을 생성하여 데이터베이스에 반영합니다.
            // 따라서 여기서 'tagRepository.save(tag)'를 명시적으로 호출할 필요가 없습니다.
        }

        // 5. 변경된 (또는 변경되지 않았다면 원래의) tag 엔티티 정보를 DTO로 변환하여 반환합니다.
        return TagResponseDto.fromEntity(tag);
    }





    /**
     * 특정 태그를 삭제합니다.
     * @param tagId 삭제할 태그의 ID
     */
    @Transactional
    public void deleteTag(Long tagId) {
        // 20. 삭제하려는 태그가 실제로 존재하는지 확인 (선택적이지만, 더 명확한 피드백을 줄 수 있음)
        if (!tagRepository.existsById(tagId)) {
            throw new IllegalArgumentException("해당 ID의 태그를 찾을 수 없습니다. ID: " + tagId);
        }
        tagRepository.deleteById(tagId);
    }
}