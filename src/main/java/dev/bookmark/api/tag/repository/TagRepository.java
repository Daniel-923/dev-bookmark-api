package dev.bookmark.api.tag.repository;

import dev.bookmark.api.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// <Tag, Long>: Tag는 이 리포지토리가 다룰 엔티티의 타입, Long은 해당 엔티티의 ID 필드 타입을 지정
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그 이름으로 태그를 찾는 쿼리 메서드 정의
    // Spring Data JPA는 메소드 이름을 분석해서 자동으로 쿼리를 생성
    // "findByName" -> "SELECT t FROM Tag t WHERE t.name = :name" 와 유사한 JPQL을 실행합니다.
    // Optional<T>은 결과가 없을 수도 있음을 명시적으로 표현하여 NullPointerException을 방지하는 데 도움을 줍니다.
    Optional<Tag> findByName(String name);

    // 6. JpaRepository를 상속받았기 때문에, 기본적인 CRUD 메소드들
    // (예: save(), findById(), findAll(), deleteById(), count(), existsById() 등)은
    // 우리가 직접 작성하지 않아도 바로 사용할 수 있습니다
    
}
