package com.example.authjwtrefresh.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.authjwtrefresh.domain.RefreshToken;
import com.example.authjwtrefresh.repository.RefreshTokenRepository;

/**
 * 리프레시 토큰의 발급·검증·회전·폐기를 담당합니다.
 *
 * 리프레시 토큰은 JWT 가 아니라 **예측 불가능한 임의 문자열(UUID)** 입니다.
 * JWT 로 만들지 않는 이유: 어차피 DB에 저장해 서버가 통제할 것이므로, 굳이 서명/파싱이 필요 없고
 * "서버가 폐기할 수 있다"는 점이 핵심이기 때문입니다.
 */
@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final long validityDays;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
							   @Value("${jwt.refresh-token-validity-days}") long validityDays) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.validityDays = validityDays;
	}

	/** 새 리프레시 토큰을 발급해 DB에 저장하고 문자열을 반환합니다. */
	@Transactional
	public String issue(String username) {
		String token = UUID.randomUUID().toString();
		Instant expiresAt = Instant.now().plus(validityDays, ChronoUnit.DAYS);
		refreshTokenRepository.save(new RefreshToken(token, username, expiresAt));
		return token;
	}

	/**
	 * 들어온 리프레시 토큰이 유효한지 검사하고, 유효하면 해당 엔티티를 반환합니다.
	 *  - DB에 없으면 → 예외 (위조되었거나 이미 회전/폐기된 토큰)
	 *  - 만료됐으면 → DB에서 지우고 예외
	 */
	@Transactional
	public RefreshToken validateAndGet(String token) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

		if (refreshToken.isExpired()) {
			refreshTokenRepository.delete(refreshToken);
			throw new IllegalArgumentException("만료된 리프레시 토큰입니다. 다시 로그인하세요.");
		}
		return refreshToken;
	}

	/**
	 * 회전(rotation): 기존 리프레시 토큰을 폐기하고 새 것을 발급합니다.
	 * 리프레시할 때마다 토큰을 새로 갈아 주면, 유출된 옛 토큰은 곧 무용지물이 되고
	 * 옛 토큰이 다시 사용되면 "DB에 없음 → 거부"로 재사용을 감지할 수 있습니다.
	 */
	@Transactional
	public String rotate(String oldToken, String username) {
		refreshTokenRepository.deleteByToken(oldToken);
		return issue(username);
	}

	/** 로그아웃: 해당 리프레시 토큰을 폐기합니다. (없으면 조용히 통과) */
	@Transactional
	public void delete(String token) {
		refreshTokenRepository.deleteByToken(token);
	}
}
