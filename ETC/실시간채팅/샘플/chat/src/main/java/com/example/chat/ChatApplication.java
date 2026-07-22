package com.example.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 프로그램의 시작점. 지금까지 만든 스프링부트 앱과 똑같이 생겼습니다.
 *
 * 다른 점은 딱 하나입니다.
 *   - 지금까지: 브라우저가 "요청 → 응답"을 주고받고 연결이 곧바로 끊겼습니다. (HTTP)
 *   - 이번:     브라우저와 서버가 한 번 연결되면 그 통로를 계속 열어 둡니다. (WebSocket)
 *
 * 그 "계속 열려 있는 통로" 덕분에 서버가 먼저 말을 걸 수 있고(=실시간 알림),
 * 한 사람이 보낸 메시지를 접속 중인 모두에게 즉시 뿌릴 수 있습니다(=채팅).
 */
@SpringBootApplication
public class ChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
	}

}
