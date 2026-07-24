package com.example.authjwtrefresh.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * "액세스 토큰"(JWT)을 만들고/검증하고/내용을 읽는 도구입니다.
 *
 * auth-jwt 샘플의 것과 거의 같지만, 이 샘플에서는 액세스 토큰의 수명을 매우 짧게(기본 60초) 잡습니다.
 * 짧게 잡는 이유: 토큰이 탈취되어도 금방 만료되어 피해 창(window)이 작아지기 때문입니다.
 * 그렇다고 사용자에게 1분마다 재로그인을 시킬 수는 없으므로, 만료되면 "리프레시 토큰"으로
 * 새 액세스 토큰을 받아 옵니다. (그 처리는 RefreshTokenService / AuthController 참고)
 *
 * ⚠️ 리프레시 토큰은 JWT 가 아닙니다. 이 클래스는 오직 "액세스 토큰"만 다룹니다.
 */
@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long validityMillis;

	public JwtTokenProvider(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.access-token-validity-seconds}") long accessTokenValiditySeconds) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.validityMillis = accessTokenValiditySeconds * 1000;
	}

	/** 로그인/리프레시 성공 시 호출: 사용자 아이디와 권한을 담은 짧은 수명의 액세스 토큰 발급. */
	public String createAccessToken(String username, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validityMillis);
		return Jwts.builder()
				.id(UUID.randomUUID().toString()) // jti: 토큰마다 고유한 ID → 같은 순간 발급해도 항상 다른 토큰이 됨
				.subject(username)
				.claim("role", role)
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}

	/** 액세스 토큰이 우리 키로 서명되었고 만료되지 않았는지 검사. */
	public boolean validateToken(String token) {
		try {
			parse(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public String getUsername(String token) {
		return parse(token).getSubject();
	}

	public String getRole(String token) {
		return parse(token).get("role", String.class);
	}

	private Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
