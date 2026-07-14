package com.example.introapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS(Cross-Origin Resource Sharing) 설정 — 프론트/백 분리의 필수 관문!
 *
 * 문제 상황:
 *   - React 개발 서버 : http://localhost:5173
 *   - 스프링 API 서버 : http://localhost:8080
 *   같은 내 컴퓨터지만 포트가 다르면 브라우저는 "다른 출처(Origin)"로 취급합니다.
 *
 * 브라우저에는 "내가 접속한 사이트(5173)가 아닌 다른 곳(8080)으로는
 * 함부로 요청을 보내지 못하게 막는" 보안 규칙이 있습니다(동일 출처 정책).
 * 악성 사이트가 여러분이 로그인해 둔 은행 서버로 몰래 요청을 보내는 것을
 * 막기 위한 장치인데, 우리처럼 "일부러" 분리한 경우에도 똑같이 걸립니다.
 *
 * 해결: 서버가 "5173에서 오는 요청은 허용한다"고 명시적으로 선언합니다.
 * 이 설정이 없으면 React의 fetch()가 전부 실패하고, 브라우저 콘솔(F12)에
 * "blocked by CORS policy" 라는 빨간 에러가 뜹니다. 프론트/백 분리 개발에서
 * 누구나 한 번은 만나는 에러이니, 일부러 이 파일을 지우고 실행해 보세요!
 *
 * ⚠️ allowedOrigins("*") 처럼 전부 열어버리면 편하지만, 위에서 설명한
 *    보안 장치를 스스로 끄는 것과 같습니다. 꼭 필요한 출처만 여세요.
 */
@Configuration // "스프링 설정 클래스"라는 표식 — 서버가 뜰 때 읽어 갑니다.
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")                       // /api로 시작하는 URL에 대해
                .allowedOrigins("http://localhost:5173")     // React 개발 서버의 요청을 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE"); // 허용할 HTTP 메서드
    }
}
