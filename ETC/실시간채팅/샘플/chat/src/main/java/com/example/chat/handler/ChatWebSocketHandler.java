package com.example.chat.handler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat.dto.ChatMessage;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * 채팅의 심장. WebSocket 연결에서 일어나는 모든 사건을 처리하는 담당자입니다.
 *
 * TextWebSocketHandler 를 상속하면, 우리는 아래 "사건이 일어났을 때 부를 메서드"만
 * 채워 넣으면 됩니다. (콜백 방식 — 언제 불릴지는 스프링이 정합니다)
 *   - afterConnectionEstablished : 누군가 연결됨      (전화가 연결된 순간)
 *   - handleTextMessage          : 메시지가 도착함    (상대가 말을 함)
 *   - afterConnectionClosed      : 연결이 끊김        (전화를 끊음)
 *   - handleTransportError       : 통신 중 오류 발생  (전화가 지직거림)
 *
 * 핵심 아이디어:
 *   접속한 사람들의 "연결(WebSocketSession)"을 목록에 모아 두었다가,
 *   한 명이 메시지를 보내면 그 목록 전체에 똑같이 뿌린다(=브로드캐스트).
 *   이것이 그룹 채팅의 전부입니다.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

	/**
	 * 지금 접속해 있는 모든 연결(세션)의 목록.
	 * 메시지를 "전체에게" 뿌릴 때 이 목록을 하나씩 훑습니다.
	 *
	 * 여러 사람이 동시에 접속/퇴장하므로 여러 스레드가 이 목록을 함께 건드립니다.
	 * 그래서 일반 HashSet 이 아니라, 동시 접근에 안전한 CopyOnWriteArraySet 을 씁니다.
	 */
	private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

	/**
	 * "세션 → 닉네임" 대응표.
	 * 연결이 끊길 때는 메시지 내용이 없으므로, 누가 나갔는지 이름을 여기서 찾습니다.
	 */
	private final Map<String, String> nicknames = new ConcurrentHashMap<>();

	/**
	 * JSON 문자열 ↔ 자바 객체(ChatMessage)를 서로 변환해 주는 도구.
	 *
	 * ⚠️ 스프링부트 4.x 부터는 JSON 라이브러리(Jackson)가 3버전으로 올라가면서
	 *    패키지 이름이 com.fasterxml.jackson → tools.jackson 으로 바뀌었습니다.
	 *    (인터넷 예제 대부분은 아직 옛날 com.fasterxml... 을 쓰므로, 그대로 붙여넣으면 컴파일 오류가 납니다.)
	 *    또 3버전에서는 new ObjectMapper() 대신 JsonMapper.builder().build() 사용이 권장됩니다.
	 */
	private final ObjectMapper objectMapper = JsonMapper.builder().build();

	/**
	 * [사건 1] 브라우저가 막 연결되었을 때 호출됩니다.
	 * 아직 이 사람의 닉네임은 모릅니다. 잠시 뒤 클라이언트가 ENTER 메시지로 알려 줍니다.
	 * 여기서는 일단 연결만 목록에 등록해 둡니다.
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.add(session);
	}

	/**
	 * [사건 2] 브라우저가 메시지를 보냈을 때 호출됩니다. 채팅의 알맹이입니다.
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		// 도착한 JSON 문자열을 우리가 다루기 쉬운 자바 객체로 바꿉니다.
		ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);

		if (chatMessage.getType() == ChatMessage.Type.ENTER) {
			// 입장 메시지: 닉네임을 기억해 두고, 안내 문구를 서버가 직접 만들어 줍니다.
			nicknames.put(session.getId(), chatMessage.getSender());
			chatMessage.setContent(chatMessage.getSender() + "님이 입장했습니다.");
		}
		// TALK(일반 대화)이면 content 를 그대로 둡니다.

		// 현재 접속 인원을 실어서 모두에게 뿌립니다. (화면 상단 "N명 접속중" 갱신용)
		chatMessage.setUserCount(sessions.size());
		broadcast(chatMessage);
	}

	/**
	 * [사건 3] 연결이 끊겼을 때(창을 닫거나 새로고침) 호출됩니다.
	 * 목록에서 지우고, 남은 사람들에게 "누가 나갔다"고 알립니다.
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session);
		String nickname = nicknames.remove(session.getId());

		// 닉네임이 있다는 건 정상적으로 입장했던 사람이라는 뜻입니다.
		if (nickname != null) {
			ChatMessage leaveMessage = new ChatMessage();
			leaveMessage.setType(ChatMessage.Type.LEAVE);
			leaveMessage.setSender(nickname);
			leaveMessage.setContent(nickname + "님이 나갔습니다.");
			leaveMessage.setUserCount(sessions.size());
			broadcast(leaveMessage);
		}
	}

	/**
	 * [사건 4] 통신 중 오류가 났을 때 호출됩니다. 남은 흔적을 정리합니다.
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		sessions.remove(session);
		nicknames.remove(session.getId());
	}

	/**
	 * 접속 중인 모두에게 같은 메시지를 보냅니다. ("브로드캐스트" = 전체 전송)
	 * 그룹 채팅의 핵심 동작입니다.
	 */
	private void broadcast(ChatMessage chatMessage) throws Exception {
		// 자바 객체를 다시 JSON 문자열로 바꿔서 보냅니다.
		String json = objectMapper.writeValueAsString(chatMessage);
		TextMessage textMessage = new TextMessage(json);

		for (WebSocketSession session : sessions) {
			if (session.isOpen()) {
				// 주의: 같은 세션에 두 스레드가 동시에 쓰면 오류가 날 수 있습니다.
				// 그래서 세션 단위로 잠금을 걸어 한 번에 하나씩만 보내도록 합니다. (실무에서 놓치기 쉬운 부분)
				synchronized (session) {
					session.sendMessage(textMessage);
				}
			}
		}
	}
}
