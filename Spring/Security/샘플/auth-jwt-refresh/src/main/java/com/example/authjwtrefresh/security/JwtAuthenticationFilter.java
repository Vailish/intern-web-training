package com.example.authjwtrefresh.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 모든 요청이 컨트롤러에 도달하기 "전"에 한 번씩 실행되는 필터입니다.
 * 요청 헤더의 JWT 토큰을 검사해서, 유효하면 "이 요청은 이 사용자가 보낸 것"이라고
 * SecurityContext 에 등록합니다. 이후 Security는 그 정보로 접근 권한을 판단합니다.
 *
 * 핵심(세션과의 차이):
 *  - 서버는 아무 것도 기억하지 않습니다. 매 요청마다 토큰만 보고 신분을 새로 판단합니다.
 *  - 그래서 DB 조회조차 하지 않고(토큰 안의 role 을 그대로 신뢰) 동작할 수 있습니다.
 *    토큰은 서명되어 있어 위변조가 불가능하기 때문입니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain chain) throws ServletException, IOException {

		String token = resolveToken(request);

		// 토큰이 있고 유효할 때만 "인증된 사용자"로 등록합니다.
		// 토큰이 없거나 잘못되면 그냥 통과시키고, 이후 인가 단계에서 401/403 이 결정됩니다.
		if (token != null && tokenProvider.validateToken(token)) {
			String username = tokenProvider.getUsername(token);
			String role = tokenProvider.getRole(token);

			var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
			var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		chain.doFilter(request, response); // 다음 필터/컨트롤러로 진행
	}

	/** "Authorization: Bearer xxxxx" 헤더에서 토큰 부분(xxxxx)만 잘라 냅니다. */
	private String resolveToken(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7); // "Bearer " 7글자 이후가 실제 토큰
		}
		return null;
	}
}
