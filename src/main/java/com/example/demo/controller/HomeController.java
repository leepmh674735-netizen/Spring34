package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.dto.MemberForm;
import com.example.demo.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class HomeController {
	private final MemberService memberService;
	
	@GetMapping
	public String getHome() {
		//return "redirect:/article/list";
	    return "forward:/article/list";
    }
	
	@GetMapping("/login")
	public String getLogin() {
		return "login";
	}
	
	@GetMapping("/logout")
	public String getLogout() {
		return "logout";
	}
	
	@GetMapping("/signup")
	public String getMemberAdd(@ModelAttribute("member") MemberForm memberFrom) {
		return "signup";
	}
	
	
	@PostMapping("/signup")
	public String postMemberAdd(@Valid @ModelAttribute("member") MemberForm memberForm, BindingResult bindingResult) {
		if (memberForm.getPassword() == null || memberForm.getPassword().trim().length() < 8) {
			bingResult.rejectValue("passoword", "NotBlank", "패스워드를 8글자 이상 입력하세요");
		}
		if(!memberService.getPassword().equals(memberFrom.getPasswprdConfirm())) {
			bingResult.rejectValue("email", "AlreadyExist")
			
			
			
			
			
		}
			
		}
