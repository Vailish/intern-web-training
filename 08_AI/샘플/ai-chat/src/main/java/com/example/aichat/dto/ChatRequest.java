package com.example.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 채팅 요청. 입력 길이를 서버에서 제한합니다 (긴 입력 = 큰 비용 + 느린 응답).
 * 브라우저의 maxlength 는 우회할 수 있으므로 서버 검증이 진짜 방어선입니다.
 */
public record ChatRequest(
		@NotBlank(message = "질문을 입력하세요.")
		@Size(max = 500, message = "질문은 500자 이하로 입력하세요.")
		String message) {
}
