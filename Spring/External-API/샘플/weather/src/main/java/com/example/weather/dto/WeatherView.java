package com.example.weather.dto;

/**
 * 화면(Thymeleaf)에 뿌리기 좋게 가공한 "완성된 결과물".
 *
 * 왜 따로 만드나요?
 *   외부에서 받은 GeoResult(좌표) 와 Current(날씨) 는 서로 다른 응답에서 왔고,
 *   화면에서 쓰기엔 날것(raw)입니다. 이 둘을 합치고, 숫자 코드를 한글 설명으로 바꾸고,
 *   화면에 필요한 것만 골라 담은 "표시 전용 객체"를 서비스가 만들어서 넘겨 줍니다.
 *   (외부 응답 DTO를 화면까지 그대로 끌고 다니지 않는 것 — 실무의 좋은 습관)
 */
public record WeatherView(
		String cityName,     // 도시 이름 (예: 서울특별시)
		String country,      // 나라 (예: 대한민국)
		double temperature,  // 기온(℃)
		int humidity,        // 습도(%)
		double windSpeed,    // 풍속(km/h)
		String description,  // 날씨 설명 (예: 소나기)
		String emoji,        // 날씨 이모지 (예: 🌦️)
		String observedAt,   // 관측 시각 (예: 2026-07-21T13:00)
		String timezone      // 시간대 (예: Asia/Seoul)
) {
}
