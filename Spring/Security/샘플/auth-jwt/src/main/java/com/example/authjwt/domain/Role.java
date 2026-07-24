package com.example.authjwt.domain;

/**
 * 회원의 권한(역할). USER=일반 회원, ADMIN=관리자.
 * Spring Security는 권한 이름 앞에 "ROLE_" 를 붙여 다룹니다. (예: ROLE_ADMIN)
 */
public enum Role {
	USER,
	ADMIN
}
