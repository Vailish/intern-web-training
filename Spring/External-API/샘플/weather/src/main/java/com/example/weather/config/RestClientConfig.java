package com.example.weather.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient = 스프링부트가 "다른 서버에 HTTP 요청을 보내는 도구".
 *
 * 지금까지는 브라우저가 우리 서버에 요청을 "받는" 입장이었습니다.
 * 이번에는 우리 서버가 외부(Open-Meteo)에 요청을 "보내는" 입장이 됩니다.
 * 그때 쓰는 것이 RestClient 입니다. (Spring 6.1 / 부트 3.2부터 제공되는 최신 방식)
 *
 * 외부 서비스가 둘(주소 검색 서버, 날씨 서버)이라서
 * 각 서버 주소(baseUrl)를 미리 박아 둔 RestClient를 2개 만들어 둡니다.
 * 이렇게 Bean으로 만들어 두면 필요한 곳에서 주입받아 재사용할 수 있습니다.
 */
@Configuration
public class RestClientConfig {

	// 외부 서버가 3초 안에 연결되지 않거나 5초 안에 응답하지 않으면 포기합니다.
	// 이 설정이 없으면, 외부 서버가 멈췄을 때 우리 서버도 하염없이 기다리게 됩니다. (실무 필수)
	private ClientHttpRequestFactory timeoutFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(3));  // 연결까지 최대 3초
		factory.setReadTimeout(Duration.ofSeconds(5));     // 응답 수신까지 최대 5초
		return factory;
	}

	/** 도시 이름 → 위도·경도를 찾아 주는 지오코딩 서버 전용 RestClient */
	@Bean
	public RestClient geocodingRestClient() {
		// RestClient.builder() 는 spring-web이 기본 제공하는 정적 팩토리입니다.
		// (별도 준비 없이 바로 RestClient를 만들 수 있습니다)
		return RestClient.builder()
				.baseUrl("https://geocoding-api.open-meteo.com")
				.requestFactory(timeoutFactory())
				.build();
	}

	/** 위도·경도 → 현재 날씨를 주는 예보 서버 전용 RestClient */
	@Bean
	public RestClient forecastRestClient() {
		return RestClient.builder()
				.baseUrl("https://api.open-meteo.com")
				.requestFactory(timeoutFactory())
				.build();
	}
}
