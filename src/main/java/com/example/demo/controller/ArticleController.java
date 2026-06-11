package com.example.demo.controller;

import com.example.demo.dto.ArticleDto;
import com.example.demo.dto.ArticleForm;
import com.example.demo.model.MemberUser;
import com.example.demo.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final ArticleService articleService;

    // 1. 전체 목록 조회 (페이징 없음)
    @GetMapping("/list-without-pagination")
    public String getArticleList(Model model) {
        List<ArticleDto> articles = articleService.findAllWithoutPagination();
        model.addAttribute("articles", articles);
        return "article-list-without-pagination";
    }

    // 2. 전체 목록 조회 (페이징 적용)
    @GetMapping("/list")
    public String getArticleList(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        Page<ArticleDto> page = articleService.findAll(pageable);
        model.addAttribute("page", page);
        return "article-list";
    }

    // 3. 게시글 상세 보기
    @GetMapping("/content")
    public String getArticle(@RequestParam("id") Long id, Model model) {
        model.addAttribute("article", articleService.findById(id));
        return "article-content";
    }

    // 4. 게시글 입력 폼 요청
    @GetMapping("/add")
    public String getArticleAdd(@ModelAttribute("article") ArticleForm articleForm) {
        articleForm.setDescription("바르고 고운말을 사용하여 주세요^^");
        return "article-add";
    }

    // 5. 게시글 저장 (비속어 검증 및 로그인 체크 추가)
    @PostMapping("/add")
    public String postArticleAdd(@Valid @ModelAttribute("article") ArticleForm articleForm,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal MemberUser memberUser) {

        // 비로그인 사용자가 글 작성을 시도한 경우 처리 (NullPointerException 방지)
        if (memberUser == null) {
            return "redirect:/login"; 
        }

        // 비속어 필터링 검증
        if (articleForm.getTitle() != null && articleForm.getTitle().contains("T발")) {
            bindingResult.rejectValue("title", "SlangDetected", "욕설을 사용하지 마세요");
        }
        if (articleForm.getDescription() != null && articleForm.getDescription().contains("T발")) {
            bindingResult.rejectValue("description", "SlangDetected", "욕설을 사용하지 마세요");
        }

        // 유효성 검증 실패 시 입력 폼으로 리턴
        if (bindingResult.hasErrors()) {
            return "article-add";
        }

        articleService.create(memberUser.getId(), articleForm);
        return "redirect:/article/list";
    }

    // 6. 게시글 수정 폼 요청 (id 유효성 검증 추가)
    @GetMapping("/edit")
    public String getArticleEdit(@ModelAttribute("article") ArticleForm articleForm) {
        // url에 id 파라미터가 없거나 비어있을 경우 예외 처리
        if (articleForm.getId() == null) {
            return "redirect:/article/list";
        }

        ArticleDto articleDto = articleService.findById(articleForm.getId());
        articleForm.setId(articleDto.getId());
        articleForm.setTitle(articleDto.getTitle());
        articleForm.setDescription(articleDto.getDescription());
        return "article-edit";
    }

    // 7. 게시글 수정 저장 (로그인 체크 추가)
    @PostMapping("/edit")
    public String postArticleEdit(@Valid @ModelAttribute("article") ArticleForm articleForm,
                                  BindingResult bindingResult,
                                  @AuthenticationPrincipal MemberUser userDetails) throws BadRequestException {
        
        // 비로그인 사용자가 수정을 시도한 경우 처리 (NullPointerException 방지)
        if (userDetails == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "article-edit";
        }
        
        articleService.update(userDetails.getId(), articleForm);
        return "redirect:/article/content?id=" + articleForm.getId();
    }

    // 8. 게시글 삭제
    @GetMapping("/delete")
    public String getArticleDelete(@RequestParam("id") Long id) {
        articleService.delete(id);
        return "redirect:/article/list";
    }
}