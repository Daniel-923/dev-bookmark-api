package dev.bookmark.api.bookmark.service; // 1. 패키지 선언

import dev.bookmark.api.bookmark.domain.Bookmark; // 2. 관련 엔티티, DTO, 리포지토리 임포트
import dev.bookmark.api.bookmark.dto.BookmarkCreateRequestDto;
import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
import dev.bookmark.api.bookmark.dto.BookmarkUpdateRequestDto; // 나중에 북마크 수정 시 필요
import dev.bookmark.api.bookmark.repository.BookmarkRepository;
import dev.bookmark.api.folder.domain.Folder;
import dev.bookmark.api.folder.repository.FolderRepository;
import dev.bookmark.api.tag.domain.Tag;
import dev.bookmark.api.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // 페이징 처리
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository; // Folder 존재 여부 확인을 위해 필요
    private final TagRepository tagRepository;       // Tag 처리(조회 또는 생성)를 위해 필요

    /**
     * 새로운 북마크를 생성합니다.
     * 요청된 folderId로 폴더를 확인하고, tagNames로 태그를 찾아 연결하거나 새로 생성합니다.
     * @param requestDto 북마크 생성 요청 데이터
     * @return 생성된 북마크 정보
     */
    @Transactional
    public BookmarkResponseDto createBookmark(BookmarkCreateRequestDto requestDto) {
        // 1. Folder 엔티티 조회 (존재하지 않으면 예외 발생)
        Folder folder = folderRepository.findById(requestDto.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("지정한 폴더를 찾을 수 없습니다. ID: " + requestDto.getFolderId()));

        // 2. Tag 엔티티 처리 (기존 태그 조회 또는 새 태그 생성)
        Set<Tag> tagsToAssociate = new HashSet<>();
        if (requestDto.getTagNames() != null && !requestDto.getTagNames().isEmpty()) {
            for (String tagName : requestDto.getTagNames()) {
                // 태그 이름으로 기존 태그를 찾거나, 없으면 새로 생성하여 저장
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                tagsToAssociate.add(tag);
            }
        }

        // 3. Bookmark 엔티티 생성
        Bookmark newBookmark = Bookmark.builder()
                .title(requestDto.getTitle())
                .url(requestDto.getUrl())
                .description(requestDto.getDescription())
                .folder(folder) // 조회한 Folder 엔티티 설정 - 1번 참고
                .build();

        // 4. 생성된 Bookmark 엔티티에 Tag 들을 연결 (연관관계 편의 메소드 사용)
        for (Tag tag : tagsToAssociate) {
            newBookmark.addTag(tag); // Bookmark 엔티티 내 addTag 메소드 호출
        }

        // 5. Bookmark 엔티티 저장 (이때 @ManyToMany 관계에 따라 bookmark_tags 테이블에도 데이터 삽입됨)
        Bookmark savedBookmark = bookmarkRepository.save(newBookmark);

        // 6. 저장된 엔티티를 응답 DTO로 변환하여 반환
        return BookmarkResponseDto.fromEntity(savedBookmark);
    }

    /**
     * ID로 특정 북마크를 조회합니다.
     * @param bookmarkId 조회할 북마크의 ID
     * @return 조회된 북마크 정보
     */
    @Transactional(readOnly = true)
    public BookmarkResponseDto getBookmarkById(Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 북마크를 찾을 수 없습니다. ID: " + bookmarkId));
        return BookmarkResponseDto.fromEntity(bookmark);
    }

    /**
     * 특정 폴더에 속한 모든 북마크를 페이징 처리하여 조회합니다.
     * @param folderId 북마크를 조회할 폴더의 ID
     * @param pageable 페이징 정보 (페이지 번호, 페이지당 개수, 정렬 등)
     * @return 페이징 처리된 북마크 정보 목록
     */
    @Transactional(readOnly = true)
    public Page<BookmarkResponseDto> getBookmarksByFolder(Long folderId, Pageable pageable) {
        // 먼저 폴더가 존재하는지 확인 (선택적이지만 안전)
        if (!folderRepository.existsById(folderId)) {
            throw new IllegalArgumentException("지정한 폴더를 찾을 수 없습니다. ID: " + folderId);
        }
        Page<Bookmark> bookmarksPage = bookmarkRepository.findByFolder_Id(folderId, pageable);
        return bookmarksPage.map(BookmarkResponseDto::fromEntity); // Page<Bookmark>를 Page<BookmarkResponseDto>로 변환
    }

    /**
     * 기존 북마크의 정보를 수정합니다.
     * 제목, URL, 설명, 소속 폴더, 태그 목록 등을 변경할 수 있습니다.
     * @param bookmarkId 수정할 북마크의 ID
     * @param requestDto 수정할 북마크 정보가 담긴 DTO
     * @return 수정된 북마크 정보
     */
    @Transactional
    public BookmarkResponseDto updateBookmark(Long bookmarkId, BookmarkUpdateRequestDto requestDto) {
        // 1. 수정할 Bookmark 엔티티 조회
        Bookmark bookmarkToUpdate = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 북마크를 찾을 수 없습니다. ID: " + bookmarkId));

        // 2. 기본 정보 업데이트 (제목, URL, 설명) - DTO의 값이 null이 아닐 때만 업데이트
        // Bookmark 엔티티 내부에 updateDetails 메소드가 있다고 가정합니다.
        bookmarkToUpdate.updateDetails(
                requestDto.getTitle(),
                requestDto.getUrl(),
                requestDto.getDescription()
        );

        // 3. 폴더 변경 처리 (요청 DTO에 folderId가 있고, 기존 폴더와 다를 경우)
        if (requestDto.getFolderId() != null &&
                (bookmarkToUpdate.getFolder() == null || !bookmarkToUpdate.getFolder().getId().equals(requestDto.getFolderId()))) {
            Folder newFolder = folderRepository.findById(requestDto.getFolderId())
                    .orElseThrow(() -> new IllegalArgumentException("새로운 소속 폴더를 찾을 수 없습니다. ID: " + requestDto.getFolderId()));
            bookmarkToUpdate.setFolder(newFolder); // Bookmark 엔티티의 setFolder 메소드 사용
        }

        // 4. 태그 변경 처리 (요청 DTO에 tagNames가 제공된 경우)
        if (requestDto.getTagNames() != null) {
            // a. 요청으로 들어온 태그 이름들로 실제 Tag 엔티티 Set 만들기 (없으면 생성)
            Set<Tag> newRequestedTags = new HashSet<>();
            if (!requestDto.getTagNames().isEmpty()) {
                for (String tagName : requestDto.getTagNames()) {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                    newRequestedTags.add(tag);
                }
            }

            // b. 현재 북마크에 연결된 태그 Set 가져오기 (수정을 위해 복사본 사용)
            Set<Tag> currentTags = new HashSet<>(bookmarkToUpdate.getTags()); // 복사본 생성

            // c. 제거할 태그 찾아서 제거하기 (현재 O, 요청 X)
            // currentTags에는 있는데 newRequestedTags에는 없는 태그들
            Set<Tag> tagsToRemove = new HashSet<>(currentTags);
            tagsToRemove.removeAll(newRequestedTags); // 차집합: currentTags - newRequestedTags
            for (Tag tagToRemove : tagsToRemove) {
                bookmarkToUpdate.removeTag(tagToRemove); // Bookmark 엔티티의 연관관계 편의 메소드 사용
            }

            // d. 추가할 태그 찾아서 추가하기 (현재 X, 요청 O)
            // newRequestedTags에는 있는데 currentTags에는 없는 태그들
            Set<Tag> tagsToAdd = new HashSet<>(newRequestedTags);
            tagsToAdd.removeAll(currentTags); // 차집합: newRequestedTags - currentTags
            for (Tag tagToAdd : tagsToAdd) {
                bookmarkToUpdate.addTag(tagToAdd); // Bookmark 엔티티의 연관관계 편의 메소드 사용
            }
        }

        // JPA의 변경 감지(Dirty Checking)에 의해 bookmarkToUpdate 객체의 변경사항이
        // (tags 컬렉션의 변경 포함) 트랜잭션 커밋 시 자동으로 반영됩니다.
        // 중간 테이블인 bookmark_tags에 대한 INSERT, DELETE SQL이 실행됩니다.

        return BookmarkResponseDto.fromEntity(bookmarkToUpdate); // 변경된 엔티티로 DTO 생성 후 반환
    }

    /**
     * 특정 ID의 북마크를 삭제합니다.
     * 북마크와 태그 간의 연결 정보도 함께 삭제됩니다. (Tag 엔티티 자체는 삭제되지 않음)
     * @param bookmarkId 삭제할 북마크의 ID
     */
    @Transactional
    public void deleteBookmark(Long bookmarkId) {
        // 1. 삭제할 북마크가 존재하는지 확인
        if (!bookmarkRepository.existsById(bookmarkId)) {
            throw new IllegalArgumentException("삭제하려는 북마크를 찾을 수 없습니다. ID: " + bookmarkId);
        }

        // 2. 북마크 삭제
        // JpaRepository의 deleteById를 사용하면 해당 ID의 엔티티를 삭제합니다.
        // @ManyToMany 관계에서 Bookmark 엔티티가 삭제될 때, 중간 테이블(bookmark_tags)에서 해당 북마크와 관련된 레코드들도
        // JPA에 의해 (또는 데이터베이스의 외래 키 제약조건 ON DELETE CASCADE 설정에 의해) 함께 삭제되는 것이 일반적입니다.
        // (Bookmark 엔티티의 tags 필드에 cascade=CascadeType.REMOVE 등을 설정하지 않는 것이 보통입니다.
        //  태그는 공유 자원이므로 북마크 삭제 시 태그까지 삭제되면 안 됩니다.)
        //  JPA는 관계의 주인이 아닌 쪽(또는 컬렉션에서 해당 엔티티를 제거하면) 연결 테이블 레코드를 정리합니다.
        bookmarkRepository.deleteById(bookmarkId);
    }







}