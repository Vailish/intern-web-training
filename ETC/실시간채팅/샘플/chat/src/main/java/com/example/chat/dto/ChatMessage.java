package com.example.chat.dto;

/**
 * 채팅 메시지 한 건을 담는 상자(DTO).
 *
 * 브라우저(자바스크립트)와 서버(자바)는 서로 다른 언어입니다.
 * 둘이 대화하려면 공통 약속이 필요한데, 그 약속이 바로 JSON 입니다.
 *
 *   브라우저가 보내는 JSON:  {"type":"TALK", "sender":"철수", "content":"안녕하세요"}
 *                                   │
 *                                   ▼  스프링(Jackson)이 자동으로 변환
 *   서버 안의 자바 객체:       ChatMessage(type=TALK, sender="철수", content="안녕하세요")
 *
 * 반대로 서버가 브라우저에게 보낼 때는 이 객체가 다시 JSON 문자열로 바뀝니다.
 * 이 변환은 스프링이 알아서 해 주므로, 우리는 필드만 잘 맞춰 주면 됩니다.
 *
 * 필드가 private 이고 getter/setter 로 여닫는 이 구조는
 * 회사 실무(eGovFrame)의 VO/DTO 클래스와 똑같은 모양입니다.
 */
public class ChatMessage {

	/**
	 * 메시지의 종류. "이게 입장 알림인지, 대화인지, 퇴장 알림인지"를 구분합니다.
	 *   - ENTER : 누군가 채팅방에 들어옴
	 *   - TALK  : 실제 대화 내용
	 *   - LEAVE : 누군가 채팅방에서 나감
	 */
	public enum Type {
		ENTER, TALK, LEAVE
	}

	private Type type;       // 메시지 종류
	private String sender;   // 보낸 사람 닉네임
	private String content;  // 메시지 내용
	private int userCount;   // 지금 채팅방에 몇 명 있는지 (화면 상단에 표시)

	// Jackson(JSON ↔ 자바 변환기)이 객체를 만들 때 기본 생성자가 필요합니다.
	public ChatMessage() {
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
}
