package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.chat.handler.ChatWebSocketHandler;

/**
 * "어떤 주소로 들어온 WebSocket 연결을, 어떤 담당자(핸들러)에게 넘길지" 정하는 설정입니다.
 *
 * 지금까지 @GetMapping("/intro") 처럼 HTTP 주소와 메서드를 연결했던 것과 같은 역할을,
 * WebSocket 세계에서는 이 파일이 합니다.
 *
 * 여기서는 "브라우저가 ws://.../ws/chat 로 연결하면 ChatWebSocketHandler가 맡는다" 고 등록합니다.
 */
@Configuration
@EnableWebSocket // 이 앱에서 WebSocket 기능을 켭니다.
public class WebSocketConfig implements WebSocketConfigurer {

	private final ChatWebSocketHandler chatWebSocketHandler;

	// 스프링이 만들어 둔 핸들러(@Component)를 생성자로 주입받습니다. (지금까지 서비스 주입과 동일)
	public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
		this.chatWebSocketHandler = chatWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
				// "/ws/chat" 주소로 들어온 연결은 chatWebSocketHandler 가 처리합니다.
				.addHandler(chatWebSocketHandler, "/ws/chat");

		// 참고: 허용 출처(Origin) 설정에 대하여
		//  이 예제는 서버가 채팅 화면(index.html)까지 함께 내려 줍니다.
		//  즉 화면과 서버가 같은 출처(http://localhost:8080)라서 별도 허용이 필요 없습니다.
		//
		//  만약 React(예: http://localhost:5173)처럼 다른 곳에서 이 서버로 접속한다면,
		//  아래처럼 그 주소를 명시해야 연결이 허용됩니다.
		//      .setAllowedOrigins("http://localhost:5173")
		//  이때 편하다고 "*"(전부 허용)로 열어 두면 안 됩니다 — 실무 보안 금기입니다.
		//  (Front-Back 모듈의 CORS "와일드카드 금지" 교육 포인트와 같은 이유)
	}
}
