package com.example.authsession.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.authsession.domain.Member;

/**
 * 회원 조회/저장을 담당하는 리포지토리.
 * JpaRepository 를 상속하면 save/findById 등 기본 메서드가 자동 제공됩니다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

	/**
	 * 로그인 아이디로 회원을 찾습니다.
	 * 로그인할 때(CustomUserDetailsService)와 아이디 중복 검사(회원가입)에 사용합니다.
	 * 없을 수도 있으므로 Optional 로 감싸서 반환합니다.
	 */
	Optional<Member> findByUsername(String username);

	/** 아이디가 이미 존재하는지 확인 (회원가입 중복 체크용). */
	boolean existsByUsername(String username);
}
