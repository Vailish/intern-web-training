package com.example.authsession.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.authsession.domain.Member;
import com.example.authsession.repository.MemberRepository;

/**
 * Spring Security가 로그인 처리를 할 때 "이 아이디의 회원 정보를 가져와" 라고
 * 물어보는 통로입니다. 즉, 우리 DB(member 테이블)와 Security를 연결하는 다리 역할.
 *
 * 동작 흐름(세션 방식):
 *  1) 사용자가 로그인 폼에 아이디/비밀번호를 입력하고 제출
 *  2) Security가 아이디로 loadUserByUsername() 를 호출 → 우리가 DB에서 회원을 찾아 반환
 *  3) Security가 폼의 비밀번호를 BCrypt로 해시해 DB의 해시값과 비교
 *  4) 일치하면 인증 성공 → 세션 생성, 이후 요청은 세션 쿠키로 신분 확인
 *
 * ✅ 우리가 직접 비밀번호를 비교하지 않습니다. 비교는 Security가 알아서 합니다.
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

		// Security가 이해하는 UserDetails 객체로 변환해서 돌려줍니다.
		// 권한 이름은 "ROLE_" 접두사를 붙여야 hasRole("ADMIN") 같은 규칙과 맞물립니다.
		return User.builder()
				.username(member.getUsername())
				.password(member.getPassword()) // 이미 해시된 값
				.authorities(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
				.build();
	}
}
