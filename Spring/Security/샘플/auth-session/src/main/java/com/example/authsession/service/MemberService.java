package com.example.authsession.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.authsession.domain.Member;
import com.example.authsession.domain.Role;
import com.example.authsession.repository.MemberRepository;

/**
 * 회원 가입 등 회원과 관련된 업무 로직을 담당합니다.
 */
@Service
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	// 생성자 주입: 스프링이 MemberRepository 와 PasswordEncoder(빈)를 자동으로 넣어 줍니다.
	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * 새 회원을 저장합니다.
	 *
	 * 핵심 포인트:
	 *  1) 아이디 중복을 먼저 확인합니다.
	 *  2) 사용자가 입력한 비밀번호(rawPassword)를 그대로 저장하지 않고,
	 *     passwordEncoder.encode() 로 "해시"한 값을 저장합니다.
	 *     → DB가 유출되어도 원문 비밀번호는 알 수 없습니다.
	 */
	@Transactional
	public Long signup(String username, String rawPassword, String displayName) {
		if (memberRepository.existsByUsername(username)) {
			// 실무에서는 커스텀 예외를 만들지만, 교육용으로는 메시지가 명확한 기본 예외를 씁니다.
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + username);
		}

		String encodedPassword = passwordEncoder.encode(rawPassword);
		Member member = new Member(username, encodedPassword, displayName, Role.USER);
		return memberRepository.save(member).getId();
	}
}
