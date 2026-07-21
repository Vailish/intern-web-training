package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 현재 날씨. 응답의 "current" 객체에 해당합니다.
 *
 * 여기서 중요한 기술 하나 —
 * JSON의 필드 이름은 temperature_2m 처럼 밑줄(snake_case)인데,
 * 자바에서는 temperature 처럼 낙타(camelCase) 이름을 쓰는 게 관례입니다.
 * 이름이 다르면 Jackson이 짝을 못 맞추므로, @JsonProperty 로 "이 JSON 이름과 연결하라"고 알려 줍니다.
 *
 * - temperature : 기온(℃)
 * - humidity    : 상대습도(%)
 * - weatherCode : 날씨 상태 코드 (WMO 표준 숫자 — WeatherCode.java 에서 한글로 번역)
 * - windSpeed   : 풍속(km/h)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Current(
		String time,
		@JsonProperty("temperature_2m") double temperature,
		@JsonProperty("relative_humidity_2m") int humidity,
		@JsonProperty("weather_code") int weatherCode,
		@JsonProperty("wind_speed_10m") double windSpeed
) {
}
