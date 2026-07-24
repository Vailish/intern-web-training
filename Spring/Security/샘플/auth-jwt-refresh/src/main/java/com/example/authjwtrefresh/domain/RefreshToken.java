package com.example.authjwtrefresh.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 리프레시 토큰을 DB에 저장하기 위한 엔티티.
 *
 * 액세스 토큰(JWT)은 서버에 저장하지 않습니다(무상태). 반대로 리프레시 토큰은 **일부러 DB에 저장**합니다.
 * 이유:
 *   1) 서버가 "누구에게 어떤 리프레시 토큰을 줬는지" 알고 있어야 강제 폐기(로그아웃)가 가능합니다.
 *   2) 재사용 공격 감지가 가능합니다(이미 회전되어 삭제된 토큰이 다시 오면 거부).
 *
 * 즉 이 샘플은 "완전한 무상태"가 아니라, 무상태(액세스)와 서버 상태(리프레시)를 **절충**한 구조입니다.
 */
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 리프레시 토큰 문자열. 예측 불가능한 임의값(UUID)이며 중복되면 안 됩니다. */
	@Column(nullable = false, unique = true, length = 200)
	private String token;

	/** 이 토큰의 주인(회원 아이디). */
	@Column(nullable = false, length = 50)
	private String username;

	/** 만료 시각. 이 시각이 지나면 새 액세스 토큰을 발급해 주지 않습니다. */
	@Column(nullable = false)
	private Instant expiresAt;

	protected RefreshToken() {
	}

	public RefreshToken(String token, String username, Instant expiresAt) {
		this.token = token;
		this.username = username;
		this.expiresAt = expiresAt;
	}

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public String getUsername() {
		return username;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	/** 만료됐는지 확인. */
	public boolean isExpired() {
		return expiresAt.isBefore(Instant.now());
	}
}
