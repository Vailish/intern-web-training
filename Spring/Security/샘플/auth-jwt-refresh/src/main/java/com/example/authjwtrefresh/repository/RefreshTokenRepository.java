package com.example.authjwtrefresh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.authjwtrefresh.domain.RefreshToken;

/** 리프레시 토큰 저장/조회/삭제. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	/** 토큰 문자열로 찾기 (리프레시 요청이 유효한지 검사할 때). */
	Optional<RefreshToken> findByToken(String token);

	/** 특정 토큰 삭제 (회전 시 옛 토큰 제거, 로그아웃 시 폐기). */
	void deleteByToken(String token);

	/** 특정 회원의 모든 리프레시 토큰 삭제 (모든 기기에서 강제 로그아웃 용도). */
	void deleteByUsername(String username);
}
