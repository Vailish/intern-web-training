package com.example.aichat.exception;

/** AI 는 답했지만 약속한 형식(JSON)이 아니라서 쓸 수 없음. → 502 */
public class AiBadOutputException extends RuntimeException {

	public AiBadOutputException(String message, Throwable cause) {
		super(message, cause);
	}
}
