package dev.bookmark.api.folder.repository; // 1. 패키지 선언

import dev.bookmark.api.folder.domain.Folder; // 2. Folder 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository; // 3. JpaRepository 임포트

import java.util.List;
import java.util.Optional;

// 4. JpaRepository<엔티티 클래스, 엔티티의 ID 필드 타입> 인터페이스를 상속받습니다.
public interface FolderRepository extends JpaRepository<Folder, Long> {

    // 5. 특정 부모 폴더(parentFolder) 내에서 같은 이름(name)의 폴더가 있는지 찾는 쿼리 메소드
    // 폴더 생성/수정 시 이름 중복 체크에 사용됩니다.
    Optional<Folder> findByNameAndParentFolder(String name, Folder parentFolder);

    // 6. 부모 폴더가 없는(최상위) 폴더들 중에서 같은 이름(name)의 폴더가 있는지 찾는 쿼리 메소드
    // 최상위 폴더 생성/수정 시 이름 중복 체크에 사용됩니다.
    Optional<Folder> findByNameAndParentFolderIsNull(String name);

    // 7. 특정 부모 폴더 ID를 가진 모든 하위 폴더들을 찾는 쿼리 메소드
    // 특정 폴더의 하위 목록을 보여줄 때 사용됩니다.
    List<Folder> findByParentFolder_Id(Long parentFolderId);
    // 또는 부모 폴더 객체 자체로도 검색 가능합니다.
    // List<Folder> findByParentFolder(Folder parentFolder);

    // 8. 부모 폴더가 없는 (parentFolder 필드가 null인) 최상위 폴더들을 찾는 쿼리 메소드
    List<Folder> findByParentFolderIsNull();

    // JpaRepository를 상속받았으므로, 기본적인 CRUD 메소드들은 이미 사용 가능합니다.
    // 예: save(Folder folder), findById(Long id), findAll(), deleteById(Long id) 등

    // 특정 폴더를 부모로 하는 하위 폴더가 하나라도 존재하는지 확인하는 메소드
    boolean existsByParentFolder(Folder parentFolder);
}