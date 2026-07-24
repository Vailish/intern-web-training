package com.example.authjwtrefresh.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authjwtrefresh.dto.AuthDtos.MeResponse;
import com.example.authjwtrefresh.dto.AuthDtos.MessageResponse;

/**
 * 토큰이 있어야 접근할 수 있는 보호된 API 들.
 *
 * 메서드 파라미터의 Authentication 은 JwtAuthenticationFilter 가 토큰을 검사해
 * SecurityContext 에 넣어 둔 "현재 사용자" 정보입니다. (토큰이 없으면 여기까지 오지 못하고 401)
 */
@RestController
@RequestMapping("/api")
public class ApiController {

	/** 로그인한 사람이면 누구나: 내 정보 확인 */
	@GetMapping("/me")
	public MeResponse me(Authentication authentication) {
		String role = authentication.getAuthorities().iterator().next().getAuthority();
		return new MeResponse(authentication.getName(), role);
	}

	/** ADMIN 권한을 가진 사람만: 관리자 전용 데이터 (경로가 /api/admin/** 이라 SecurityConfig 가 통제) */
	@GetMapping("/admin/dashboard")
	public MessageResponse adminDashboard(Authentication authentication) {
		return new MessageResponse(authentication.getName() + " 님, 여기는 관리자 전용 대시보드입니다.");
	}
}
