package com.example.authsession.domain;

/**
 * 회원의 권한(역할)을 나타냅니다.
 *
 * - USER  : 일반 회원 (로그인만 하면 접근 가능한 페이지)
 * - ADMIN : 관리자   (관리자 전용 페이지에 접근 가능)
 *
 * Spring Security는 내부적으로 권한 이름 앞에 "ROLE_" 를 붙여서 다룹니다.
 * 즉 여기서 ADMIN 이라고 저장하면, 보안 설정에서는 "ROLE_ADMIN" 으로 취급됩니다.
 * (자세한 규칙은 SecurityConfig 와 CustomUserDetailsService 주석 참고)
 */
public enum Role {
	USER,
	ADMIN
}
