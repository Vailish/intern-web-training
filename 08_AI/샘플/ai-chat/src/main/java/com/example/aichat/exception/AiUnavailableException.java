package com.example.aichat.exception;

/** AI 서버에 연결 못 함 / 시간 초과 / 사용량 초과 등 "AI 쪽 사정"으로 실패. → 503 */
public class AiUnavailableException extends RuntimeException {

	public AiUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
