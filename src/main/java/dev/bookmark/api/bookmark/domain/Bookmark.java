package dev.bookmark.api.bookmark.domain;

import dev.bookmark.api.folder.domain.Folder;
import dev.bookmark.api.tag.domain.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "bookmarks") // 데이터베이스 테이블 이름을 "bookmarks"로 지정
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id; // 북마크의 고유 ID

    @Column(name = "title", nullable = false, length = 255) // 제목, null 불가, 길이 255
    private String title;

    @Column(name = "url", nullable = false, length = 2083) // URL, null 불가, 일반적인 URL 최대 길이 고려
    private String url;

    @Column(name = "description", length = 1000) // 2. 대신 @Column의 length 속성으로 길이를 지정합니다. (예: 1000자)
    private String description; // 북마크 설명

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Folder와의 다대일(N:1) 관계 설정
    @ManyToOne(fetch = FetchType.LAZY) // 하나의 폴더는 여러 북마크를 가질 수 있음 (북마크 입장에서는 하나의 폴더에 속함)
    @JoinColumn(name = "folder_id", nullable = false) // 외래 키 컬럼 이름은 "folder_id", null 불가 (모든 북마크는 폴더에 속해야 함)
    private Folder folder; // 이 북마크가 속한 폴더


    // 10. Tag와의 다대다(N:M) 관계 설정
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // 하나의 북마크는 여러 태그를, 하나의 태그는 여러 북마크에 사용될 수 있음
    @JoinTable(
            name = "bookmark_tags", // 11. 중간 연결 테이블의 이름
            joinColumns = @JoinColumn(name = "bookmark_id"), // 12. Bookmark_Tags 테이블에서 Bookmark를 참조하는 외래 키
            inverseJoinColumns = @JoinColumn(name = "tag_id") // 13. Bookmark_Tags 테이블에서 Tag를 참조하는 외래 키
    )
    private Set<Tag> tags = new HashSet<>(); // 14. 이 북마크에 연결된 태그들 (중복을 허용하지 않기 위해 Set 사용)

    @Builder
    public Bookmark(String title, String url, String description, Folder folder) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.folder = folder;
        // tags는 북마크 생성 후 별도로 추가/관리하는 것이 일반적입니다.
    }

    //== 연관관계 편의 메소드 ==//
    // Folder 설정 (양방향 관계 시 Folder 엔티티에도 addBookmark 와 같은 메소드가 있을 수 있음)
    public void setFolder(Folder folder) {
        this.folder = folder;
        // 만약 Folder 엔티티에서 북마크 목록을 관리한다면, 여기서 folder.getBookmarks().add(this); 와 같은 코드도 필요할 수 있음
    }

    // Tag 추가
    public void addTag(Tag tag) {
        this.tags.add(tag);
        // 만약 Tag 엔티티에서 북마크 목록을 관리한다면, 여기서 tag.getBookmarks().add(this); 와 같은 코드도 필요할 수 있음
    }

    // Tag 제거
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        // 만약 Tag 엔티티에서 북마크 목록을 관리한다면, 여기서 tag.getBookmarks().remove(this); 와 같은 코드도 필요할 수 있음
    }

    // 모든 Tags 제거 (북마크 수정 시 태그 전체 교체 등에 사용 가능)
    public void clearTags() {
        this.tags.clear();
    }

    /**
     * 북마크의 제목, URL, 설명을 업데이트합니다.
     * 각 파라미터가 null이 아니고 비어있지 않은 경우에만 해당 필드를 업데이트합니다.
     * @param title 새로운 제목 (선택 사항)
     * @param url 새로운 URL (선택 사항)
     * @param description 새로운 설명 (선택 사항)
     */
    public void updateDetails(String title, String url, String description) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (url != null && !url.isBlank()) {
            this.url = url;
        }
        // description은 null이나 빈 문자열로도 업데이트될 수 있도록 허용할 수 있습니다.
        // 만약 null이거나 비어있지 않을 때만 업데이트하고 싶다면 위와 유사한 조건을 추가합니다.
        if (description != null) { // description은 null로도 업데이트 가능하도록
            this.description = description;
        }
    }
}
