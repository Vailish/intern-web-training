package com.example.authsession.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security의 핵심 설정 파일입니다. "누가 어디에 접근할 수 있는가"를 여기서 정합니다.
 *
 * 스프링부트 4(Spring Security 7)에서는 옛날 방식(WebSecurityConfigurerAdapter 상속)이
 * 완전히 사라졌습니다. 대신 SecurityFilterChain 이라는 "빈"을 하나 만들어 반환합니다.
 * 또한 설정은 모두 람다(-> ) 스타일로 작성합니다.
 */
@Configuration
public class SecurityConfig {

	/**
	 * 비밀번호 암호화 도구. BCrypt는 같은 비밀번호라도 매번 다른 해시를 만들고,
	 * 원문으로 되돌릴 수 없는(단방향) 업계 표준 알고리즘입니다.
	 * 회원가입 시 저장할 때, 로그인 시 비교할 때 모두 이 빈을 사용합니다.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 보안 규칙의 본체.
	 * HttpSecurity 를 조립해서 "어떤 URL은 아무나, 어떤 URL은 로그인해야, 어떤 URL은 관리자만"
	 * 같은 규칙과, 로그인/로그아웃 화면을 설정합니다.
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// 1) URL별 접근 권한 규칙 (위에서부터 순서대로 매칭되므로 순서가 중요합니다)
			.authorizeHttpRequests(auth -> auth
				// 로그인 없이 접근 가능한 곳: 홈, 로그인/회원가입 페이지, 정적 리소스, H2 콘솔
				.requestMatchers("/", "/login", "/signup", "/css/**", "/h2-console/**").permitAll()
				// /admin 으로 시작하는 주소는 ADMIN 권한을 가진 사람만
				.requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
				// 그 밖의 모든 요청은 "로그인한 사람"이면 접근 가능
				.anyRequest().authenticated()
			)
			// 2) 폼 로그인 설정 (아이디/비밀번호를 입력받는 전통적인 방식)
			.formLogin(form -> form
				.loginPage("/login")            // 우리가 만든 로그인 페이지 주소
				.loginProcessingUrl("/login")   // 이 주소로 POST가 오면 Security가 로그인 처리(우리가 코드 짤 필요 없음)
				.defaultSuccessUrl("/", true)   // 로그인 성공 시 홈으로 이동
				.failureUrl("/login?error")     // 실패 시 이 주소로 (화면에서 에러 메시지 표시)
				.permitAll()
			)
			// 3) 로그아웃 설정
			.logout(logout -> logout
				.logoutUrl("/logout")           // 이 주소로 POST가 오면 세션을 없앰
				.logoutSuccessUrl("/?logout")   // 로그아웃 후 홈으로
				.permitAll()
			);

		// 4) H2 콘솔 전용 예외 처리 (학습 편의 목적)
		//    - H2 콘솔은 자체 폼을 쓰기 때문에 CSRF 보호를 꺼 줘야 하고,
		//    - 화면을 <iframe> 안에 그리기 때문에 같은 사이트의 frame 은 허용해야 합니다.
		//    실무 서비스에서는 보통 H2 콘솔 자체를 열지 않습니다.
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
		http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

		return http.build();
	}
}
