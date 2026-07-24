package com.example.authsession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 세션 방식 인증 데모의 시작점입니다.
 *
 * 이 앱은 "로그인하면 서버가 세션을 만들고, 브라우저는 세션 쿠키(JSESSIONID)를
 * 가지고 다니면서 신분을 증명한다"는 전통적인 방식을 보여 줍니다.
 *
 * 실행: ./gradlew bootRun  → 브라우저에서 http://localhost:8080 접속
 */
@SpringBootApplication
public class AuthSessionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthSessionApplication.class, args);
	}
}
