package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/article")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
	private final ArticleService articleService;
	
	@GetMapping("/list-without-paination")
	public String getArticleList(Model model) {
		List<ArticleDto> articles = articleService.findAlloWithoutPagination();
		model.addAttribute("articles", articles);
		return article-list-without-pagination";
    
	}
	
	// Pageable http://localhost:8080/article/list?page=0.size=10&sort=id.as&sort=name, desc
	// @PageableDefault(page, size, sort, direxction)-경우 sort는 하나만 가능
	// 여러개의 Sort 옵션을 전달하려면 @PageableDefault로는 page와 size만 전달하고 추가로 @SortDefault.SortDefaults()를 전달
//	@GetMapping("/list")
// public String getArticleList(@PageableDafault(page = 0, size = 10)
//	                            @SortDefault.SortDefaults({
//	                            	@SortDefault(sort="id"
//	                            	@SortDefault
	                            })

}
