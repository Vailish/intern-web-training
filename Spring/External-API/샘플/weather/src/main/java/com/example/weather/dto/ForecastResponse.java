package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 날씨 예보 서버의 응답 전체.
 *
 * 실제 응답 예:
 * {
 *   "timezone": "Asia/Seoul",
 *   "current": { "time": "...", "temperature_2m": 25.5, ... },
 *   ... (우리가 안 쓰는 필드 다수)
 * }
 *
 * 응답이 중첩(객체 안의 객체) 구조라서, 자바 객체도 똑같이 중첩합니다.
 * ForecastResponse 안에 Current 를 품고 있는 형태.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ForecastResponse(
		String timezone,
		Current current
) {
}
