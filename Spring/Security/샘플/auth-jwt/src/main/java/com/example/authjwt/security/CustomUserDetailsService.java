package com.example.authjwt.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.authjwt.domain.Member;
import com.example.authjwt.repository.MemberRepository;

/**
 * 로그인(POST /api/auth/login) 시점에만 사용됩니다.
 * AuthenticationManager 가 이 서비스로 회원을 찾고 비밀번호를 대조합니다.
 *
 * ⚠️ 세션 방식과 다른 점: 로그인 이후의 일반 요청에서는 이 서비스를 타지 않습니다.
 *    이후 요청은 JwtAuthenticationFilter 가 "토큰만 보고" 신분을 판단합니다(무상태).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	public CustomUserDetailsService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = memberRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다: " + username));

		return User.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.authorities(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
				.build();
	}
}
