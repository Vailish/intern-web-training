package com.example.authjwt.dto;

/**
 * 요청/응답에 쓰는 자료구조 모음. record 는 값만 담는 불변 객체를 짧게 정의하는 문법입니다.
 * 엔티티(Member)를 직접 요청/응답에 노출하지 않고 DTO 로 분리하는 것이 실무 원칙입니다.
 */
public class AuthDtos {

	/** 회원가입 요청 본문(JSON): {"username":..., "password":..., "displayName":...} */
	public record SignupRequest(String username, String password, String displayName) {
	}

	/** 로그인 요청 본문(JSON): {"username":..., "password":...} */
	public record LoginRequest(String username, String password) {
	}

	/** 로그인 응답: 발급된 토큰과 권한 */
	public record LoginResponse(String token, String role) {
	}

	/** 내 정보 응답: 현재 토큰의 주인과 권한 */
	public record MeResponse(String username, String role) {
	}

	/** 단순 메시지 응답 (성공/오류 안내) */
	public record MessageResponse(String message) {
	}
}
