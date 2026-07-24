package com.example.authsession.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 회원 한 명을 나타내는 엔티티. DB의 member 테이블과 매핑됩니다.
 *
 * ⚠️ 보안 핵심: password 컬럼에는 절대로 사용자가 입력한 원문 비밀번호를
 *    그대로 저장하지 않습니다. 항상 BCrypt 로 "단방향 암호화(해시)"한 값을 저장합니다.
 *    (암호화는 MemberService.signup() 에서 수행합니다)
 */
@Entity
@Table(name = "member")
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 로그인 아이디. 중복되면 안 되므로 unique 제약을 겁니다. */
	@Column(nullable = false, unique = true, length = 50)
	private String username;

	/** BCrypt 로 해시된 비밀번호. 해시 결과가 길기 때문에 넉넉히 길이를 잡습니다. */
	@Column(nullable = false, length = 100)
	private String password;

	/** 화면에 보여 줄 이름(별명). */
	@Column(nullable = false, length = 50)
	private String displayName;

	/** 권한(USER / ADMIN). 문자열로 DB에 저장합니다. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	// JPA 는 기본 생성자가 필요합니다.
	protected Member() {
	}

	public Member(String username, String password, String displayName, Role role) {
		this.username = username;
		this.password = password;
		this.displayName = displayName;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Role getRole() {
		return role;
	}
}
