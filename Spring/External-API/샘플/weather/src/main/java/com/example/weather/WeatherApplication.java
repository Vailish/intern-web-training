package com.example.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 프로그램의 시작점. 지금까지 만든 스프링부트 앱과 똑같습니다.
 * 다른 점은 이 앱이 "DB를 가진 서버"가 아니라
 * "외부 API를 불러다 쓰는 서버"라는 것뿐입니다.
 */
@SpringBootApplication
public class WeatherApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherApplication.class, args);
	}

}
