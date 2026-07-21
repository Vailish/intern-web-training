package com.example.weather.service;

import org.springframework.stereotype.Service;

import com.example.weather.CityNotFoundException;
import com.example.weather.WeatherCode;
import com.example.weather.client.ForecastClient;
import com.example.weather.client.GeocodingClient;
import com.example.weather.dto.Current;
import com.example.weather.dto.ForecastResponse;
import com.example.weather.dto.GeoResult;
import com.example.weather.dto.WeatherView;

/**
 * 두 번의 외부 호출을 순서대로 엮는 "지휘자".
 *
 *   도시 이름 → (1단계: 지오코딩) 좌표 → (2단계: 예보) 날씨 → 화면용 WeatherView 로 조립
 *
 * 컨트롤러는 이 서비스의 getWeather(city) 하나만 부르면 됩니다.
 * 외부 API가 둘이라는 복잡함은 이 안에 감춰집니다. (관심사 분리)
 */
@Service
public class WeatherService {

	private final GeocodingClient geocodingClient;
	private final ForecastClient forecastClient;

	public WeatherService(GeocodingClient geocodingClient, ForecastClient forecastClient) {
		this.geocodingClient = geocodingClient;
		this.forecastClient = forecastClient;
	}

	public WeatherView getWeather(String city) {
		// 1단계: 도시 이름으로 좌표 찾기. 없으면 예외를 던져 컨트롤러가 안내하도록.
		GeoResult geo = geocodingClient.findCity(city)
				.orElseThrow(() -> new CityNotFoundException(city));

		// 2단계: 찾은 좌표로 현재 날씨 조회
		ForecastResponse forecast = forecastClient.getCurrent(geo.latitude(), geo.longitude());
		Current now = forecast.current();

		// 숫자 날씨코드를 한글 설명 + 이모지로 번역
		WeatherCode.Description desc = WeatherCode.of(now.weatherCode());

		// 화면에 뿌리기 좋은 하나의 객체로 조립해서 반환
		return new WeatherView(
				geo.name(),
				geo.country(),
				now.temperature(),
				now.humidity(),
				now.windSpeed(),
				desc.text(),
				desc.emoji(),
				now.time(),
				forecast.timezone()
		);
	}
}
