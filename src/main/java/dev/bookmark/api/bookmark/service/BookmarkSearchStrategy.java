package dev.bookmark.api.bookmark.service;

import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 * 북마크 검색 전략을 위한 인터페이스입니다.
 * 모든 구체적인 검색 전략 클래스는 이 인터페이스를 구현해야 합니다.
 */
public interface BookmarkSearchStrategy {
    /**
     * 주어진 조건으로 북마크를 검색하고 정렬하여 페이징된 결과를 반환합니다.
     *
     * @param keyword 검색할 키워드 (제목, 설명 등)
     * @param tagNames 검색할 태그 이름 목록
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징 처리된 북마크 DTO 목록
     */
    Page<BookmarkResponseDto> search(String keyword, List<String> tagNames, Pageable pageable);
}