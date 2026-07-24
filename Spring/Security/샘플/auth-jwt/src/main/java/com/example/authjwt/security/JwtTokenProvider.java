package com.example.authjwt.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT(JSON Web Token)를 "만들고", "검증하고", "안의 내용을 읽는" 도구입니다.
 *
 * JWT 한 줄은 점(.)으로 구분된 3조각입니다:  header . payload . signature
 *  - header    : 어떤 알고리즘으로 서명했는지
 *  - payload   : 실제 내용(누구인지=subject, 권한=role, 만료시각 등). Base64라 "누구나 읽을 수 있음"
 *  - signature : 비밀키로 만든 서명. 내용이 위변조되면 서명이 맞지 않아 검증에 실패함
 *
 * ⚠️ payload 는 암호화가 아니라 인코딩(Base64)일 뿐입니다. 즉 "누구나 열어 볼 수 있으니"
 *    비밀번호 같은 민감정보를 넣으면 안 됩니다. 위변조만 막아 줄 뿐입니다.
 */
@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long validityMillis;

	/**
	 * 비밀키와 만료 시간을 설정 파일(application.properties)에서 주입받습니다.
	 * HMAC 서명은 키가 최소 256비트(32바이트) 이상이어야 하므로 secret 은 충분히 길어야 합니다.
	 * (jjwt 는 키 길이에 맞춰 알고리즘을 자동 선택합니다. 32바이트=HS256, 64바이트=HS512)
	 */
	public JwtTokenProvider(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-minutes}") long expirationMinutes) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.validityMillis = expirationMinutes * 60 * 1000;
	}

	/** 로그인 성공 시 호출: 사용자 아이디와 권한을 담은 서명된 토큰을 만들어 반환합니다. */
	public String createToken(String username, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validityMillis);
		return Jwts.builder()
				.subject(username)         // 이 토큰의 주인(누구인지)
				.claim("role", role)       // 커스텀 정보: 권한
				.issuedAt(now)             // 발급 시각
				.expiration(expiry)        // 만료 시각 (지나면 검증 실패)
				.signWith(key)             // 비밀키로 서명 (HS256 자동 선택)
				.compact();                // 최종 문자열로 압축
	}

	/** 토큰이 우리 키로 서명되었고 만료되지 않았는지 검사합니다. 문제가 있으면 false. */
	public boolean validateToken(String token) {
		try {
			parse(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			// 서명 불일치, 만료, 형식 오류 등 모든 실패를 여기서 잡습니다.
			return false;
		}
	}

	/** 토큰에서 사용자 아이디(subject)를 꺼냅니다. */
	public String getUsername(String token) {
		return parse(token).getSubject();
	}

	/** 토큰에서 권한(role)을 꺼냅니다. */
	public String getRole(String token) {
		return parse(token).get("role", String.class);
	}

	/** 서명을 검증하면서 payload(Claims)를 꺼내는 공통 로직. 실패하면 예외가 발생합니다. */
	private Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(key)               // 이 키로 서명 검증
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
