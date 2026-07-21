package com.example.weather.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.weather.dto.ForecastResponse;

/**
 * 2단계 호출: 위도·경도 → 현재 날씨.
 *
 * 1단계(GeocodingClient)에서 얻은 좌표를 넣어 실제 날씨를 받아 옵니다.
 *
 * 실제로 보내는 요청:
 *   GET https://api.open-meteo.com/v1/forecast
 *       ?latitude=37.57&longitude=126.98
 *       &current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m
 *       &timezone=auto
 */
@Component
public class ForecastClient {

	private final RestClient forecastRestClient;

	public ForecastClient(@Qualifier("forecastRestClient") RestClient forecastRestClient) {
		this.forecastRestClient = forecastRestClient;
	}

	public ForecastResponse getCurrent(double latitude, double longitude) {
		return forecastRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/v1/forecast")
						.queryParam("latitude", latitude)
						.queryParam("longitude", longitude)
						// 받고 싶은 현재 항목들: 기온, 습도, 날씨코드, 풍속
						.queryParam("current", "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m")
						.queryParam("timezone", "auto")  // 해당 지역 시간대로 시각 표시
						.build())
				.retrieve()
				.body(ForecastResponse.class);
	}
}
