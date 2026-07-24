package com.example.authjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JWT 토큰 방식 인증 데모의 시작점입니다.
 *
 * 세션 방식과 달리 서버는 아무 것도 기억하지 않습니다(무상태, stateless).
 * 로그인에 성공하면 서버는 "서명된 토큰(JWT)"을 발급하고, 클라이언트는
 * 이후 모든 요청의 헤더에 그 토큰을 실어 보내 신분을 증명합니다.
 *
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
 *
 * 실행: ./gradlew bootRun  → 브라우저에서 http://localhost:8080 (데모 페이지)
 */
@SpringBootApplication
public class AuthJwtApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthJwtApplication.class, args);
	}
}
