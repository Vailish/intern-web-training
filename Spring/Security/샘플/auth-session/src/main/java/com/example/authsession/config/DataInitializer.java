package com.example.authsession.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.authsession.domain.Member;
import com.example.authsession.domain.Role;
import com.example.authsession.repository.MemberRepository;

/**
 * 앱이 시작될 때 실습용 계정 2개를 미리 넣어 둡니다.
 * (인메모리 H2라서 앱을 끄면 사라지고, 다시 켜면 이 코드가 다시 채워 넣습니다.)
 *
 *  - admin / admin123  → 관리자(ADMIN)  : /admin 접근 가능
 *  - user  / user123   → 일반 회원(USER) : /admin 접근하면 거부됨
 *
 * ⚠️ 이렇게 코드에 비밀번호를 적어 두는 것은 "교육용 시드 데이터"이기 때문입니다.
 *    실제 서비스에서는 절대 하지 않습니다.
 */
@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner initMembers(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (memberRepository.count() > 0) {
				return; // 이미 데이터가 있으면 넣지 않음
			}
			memberRepository.save(new Member(
					"admin", passwordEncoder.encode("admin123"), "관리자", Role.ADMIN));
			memberRepository.save(new Member(
					"user", passwordEncoder.encode("user123"), "일반회원", Role.USER));
		};
	}
}
