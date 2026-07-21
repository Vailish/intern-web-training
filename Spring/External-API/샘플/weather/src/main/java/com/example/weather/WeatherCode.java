package com.example.weather;

/**
 * 날씨 코드 번역기.
 *
 * Open-Meteo는 현재 날씨를 "숫자 코드(WMO 표준)"로 줍니다. 예) 0=맑음, 61=비, 63=강한 비.
 * 사람이 보기엔 무의미하므로, 이 숫자를 한글 설명 + 이모지로 바꿔 줍니다.
 *
 * 이런 "코드 → 사람이 읽을 값" 변환은 외부 API를 쓸 때 흔히 필요합니다.
 * (은행 API의 거래코드, 공공 API의 상태코드 등도 마찬가지)
 *
 * 표 출처: Open-Meteo 공식 문서의 WMO Weather interpretation codes
 *          (https://open-meteo.com/en/docs — 공식 API 제공처의 문서)
 */
public final class WeatherCode {

	private WeatherCode() {
	}

	/** 화면 표시에 쓸 설명과 이모지 한 쌍 */
	public record Description(String text, String emoji) {
	}

	public static Description of(int code) {
		return switch (code) {
			case 0 -> new Description("맑음", "☀️");
			case 1 -> new Description("대체로 맑음", "🌤️");
			case 2 -> new Description("부분적으로 흐림", "⛅");
			case 3 -> new Description("흐림", "☁️");
			case 45, 48 -> new Description("안개", "🌫️");
			case 51, 53, 55 -> new Description("이슬비", "🌦️");
			case 56, 57 -> new Description("어는 이슬비", "🌧️");
			case 61 -> new Description("약한 비", "🌦️");
			case 63 -> new Description("비", "🌧️");
			case 65 -> new Description("강한 비", "🌧️");
			case 66, 67 -> new Description("어는 비", "🌧️");
			case 71 -> new Description("약한 눈", "🌨️");
			case 73 -> new Description("눈", "❄️");
			case 75 -> new Description("강한 눈", "❄️");
			case 77 -> new Description("싸락눈", "🌨️");
			case 80, 81, 82 -> new Description("소나기", "🌦️");
			case 85, 86 -> new Description("소낙눈", "🌨️");
			case 95 -> new Description("천둥번개", "⛈️");
			case 96, 99 -> new Description("우박을 동반한 천둥번개", "⛈️");
			default -> new Description("알 수 없음(코드 " + code + ")", "❓");
		};
	}
}
