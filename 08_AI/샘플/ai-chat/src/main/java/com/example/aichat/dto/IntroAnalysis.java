package com.example.aichat.dto;

import java.util.List;

/**
 * AI 가 돌려줄 "구조화된 결과".
 * Spring AI 가 이 record 를 보고 JSON 스키마를 만들어 프롬프트에 붙이고,
 * AI 가 돌려준 JSON 문자열을 다시 이 record 로 바꿔 줍니다.
 *
 * @param summary  한 줄 요약
 * @param keywords 핵심 키워드 (최대 5개)
 * @param field    추천 직무 분야 — 반드시 ALLOWED_FIELDS 중 하나여야 함 (서버가 검증)
 */
public record IntroAnalysis(String summary, List<String> keywords, String field) {

	/** AI 가 이 목록 밖의 값을 주면 믿지 않고 "기타"로 바꿉니다 (허용 목록 검증). */
	public static final List<String> ALLOWED_FIELDS = List.of("백엔드", "프론트엔드", "데이터", "기타");
}
