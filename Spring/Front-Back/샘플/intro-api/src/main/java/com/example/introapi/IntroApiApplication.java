package com.example.introapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 백엔드 API 서버의 시작점입니다. (intro-jpa와 똑같습니다)
 *
 * 달라진 점은 이 서버가 하는 "일"입니다.
 *   - intro-jpa : 요청 → 데이터 조회 → HTML 화면을 만들어서 응답 (화면 담당까지 겸함)
 *   - intro-api : 요청 → 데이터 조회 → JSON 데이터만 응답 (화면은 React가 담당)
 */
@SpringBootApplication
public class IntroApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntroApiApplication.class, args);
    }
}
