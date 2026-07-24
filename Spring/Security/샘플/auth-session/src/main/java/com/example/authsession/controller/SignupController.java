package com.example.authsession.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.authsession.service.MemberService;

/**
 * 회원가입 화면과 처리를 담당합니다.
 * (로그인 "처리"는 Security가 대신 해 주지만, 회원가입은 우리가 직접 구현합니다.)
 */
@Controller
public class SignupController {

	private final MemberService memberService;

	public SignupController(MemberService memberService) {
		this.memberService = memberService;
	}

	/** 회원가입 폼 화면. */
	@GetMapping("/signup")
	public String signupForm() {
		return "signup";
	}

	/**
	 * 회원가입 처리. 폼에서 넘어온 값 3개를 받아 MemberService 로 저장합니다.
	 * 아이디가 중복이면 서비스가 예외를 던지므로, 에러 메시지를 화면에 다시 보여 줍니다.
	 */
	@PostMapping("/signup")
	public String signup(@RequestParam String username,
						  @RequestParam String password,
						  @RequestParam String displayName,
						  Model model) {
		try {
			memberService.signup(username, password, displayName);
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "signup"; // 다시 회원가입 화면으로 (입력값 검증 실패)
		}
		// 가입 성공 → 로그인 페이지로 이동 (registered 파라미터로 안내 메시지 표시)
		return "redirect:/login?registered";
	}
}
