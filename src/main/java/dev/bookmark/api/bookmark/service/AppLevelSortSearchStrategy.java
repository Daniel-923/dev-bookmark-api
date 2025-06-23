package dev.bookmark.api.bookmark.service;

import dev.bookmark.api.bookmark.domain.Bookmark;
import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
import dev.bookmark.api.tag.dto.TagResponseDto;
import dev.bookmark.api.bookmark.repository.BookmarkRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DB에서 넓게 조회한 후, 애플리케이션 레벨에서 점수를 매겨 정렬하는 검색 전략입니다.
 */
@Slf4j
@Component  // 이 클래스를 스프링 빈으로 등록합니다. @Service와 유사합니다.
@Primary    // 여러 BookmarkSearchStrategy 구현체 중, 이것을 기본으로 사용하도록 설정합니다.
@RequiredArgsConstructor
public class AppLevelSortSearchStrategy implements BookmarkSearchStrategy {

    private final BookmarkRepository bookmarkRepository;

    @Override
    public Page<BookmarkResponseDto> search(String keyword, List<String> tagNames, Pageable pageable) {
        log.info("Searching with AppLevelSortSearchStrategy. Keyword: '{}', Tags: {}", keyword, tagNames);
        // 여기에 방법 B의 로직을 그대로 구현합니다.
        //    - DB에서 OR 조건으로 넓게 북마크 목록 조회 (bookmarkRepository.findByKeywordAndTags(...))
        //    - 조회된 결과를 바탕으로 점수 매기기 (calculateScore 헬퍼 메소드)
        //    - 점수 기준으로 재정렬하기
        //    - 최종 결과를 Page 객체로 만들어 반환하기


        // 1. 검색 조건이 유효한지 확인합니다.
        boolean keywordExists = StringUtils.hasText(keyword);
        boolean tagsExist = (tagNames != null && !tagNames.isEmpty());

        // 2. 아무 검색 조건도 없는 경우, 유효하지 않은 요청으로 간주하고 예외를 발생시킵니다.
        if (!keywordExists && !tagsExist) {
            log.warn("Search attempted without any criteria.");
            // 또는 return Page.empty(pageable); 로 빈 결과를 반환할 수도 있습니다.
            // 예외를 던지는 것이 클라이언트에게 더 명확한 피드백을 줍니다.
            throw new IllegalArgumentException("검색 조건(키워드 또는 태그)을 하나 이상 입력해주세요.");
        }

        // 3. 조건이 하나라도 있으면 커스텀 쿼리를 사용하여 조회합니다.
        log.debug("Search criteria present. Using custom query.");
        List<Bookmark> foundBookmarks = bookmarkRepository.findByKeywordOrTags(
                keywordExists ? keyword : null,
                tagsExist ? tagNames : null
        );


        // 각 북마크를 DTO로 변환하고 점수를 매깁니다.
        List<ScoredBookmarkDto> scoredList = foundBookmarks.stream()
                .map(bookmark -> {
                    BookmarkResponseDto dto = BookmarkResponseDto.fromEntity(bookmark);
                    int score = calculateScore(dto, keyword, tagNames);
                    return new ScoredBookmarkDto(dto, score);
                })
                .collect(Collectors.toList());

        // 점수가 높은 순서대로 재정렬합니다.
        scoredList.sort((o1, o2) -> {
            int scoreCompare = Integer.compare(o2.getScore(), o1.getScore()); // 점수 내림차순
            if (scoreCompare == 0) { // 점수가 같다면
                // 생성일 내림차순 (최신순)으로 2차 정렬
                return o2.getBookmark().getCreatedAt().compareTo(o1.getBookmark().getCreatedAt());
            }
            return scoreCompare;
        });

        // 정렬된 목록에서 순수한 DTO만 다시 리스트로 만듭니다.
        List<BookmarkResponseDto> sortedResult = scoredList.stream()
                .map(ScoredBookmarkDto::getBookmark)
                .collect(Collectors.toList());

        // 수동으로 페이징 처리하여 최종 결과를 만듭니다.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedResult.size());

        // start가 리스트 크기를 넘어서는 경우, 빈 리스트를 포함한 페이지 반환
        List<BookmarkResponseDto> pageContent = (start > sortedResult.size()) ? Collections.emptyList() : sortedResult.subList(start, end);

        return new PageImpl<>(pageContent, pageable, sortedResult.size());
    }

    /**
     * 북마크 DTO와 검색 조건에 따라 점수를 계산하는 헬퍼 메소드
     */
    private int calculateScore(BookmarkResponseDto dto, String keyword, List<String> tagNames) {
        int score = 0;
        boolean keywordProvided = (keyword != null && !keyword.isBlank());
        boolean tagsProvided = (tagNames != null && !tagNames.isEmpty());

        // 키워드 점수
        boolean keywordMatch = false;
        if (keywordProvided && dto.getTitle().contains(keyword)) {
            keywordMatch = true;
            score += 20; // 제목에 포함되면 높은 점수
        } else if (keywordProvided && dto.getDescription() != null && dto.getDescription().contains(keyword)) {
            keywordMatch = true;
            score += 10; // 설명에 포함되면 중간 점수
        }

        // 태그 점수
        long tagMatchCount = 0;
        if (tagsProvided) {
            tagMatchCount = dto.getTags().stream()
                    .map(TagResponseDto::getName)
                    .filter(tagName -> tagNames.stream().anyMatch(reqTag -> reqTag.equalsIgnoreCase(tagName)))
                    .count();

            if (tagMatchCount == tagNames.size()) { // 모든 요청 태그와 일치 (AND 조건 만족)
                score += 100; // 매우 높은 보너스 점수
            }
            score += tagMatchCount * 5; // 일치하는 태그 수만큼 점수 추가
        }

        // 키워드와 태그 모두 일치 시 추가 보너스 점수
        if (keywordMatch && tagMatchCount > 0) {
            score += 30;
        }

        return score;
    }

    /**
     * 점수와 북마크 DTO를 함께 관리하기 위한 내부 헬퍼 클래스
     */
    @Getter
    private static class ScoredBookmarkDto {
        private final BookmarkResponseDto bookmark;
        private final int score;

        public ScoredBookmarkDto(BookmarkResponseDto bookmark, int score) {
            this.bookmark = bookmark;
            this.score = score;
        }
    }


}
