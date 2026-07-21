package com.example.weather.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.weather.dto.GeocodingResponse;
import com.example.weather.dto.GeoResult;

/**
 * 1단계 호출: 도시 이름 → 위도·경도.
 *
 * 날씨 서버는 "도시 이름"이 아니라 "좌표(위도·경도)"를 요구합니다.
 * 그래서 먼저 이 지오코딩 서버에 도시 이름을 물어 좌표를 알아냅니다.
 *
 * 실제로 보내는 요청:
 *   GET https://geocoding-api.open-meteo.com/v1/search?name=Seoul&count=1&language=ko&format=json
 */
@Component
public class GeocodingClient {

	private final RestClient geocodingRestClient;

	// RestClientConfig가 만들어 둔 Bean 중 "geocodingRestClient"를 콕 집어 주입받습니다.
	public GeocodingClient(@Qualifier("geocodingRestClient") RestClient geocodingRestClient) {
		this.geocodingRestClient = geocodingRestClient;
	}

	/**
	 * 도시를 찾으면 좌표 정보를, 못 찾으면 비어 있음(Optional.empty)을 돌려줍니다.
	 * "결과가 없을 수 있다"는 사실을 Optional로 분명히 표현합니다.
	 */
	public Optional<GeoResult> findCity(String cityName) {
		GeocodingResponse response = geocodingRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/v1/search")
						.queryParam("name", cityName)   // 검색할 도시 이름 (영문으로 넣어야 잘 찾습니다)
						.queryParam("count", 1)          // 가장 잘 맞는 1건만
						.queryParam("language", "ko")    // 결과 이름을 한글로 (서울특별시)
						.queryParam("format", "json")
						.build())
				.retrieve()                              // 요청을 실제로 보냄
				.body(GeocodingResponse.class);          // 응답 JSON을 GeocodingResponse 객체로 변환

		// 존재하지 않는 도시면 results가 비어 있거나 null 입니다.
		if (response == null || response.results() == null || response.results().isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(response.results().get(0));
	}
}
