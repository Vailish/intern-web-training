package com.example.authsession;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 스프링 컨텍스트(설정, 빈)가 오류 없이 뜨는지 확인하는 최소 테스트.
 * SecurityConfig 등에 설정 오류가 있으면 이 테스트가 실패합니다.
 */
@SpringBootTest
class AuthSessionApplicationTests {

	@Test
	void contextLoads() {
	}
}
