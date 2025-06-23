package dev.bookmark.api.config;

import dev.bookmark.api.bookmark.domain.Bookmark;
import dev.bookmark.api.bookmark.repository.BookmarkRepository;
import dev.bookmark.api.folder.domain.Folder;
import dev.bookmark.api.folder.repository.FolderRepository;
import dev.bookmark.api.tag.domain.Tag;
import dev.bookmark.api.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 개발 환경에서 애플리케이션 시작 시 초기 데이터를 생성합니다.
 */
@Slf4j
@Component // 1. 이 클래스를 스프링 빈으로 등록합니다.
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner { // 2. CommandLineRunner 인터페이스 구현

    private final FolderRepository folderRepository;
    private final TagRepository tagRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    @Transactional // 3. 데이터 변경 작업을 하나의 트랜잭션으로 묶습니다.
    public void run(String... args) throws Exception {
        log.info("Checking for initial data...");

        // 4. 이미 폴더 데이터가 있으면, 초기 데이터를 생성하지 않고 종료합니다.
        if (folderRepository.count() > 0) {
            log.info("Data already exists. Skipping initialization.");
            return;
        }

        log.info("Initializing test data...");

        // 5. 태그 생성
        Tag tagJava = tagRepository.save(Tag.builder().name("Java").build());
        Tag tagSpring = tagRepository.save(Tag.builder().name("Spring").build());
        Tag tagJpa = tagRepository.save(Tag.builder().name("JPA").build());
        Tag tagProjectA = tagRepository.save(Tag.builder().name("프로젝트A").build());
        Tag tagTravel = tagRepository.save(Tag.builder().name("여행").build());
        Tag tagPlanning = tagRepository.save(Tag.builder().name("기획").build());

        // 6. 폴더 생성 (계층 구조)
        Folder folderWork = folderRepository.save(Folder.builder().name("업무").parentFolder(null).build());
        Folder folderPersonal = folderRepository.save(Folder.builder().name("개인").parentFolder(null).build());

        Folder folderProjectA = folderRepository.save(Folder.builder().name("프로젝트A").parentFolder(folderWork).build());
        Folder folderProjectB = folderRepository.save(Folder.builder().name("프로젝트B").parentFolder(folderWork).build());

        Folder folderPlan = folderRepository.save(Folder.builder().name("기획").parentFolder(folderProjectA).build());
        Folder folderDev = folderRepository.save(Folder.builder().name("개발").parentFolder(folderProjectA).build());

        // 7. 북마크 생성
        Bookmark bm1 = Bookmark.builder()
                .title("초기 기획서")
                .url("https://example.com/projectA/plan")
                .description("프로젝트A 초기 기획 내용")
                .folder(folderPlan)
                .build();
        bm1.addTag(tagPlanning);
        bm1.addTag(tagProjectA);
        bookmarkRepository.save(bm1);

        Bookmark bm2 = Bookmark.builder()
                .title("Spring Data JPA 공식 문서")
                .url("https://spring.io/projects/spring-data-jpa")
                .description("JPA 관련 핵심 기술 자료")
                .folder(folderDev)
                .build();
        bm2.addTag(tagSpring);
        bm2.addTag(tagJpa);
        bm2.addTag(tagJava);
        bookmarkRepository.save(bm2);

        Bookmark bm3 = Bookmark.builder()
                .title("여름 휴가 계획")
                .url("https://example.com/vacation")
                .description("숙소, 교통편 예약하기")
                .folder(folderPersonal)
                .build();
        bm3.addTag(tagTravel);
        bookmarkRepository.save(bm3);

        log.info("Test data initialization completed.");
    }
}
