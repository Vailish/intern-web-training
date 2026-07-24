package com.example.authjwtrefresh.dto;

/**
 * 요청/응답 자료구조 모음. 엔티티를 직접 노출하지 않고 DTO 로 분리합니다.
 */
public class AuthDtos {

	/** 회원가입 요청: {"username":..., "password":..., "displayName":...} */
	public record SignupRequest(String username, String password, String displayName) {
	}

	/** 로그인 요청: {"username":..., "password":...} */
	public record LoginRequest(String username, String password) {
	}

	/** 리프레시/로그아웃 요청: {"refreshToken":...} */
	public record RefreshRequest(String refreshToken) {
	}

	/**
	 * 로그인/리프레시 응답: 액세스 토큰 + 리프레시 토큰을 함께 내려 줍니다.
	 * 클라이언트는 accessToken 으로 API를 호출하고, 만료되면 refreshToken 으로 새 토큰을 받습니다.
	 */
	public record TokenResponse(String accessToken, String refreshToken, String role) {
	}

	/** 내 정보 응답 */
	public record MeResponse(String username, String role) {
	}

	/** 단순 메시지 응답 */
	public record MessageResponse(String message) {
	}
}
