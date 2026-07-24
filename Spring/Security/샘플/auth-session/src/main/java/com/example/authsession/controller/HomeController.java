package com.example.authsession.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 화면 이동을 담당하는 컨트롤러.
 *
 * 로그인한 사용자가 누구인지 알아야 할 때 메서드 파라미터에 Principal 을 선언하면,
 * Spring Security가 현재 로그인한 사용자 정보를 자동으로 넣어 줍니다.
 * (로그인하지 않았다면 principal 은 null)
 */
@Controller
public class HomeController {

	/** 홈 화면: 로그인 상태에 따라 다른 내용을 보여 줍니다. */
	@GetMapping("/")
	public String home(Principal principal, Model model) {
		if (principal != null) {
			model.addAttribute("username", principal.getName());
		}
		return "home";
	}

	/** 로그인 페이지. 실제 로그인 "처리"는 Security가 하고, 우리는 화면만 제공합니다. */
	@GetMapping("/login")
	public String login() {
		return "login";
	}

	/** 로그인한 사람이면 누구나 볼 수 있는 마이페이지. */
	@GetMapping("/mypage")
	public String mypage(Principal principal, Model model) {
		model.addAttribute("username", principal.getName());
		return "mypage";
	}

	/** ADMIN 권한을 가진 사람만 볼 수 있는 관리자 페이지 (규칙은 SecurityConfig 에서 통제). */
	@GetMapping("/admin")
	public String admin(Principal principal, Model model) {
		model.addAttribute("username", principal.getName());
		return "admin";
	}
}
