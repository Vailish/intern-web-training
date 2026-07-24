package com.example.authjwtrefresh.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT 방식의 보안 설정. 세션 방식과 대비되는 3가지 핵심이 여기 다 들어 있습니다.
 *
 *  1) 세션을 만들지 않는다 (STATELESS) → 서버가 로그인 상태를 기억하지 않음
 *  2) CSRF 보호를 끈다 → 쿠키를 안 쓰므로 CSRF 공격 대상이 아님(대신 토큰을 헤더로 보냄)
 *  3) 우리가 만든 JwtAuthenticationFilter 를 체인에 끼워 넣는다 → 매 요청 토큰 검사
 */
@Configuration
public class SecurityConfig {

	private final JwtTokenProvider tokenProvider;

	public SecurityConfig(JwtTokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 로그인 시 아이디/비밀번호를 검증해 주는 AuthenticationManager 를 빈으로 꺼냅니다.
	 * AuthController 의 로그인 처리에서 이 빈을 사용합니다.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// (2) 쿠키/세션을 쓰지 않으므로 CSRF 보호가 필요 없습니다.
			.csrf(csrf -> csrf.disable())
			// (1) 세션을 절대 만들지 않습니다(무상태). 서버는 요청 사이에 아무 것도 기억하지 않음.
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// URL별 접근 권한
			.authorizeHttpRequests(auth -> auth
				// 데모 페이지/정적 리소스와 회원가입·로그인은 토큰 없이 허용
				.requestMatchers("/", "/index.html", "/css/**", "/js/**", "/api/auth/**").permitAll()
				// 관리자 전용 API 는 ADMIN 권한 필요
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				// 그 외 모든 API 는 유효한 토큰(로그인) 필요
				.anyRequest().authenticated()
			)
			// 인증/인가 실패 시 로그인 페이지로 보내지 않고 JSON 으로 상태코드를 돌려줍니다(REST API 답게).
			.exceptionHandling(ex -> ex
				// 토큰이 없거나 유효하지 않을 때 → 401 Unauthorized
				.authenticationEntryPoint((request, response, e) -> {
					response.setStatus(401);
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"message\":\"인증이 필요합니다. 토큰이 없거나 유효하지 않습니다.\"}");
				})
				// 로그인은 했지만 권한이 부족할 때 → 403 Forbidden
				.accessDeniedHandler((request, response, e) -> {
					response.setStatus(403);
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"message\":\"권한이 없습니다.\"}");
				})
			)
			// (3) 우리 필터를 기본 로그인 필터(UsernamePasswordAuthenticationFilter) 앞에 끼웁니다.
			.addFilterBefore(new JwtAuthenticationFilter(tokenProvider),
					UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
