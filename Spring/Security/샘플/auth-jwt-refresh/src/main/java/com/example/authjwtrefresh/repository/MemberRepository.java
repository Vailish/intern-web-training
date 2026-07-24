package com.example.authjwtrefresh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.authjwtrefresh.domain.Member;

/** 회원 조회/저장 리포지토리. */
public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByUsername(String username);

	boolean existsByUsername(String username);
}
