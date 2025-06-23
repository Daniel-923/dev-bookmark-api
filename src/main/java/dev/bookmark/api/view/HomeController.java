package dev.bookmark.api.view;

import dev.bookmark.api.bookmark.dto.BookmarkResponseDto;
import dev.bookmark.api.bookmark.service.BookmarkService;
import dev.bookmark.api.folder.dto.FolderTreeResponseDto;
import dev.bookmark.api.folder.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Thymeleaf 뷰를 반환하기 위한 컨트롤러입니다.
 */
@Controller // 1. @RestController가 아닌 @Controller를 사용합니다.
@RequiredArgsConstructor
public class HomeController {

    private final FolderService folderService; // 2. 폴더 트리 데이터를 가져오기 위해 FolderService를 주입받습니다.
    private final BookmarkService bookmarkService;

    /**
     * 메인 페이지를 요청받아 폴더 트리 데이터를 모델에 담아 뷰로 전달합니다.
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return 렌더링할 뷰(HTML 파일)의 이름
     */
    @GetMapping("/") // 3. 사용자가 웹 브라우저에서 루트 경로("/")를 요청했을 때 이 메소드가 실행됩니다.
    public String home(Model model) {
        // 4. FolderService를 통해 폴더 트리 데이터를 조회합니다.
        List<FolderTreeResponseDto> folderTree = folderService.getFolderTree();

        // 5. Model 객체에 "rootFolders"라는 이름으로 폴더 트리 데이터를 담습니다.
        // 이제 HTML 파일에서는 'rootFolders'라는 이름으로 이 데이터에 접근할 수 있습니다.
        model.addAttribute("rootFolders", folderTree);

        // 6. "home"이라는 이름의 뷰를 반환합니다.
        // Spring Boot는 'src/main/resources/templates/home.html' 파일을 찾아 렌더링합니다.
        return "home";
    }


    @GetMapping("/search") // 1. /search 경로의 GET 요청을 처리합니다.
    public String searchBookmarks(
            @RequestParam(name = "keyword", required = false) String keyword, // 2. 'keyword' 쿼리 파라미터를 받습니다.
            @RequestParam(name = "tags", required = false) List<String> tagNames,    // 2. 'tags' 쿼리 파라미터를 받습니다.
            Pageable pageable, // 3. 페이징 파라미터(page, size, sort)를 받습니다.
            Model model        // 4. HTML로 데이터를 전달할 모델 객체입니다.
    ) {
        // 5. BookmarkService의 검색 메소드를 호출합니다.
        Page<BookmarkResponseDto> searchResultPage = bookmarkService.searchBookmarks(keyword, tagNames, pageable);

        // 6. 모델에 검색 결과를 담아 뷰로 전달합니다.
        model.addAttribute("searchResults", searchResultPage);
        // (선택) 사용자가 검색한 키워드나 태그를 화면에 다시 보여주기 위해 모델에 담을 수도 있습니다.
        model.addAttribute("keyword", keyword);
        model.addAttribute("tags", tagNames);

        // 7. 'search-result'라는 이름의 뷰(HTML 파일)를 반환합니다.
        return "search-result";
    }




}
