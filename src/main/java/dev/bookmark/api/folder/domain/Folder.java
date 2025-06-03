package dev.bookmark.api.folder.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
// import java.util.ArrayList; // 만약 자식 폴더 목록을 양방향으로 가진다면 필요
// import java.util.List;    // 만약 자식 폴더 목록을 양방향으로 가진다면 필요

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100) // unique = true 제거 (서비스에서 로직으로 처리 권장)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id") // 이 컬럼은 null을 허용해야 최상위 폴더를 표현 가능 (기본적으로 FK는 null 허용)
    private Folder parentFolder; // 부모 폴더 참조

    // 만약 Folder 엔티티가 자식 폴더 목록도 알아야 한다면 (양방향 관계)
    // @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Folder> childFolders = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Folder(String name, Folder parentFolder) {
        this.name = name;
        this.parentFolder = parentFolder;
    }

    // 이름 변경을 위한 메소드 (서비스 계층에서 호출)
    public void updateName(String newName) {
        if (newName != null && !newName.isBlank()) {
            this.name = newName;
        }
    }

    /**
     * 이 폴더의 부모 폴더를 변경합니다.
     * @param newParentFolder 새로운 부모 폴더 (최상위로 옮길 경우 null)
     */
    public void changeParentFolder(Folder newParentFolder) {
        // 여기에 추가적인 비즈니스 로직을 넣을 수 있습니다.
        // 예를 들어, 자기 자신을 부모로 설정하려는지,
        // 또는 자신의 하위 폴더를 부모로 설정하려는지 (순환 참조) 등을 검사할 수 있습니다.
        // (이러한 순환 참조 방지 로직은 서비스 계층에서 처리하는 것이 더 적절할 수도 있습니다.)
        this.parentFolder = newParentFolder;
    }
}