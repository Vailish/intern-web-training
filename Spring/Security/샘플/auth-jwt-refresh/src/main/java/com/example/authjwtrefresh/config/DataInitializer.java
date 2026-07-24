package com.example.authjwtrefresh.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.authjwtrefresh.domain.Member;
import com.example.authjwtrefresh.domain.Role;
import com.example.authjwtrefresh.repository.MemberRepository;

/**
 * 실습용 계정 시드. 세션 데모와 동일합니다.
 *  - admin / admin123 → ADMIN
 *  - user  / user123  → USER
 * (교육용이므로 코드에 비밀번호를 적어 둡니다. 실제 서비스에서는 하지 않습니다.)
 */
@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner initMembers(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (memberRepository.count() > 0) {
				return;
			}
			memberRepository.save(new Member(
					"admin", passwordEncoder.encode("admin123"), "관리자", Role.ADMIN));
			memberRepository.save(new Member(
					"user", passwordEncoder.encode("user123"), "일반회원", Role.USER));
		};
	}
}
