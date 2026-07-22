package com.example.chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 앱이 정상적으로 뜨는지(스프링 설정에 오류가 없는지)만 확인하는 기본 테스트입니다.
 * WebSocketConfig, ChatWebSocketHandler 등이 서로 잘 조립되면 통과합니다.
 */
@SpringBootTest
class ChatApplicationTests {

	@Test
	void contextLoads() {
	}

}
