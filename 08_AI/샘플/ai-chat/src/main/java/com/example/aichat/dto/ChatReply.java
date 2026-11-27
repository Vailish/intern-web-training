package com.example.aichat.dto;

/**
 * 채팅 응답.
 * (Spring AI 에 ChatResponse 라는 클래스가 이미 있어서 헷갈리지 않게 ChatReply 로 이름 붙였습니다.)
 *
 * @param answer      AI 답변 본문
 * @param model       실제로 답한 모델 이름 (설정 확인용)
 * @param totalTokens 이번 요청에 쓴 토큰 수 = 비용의 단위 (모르면 null)
 * @param elapsedMs   걸린 시간(밀리초)
 */
public record ChatReply(String answer, String model, Integer totalTokens, long elapsedMs) {
}
