package com.example.authjwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authjwt.dto.AuthDtos.LoginRequest;
import com.example.authjwt.dto.AuthDtos.LoginResponse;
import com.example.authjwt.dto.AuthDtos.MessageResponse;
import com.example.authjwt.dto.AuthDtos.SignupRequest;
import com.example.authjwt.security.JwtTokenProvider;
import com.example.authjwt.service.MemberService;

/**
 * 회원가입과 로그인(토큰 발급)을 담당하는 REST 컨트롤러.
 * 이 두 주소(/api/auth/**)는 토큰 없이 접근할 수 있도록 SecurityConfig 에서 허용했습니다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final MemberService memberService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;

	public AuthController(MemberService memberService,
						  AuthenticationManager authenticationManager,
						  JwtTokenProvider tokenProvider) {
		this.memberService = memberService;
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
	}

	/** 회원가입: 성공하면 201 Created. 아이디가 중복이면 400. */
	@PostMapping("/signup")
	public ResponseEntity<MessageResponse> signup(@RequestBody SignupRequest request) {
		try {
			Long id = memberService.signup(request.username(), request.password(), request.displayName());
			return ResponseEntity.status(201).body(new MessageResponse("회원가입 완료 (id=" + id + ")"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
		}
	}

	/**
	 * 로그인: 아이디/비밀번호가 맞으면 JWT 를 발급해 돌려줍니다.
	 *
	 *  1) AuthenticationManager 가 CustomUserDetailsService + BCrypt 로 비밀번호를 검증
	 *  2) 성공하면 인증 객체에서 권한을 꺼내 토큰에 담아 발급
	 *  3) 실패하면 401 (AuthenticationException 발생)
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));

			// authorities 는 "ROLE_USER" 형태 → 토큰에는 접두사를 뺀 "USER" 만 담습니다.
			String role = authentication.getAuthorities().iterator().next()
					.getAuthority().replace("ROLE_", "");

			String token = tokenProvider.createToken(authentication.getName(), role);
			return ResponseEntity.ok(new LoginResponse(token, role));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(401)
					.body(new MessageResponse("아이디 또는 비밀번호가 올바르지 않습니다."));
		}
	}
}
