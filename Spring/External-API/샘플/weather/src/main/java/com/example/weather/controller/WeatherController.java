package com.example.weather.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

import com.example.weather.CityNotFoundException;
import com.example.weather.dto.WeatherView;
import com.example.weather.service.WeatherService;

/**
 * 브라우저 요청을 받는 곳. 지금까지 만든 @Controller 와 똑같은 방식입니다.
 *
 * 화면은 하나(index.html)뿐입니다.
 *   - 처음 접속(?city 없음)  → 검색 폼만 보여 줌
 *   - 도시를 검색(?city=...) → 폼 + 결과 카드(또는 에러 메시지)를 함께 보여 줌
 */
@Controller
public class WeatherController {

	private final WeatherService weatherService;

	public WeatherController(WeatherService weatherService) {
		this.weatherService = weatherService;
	}

	@GetMapping("/")
	public String index(@RequestParam(required = false) String city, Model model) {
		// 아직 검색 전이면 폼만 보여 주고 끝
		if (city == null || city.isBlank()) {
			return "index";
		}

		model.addAttribute("city", city);
		try {
			WeatherView weather = weatherService.getWeather(city);
			model.addAttribute("weather", weather);
		} catch (CityNotFoundException e) {
			// "그런 도시 없음" → 입력을 고치라는 안내
			model.addAttribute("error", e.getMessage());
		} catch (RestClientException e) {
			// 외부 서버가 느리거나(타임아웃) 죽었거나 네트워크 문제 → 우리 잘못이 아님을 알림
			model.addAttribute("error", "날씨 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.");
		}
		return "index";
	}
}
