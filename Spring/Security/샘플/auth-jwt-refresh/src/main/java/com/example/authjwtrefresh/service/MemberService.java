package com.example.authjwtrefresh.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.authjwtrefresh.domain.Member;
import com.example.authjwtrefresh.domain.Role;
import com.example.authjwtrefresh.repository.MemberRepository;

/** 회원 가입 로직. 세션 데모와 동일하게 비밀번호를 BCrypt 로 해시해서 저장합니다. */
@Service
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public Long signup(String username, String rawPassword, String displayName) {
		if (memberRepository.existsByUsername(username)) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + username);
		}
		String encodedPassword = passwordEncoder.encode(rawPassword);
		Member member = new Member(username, encodedPassword, displayName, Role.USER);
		return memberRepository.save(member).getId();
	}
}
