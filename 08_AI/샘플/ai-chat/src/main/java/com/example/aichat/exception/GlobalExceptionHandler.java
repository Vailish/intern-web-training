package com.example.aichat.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 에러를 화면이 이해할 수 있는 JSON {"error": "..."} 으로 바꿉니다.
 * 원칙: 사용자에게는 짧고 친절한 문장만, 자세한 원인은 서버 로그에만.
 * (AI 서버 주소, 키, 스택트레이스를 화면에 그대로 내보내면 안 됩니다.)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/** 400: 입력 검증 실패 (빈 값, 너무 긴 값) */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(fieldError -> fieldError.getDefaultMessage())
				.orElse("입력값을 확인하세요.");
		return ResponseEntity.badRequest().body(Map.of("error", message));
	}

	/** 503: AI 서버 문제 — 잠시 후 다시 시도하면 될 수도 있음 */
	@ExceptionHandler(AiUnavailableException.class)
	public ResponseEntity<Map<String, String>> handleUnavailable(AiUnavailableException e) {
		log.warn("AI 호출 실패: {}", e.getMessage(), e.getCause());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
	}

	/** 502: AI 가 이상한 형식으로 답함 — 다시 시도하면 성공할 수도 있음 */
	@ExceptionHandler(AiBadOutputException.class)
	public ResponseEntity<Map<String, String>> handleBadOutput(AiBadOutputException e) {
		log.warn("AI 출력 형식 오류: {}", e.getMessage(), e.getCause());
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", e.getMessage()));
	}
}
