package dev.bookmark.api.bookmark.service;

import dev.bookmark.api.bookmark.domain.Bookmark;
import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
import dev.bookmark.api.bookmark.repository.BookmarkRepository;
import dev.bookmark.api.folder.domain.Folder;
import dev.bookmark.api.tag.domain.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*; // JUnit의 Assertions
import static org.assertj.core.api.Assertions.assertThat; // AssertJ의 Assertions (더 가독성 좋음)
import static org.mockito.ArgumentMatchers.any; // Mockito에서 어떤 값이든 매칭
import static org.mockito.Mockito.when; // Mockito의 when()
import org.springframework.data.domain.PageImpl;

/**
 * AppLevelSortSearchStrategy 클래스에 대한 단위 테스트
 */
@ExtendWith(MockitoExtension.class) // 1. 이 테스트에서 Mockito를 사용하겠다고 JUnit5에게 알림
class AppLevelSortSearchStrategyTest {

    @Mock // 2. 가짜 배우(Mock) 생성: DB에 접근하지 않는 가짜 BookmarkRepository
    private BookmarkRepository bookmarkRepository;

    @InjectMocks // 3. 테스트 대상 객체 생성: 위에서 만든 @Mock 객체들을 이 객체에 자동으로 주입
    private AppLevelSortSearchStrategy searchStrategy;

    private Folder folder_tech;
    private Tag tag_java, tag_spring;
    private Bookmark bookmark_jpa, bookmark_spring_basic;

    @BeforeEach // 4. 각 @Test 메소드가 실행되기 전에 항상 먼저 실행되는 설정 메소드
    void setUp() {
        // 테스트에 사용할 공통 데이터 미리 생성
        folder_tech = Folder.builder().name("기술").build();
        tag_java = Tag.builder().name("Java").build();
        tag_spring = Tag.builder().name("Spring").build();

        bookmark_jpa = Bookmark.builder()
                .title("JPA 핵심")
                .url("http://example.com/jpa")
                .folder(folder_tech)
                .build();
        bookmark_jpa.addTag(tag_java);
        bookmark_jpa.addTag(tag_spring);

        bookmark_spring_basic = Bookmark.builder()
                .title("Spring 기초")
                .url("http://example.com/spring")
                .folder(folder_tech)
                .build();
        bookmark_spring_basic.addTag(tag_spring);
    }

    @Test
    @DisplayName("키워드와 태그로 검색 시, 모든 조건에 맞는 북마크가 최상단에 정렬되어야 한다")
    void search_withKeywordAndTags_shouldReturnSortedByScore() {
        // === Given (준비): 테스트 상황 설정 ===

        // 1. 검색 조건 정의
        String keyword = "JPA";
        List<String> tagNames = List.of("Java", "Spring");
        Pageable pageable = PageRequest.of(0, 10);

        // 2. Mock Repository의 동작 정의
        // "만약 bookmarkRepository.findByKeywordOrTags가 이런 파라미터로 호출되면,
        //  미리 만들어둔 북마크 리스트를 반환해줘" 라고 시나리오를 짬.
        when(bookmarkRepository.findByKeywordOrTags(keyword, tagNames))
                .thenReturn(List.of(bookmark_jpa, bookmark_spring_basic));


        // === When (실행): 실제 테스트할 메소드 호출 ===
        Page<BookmarkResponseDto> resultPage = searchStrategy.search(keyword, tagNames, pageable);


        // === Then (검증): 결과가 예상과 일치하는지 확인 ===

        // 1. 기본적인 검증 (Assertions 예시)
        assertNotNull(resultPage, "결과 페이지는 null이 아니어야 합니다."); // 결과가 null이 아닌지
        assertEquals(2, resultPage.getTotalElements(), "전체 요소의 수는 2개여야 합니다."); // 전체 결과 개수 확인
        assertFalse(resultPage.isEmpty(), "결과 페이지는 비어있지 않아야 합니다."); // 결과가 비어있지 않은지

        List<BookmarkResponseDto> content = resultPage.getContent();
        assertEquals(2, content.size(), "현재 페이지의 콘텐츠 수는 2개여야 합니다."); // 현재 페이지의 결과 개수 확인

        // 2. 핵심 로직 검증: 정렬 순서 확인 (AssertJ 사용 - 가독성이 더 좋음)
        // 'JPA' 키워드와 'Java', 'Spring' 태그를 모두 만족하는 'bookmark_jpa'가 더 높은 점수를 받아야 함.
        // 따라서 결과 리스트의 첫 번째 요소는 'JPA 핵심' 이어야 함.
        assertThat(content.get(0).getTitle()).isEqualTo("JPA 핵심");
        assertThat(content.get(1).getTitle()).isEqualTo("Spring 기초");

        // 3. (추가 예시) 예외 발생 테스트 (별도의 테스트 메소드에서)
        // 이 테스트는 현재 테스트 케이스와는 별개로, 예외 상황을 테스트하는 방법의 예시입니다.
        // assertThrows(예상되는_예외_클래스, 예외를_발생시키는_코드_블록, 예외_메시지_검증);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            searchStrategy.search(null, null, pageable); // 아무 조건 없이 검색 시도
        });
        assertEquals("검색 조건(키워드 또는 태그)을 하나 이상 입력해주세요.", exception.getMessage());
    }


    @Test
    @DisplayName("키워드로만 검색 시, 제목이나 설명에 키워드가 포함된 북마크만 반환되어야 한다")
    void search_withKeywordOnly_shouldReturnMatchingBookmarks() {
        // === Given (준비) ===
        String keyword = "기초";
        Pageable pageable = PageRequest.of(0, 10);

        // "기초"라는 단어는 bookmark_spring_basic에만 포함됨
        when(bookmarkRepository.findByKeywordOrTags(keyword, null))
                .thenReturn(List.of(bookmark_spring_basic));

        // === When (실행) ===
        Page<BookmarkResponseDto> resultPage = searchStrategy.search(keyword, null, pageable);

        // === Then (검증) ===
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getTitle()).isEqualTo("Spring 기초");
    }

    @Test
    @DisplayName("태그로만 검색 시, 해당 태그를 가진 북마크만 반환되어야 한다")
    void search_withTagOnly_shouldReturnMatchingBookmarks() {
        // === Given (준비) ===
        // "Java" 태그로 검색하는 시나리오
        List<String> tagNames = List.of("Java");
        Pageable pageable = PageRequest.of(0, 10);

        // "Java" 태그를 가진 bookmark_jpa를 반환하도록 Mock 설정
        when(bookmarkRepository.findByKeywordOrTags(null, tagNames))
                .thenReturn(List.of(bookmark_jpa));

        // === When (실행) ===
        Page<BookmarkResponseDto> resultPage = searchStrategy.search(null, tagNames, pageable);

        // === Then (검증) ===

        assertThat(resultPage.getTotalElements()).isEqualTo(1); // 전체 결과는 1개
        assertThat(resultPage.getContent()).hasSize(1); // 현재 페이지의 결과도 1개

        // 결과의 첫 번째(인덱스 0) 북마크의 제목이 "JPA 핵심"인지 확인
        assertThat(resultPage.getContent().get(0).getTitle()).isEqualTo("JPA 핵심");

        // 결과의 첫 번째 북마크가 "Java" 태그를 포함하는지 확인
        assertThat(resultPage.getContent().get(0).getTags())
                .extracting("name")
                .contains("Java");
    }


    @Test
    @DisplayName("아무 검색 조건 없이 검색하면 IllegalArgumentException이 발생해야 한다")
    void no_search_shouldReturnEmptyList() {
        // === Given (준비) ===
        Pageable pageable = PageRequest.of(0, 10);

        // === When & Then (실행 및 검증) ===
        // searchStrategy.search(null, null, pageable) 코드를 실행했을 때,
        // IllegalArgumentException.class 타입의 예외가 발생하는 것을 기대하고,
        // 발생한 예외 객체를 'exception' 변수에 담는다.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            searchStrategy.search(null, null, pageable);
        });

        // 발생한 예외의 메시지가 우리가 의도한 메시지와 일치하는지 추가로 검증한다.
        assertThat(exception.getMessage()).isEqualTo("검색 조건(키워드 또는 태그)을 하나 이상 입력해주세요.");
    }


    @Test
    @DisplayName("결과가 없는 키워드로 검색 시, 빈 페이지를 반환해야 한다")
    void search_withNonExistingKeyword_shouldReturnEmptyPage() {
        // === Given (준비) ===
        String keyword = "이런키워드는절대없을거야12345";
        Pageable pageable = PageRequest.of(0, 10);

        // Mock Repository는 이 조건으로 조회 시 빈 리스트를 반환하도록 설정
        when(bookmarkRepository.findByKeywordOrTags(keyword, null))
                .thenReturn(Collections.emptyList());

        // === When (실행) ===
        Page<BookmarkResponseDto> resultPage = searchStrategy.search(keyword, null, pageable);

        // === Then (검증) ===
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(0);
        assertThat(resultPage.getContent()).isEmpty();
    }


    @Test
    @DisplayName("점수가 동일할 경우, 최신 생성된 북마크가 먼저 정렬되어야 한다")
    void search_whenScoresAreEqual_shouldSortByCreationDateDescending() {
        // === Given (준비) ===
        String keyword = "동일 점수 테스트";
        Pageable pageable = PageRequest.of(0, 10);

        // 1. 점수가 동일하도록, 생성 시간만 다른 두 개의 북마크 생성
        Bookmark olderBookmark = Bookmark.builder().title(keyword).url("url1").folder(folder_tech).build();
        // 자바 리플렉션을 통해 private 필드인 createdAt에 값을 설정 (테스트를 위한 기술)
        // 또는 createdAt을 설정할 수 있는 생성자나 메소드를 엔티티에 추가할 수도 있습니다.
        setPrivateField(olderBookmark, "id", 1L);
        setPrivateField(olderBookmark, "createdAt", LocalDateTime.now().minusDays(1)); // 어제 생성

        Bookmark newerBookmark = Bookmark.builder().title(keyword).url("url2").folder(folder_tech).build();
        setPrivateField(newerBookmark, "id", 2L);
        setPrivateField(newerBookmark, "createdAt", LocalDateTime.now()); // 오늘 생성

        // 2. Mock Repository가 두 북마크를 순서 섞어서 반환하도록 설정
        when(bookmarkRepository.findByKeywordOrTags(keyword, null))
                .thenReturn(List.of(olderBookmark, newerBookmark));

        // === When (실행) ===
        Page<BookmarkResponseDto> resultPage = searchStrategy.search(keyword, null, pageable);

        // === Then (검증) ===
        List<BookmarkResponseDto> content = resultPage.getContent();
        assertThat(content).hasSize(2); // 결과는 2개여야 함

        // 3. 최신 북마크(newerBookmark)가 첫 번째(인덱스 0)에 오는지 확인
        assertThat(content.get(0).getId()).isEqualTo(newerBookmark.getId());
        assertThat(content.get(1).getId()).isEqualTo(olderBookmark.getId());
    }

    // 테스트를 위해 private 필드에 값을 설정하는 헬퍼 메소드
    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    @DisplayName("페이징 로직이 올바르게 동작해야 한다")
    void search_withPaging_shouldReturnCorrectPageInfo() {
        // === Given (준비) ===
        // 1. 12개의 테스트용 북마크 생성
        List<Bookmark> mockBookmarks = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Bookmark bookmark = Bookmark.builder().title("Bookmark " + i).url("url" + i).folder(folder_tech).build();
            setPrivateField(bookmark, "id", (long)i);
            // ⭐ 이 부분을 추가하여 createdAt 필드에 실제 시간 값을 설정합니다.
            setPrivateField(bookmark, "createdAt", LocalDateTime.now().minusHours(i));
            mockBookmarks.add(bookmark);
        }

        // 2. Mock Repository가 12개의 북마크를 반환하도록 설정
        when(bookmarkRepository.findByKeywordOrTags(any(), any()))
                .thenReturn(mockBookmarks);

        // 3. "두 번째 페이지, 페이지당 5개" 요청
        Pageable pageable = PageRequest.of(1, 5);

        // === When (실행) ===
        Page<BookmarkResponseDto> resultPage = searchStrategy.search("someKeyword", null, pageable);

        // === Then (검증) ===
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(12); // 전체 아이템 수
        assertThat(resultPage.getTotalPages()).isEqualTo(3);    // 전체 페이지 수
        assertThat(resultPage.getNumber()).isEqualTo(1);          // 현재 페이지 번호 (0부터 시작)
        assertThat(resultPage.getContent()).hasSize(5);           // 현재 페이지의 아이템 수

        // (선택) 현재 페이지의 첫 번째 아이템이 예상과 맞는지 확인 (6번째 북마크, 인덱스 5)
        assertThat(resultPage.getContent().get(0).getTitle()).isEqualTo("Bookmark 5");
    }



}