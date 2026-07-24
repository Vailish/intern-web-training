package com.example.authjwtrefresh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JWT + 리프레시 토큰 데모의 시작점입니다.
 *
 * 앞의 auth-jwt 샘플은 토큰이 딱 하나(액세스 토큰)뿐이라, 만료되면 다시 로그인해야 했습니다.
 * 이 샘플은 토큰을 두 개로 나눕니다.
 *
 *   - 액세스 토큰(Access Token) : 실제 API 호출에 쓰는 JWT. 수명이 아주 짧음(기본 60초).
 *   - 리프레시 토큰(Refresh Token): 액세스 토큰이 만료됐을 때 "재로그인 없이" 새로 발급받는 열쇠.
 *                                   수명이 긺(기본 14일)이고, 서버 DB에 저장되어 강제 폐기가 가능함.
 *
 * 실행: ./gradlew bootRun  → 브라우저에서 http://localhost:8080 (데모 페이지)
 */
@SpringBootApplication
public class AuthJwtRefreshApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthJwtRefreshApplication.class, args);
	}
}
