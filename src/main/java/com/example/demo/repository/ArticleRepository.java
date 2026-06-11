package com.example.demo.repository;

import java.lang.reflect.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Article;

import jakarta.transaction.Transactional;

public interface ArticleRepository extends JpaRepository<Article, Long>{
	@Transactional
	void deleteAllByMember(Member member);

}
