package com.example.authjwtrefresh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authjwtrefresh.domain.Member;
import com.example.authjwtrefresh.domain.RefreshToken;
import com.example.authjwtrefresh.dto.AuthDtos.LoginRequest;
import com.example.authjwtrefresh.dto.AuthDtos.MessageResponse;
import com.example.authjwtrefresh.dto.AuthDtos.RefreshRequest;
import com.example.authjwtrefresh.dto.AuthDtos.SignupRequest;
import com.example.authjwtrefresh.dto.AuthDtos.TokenResponse;
import com.example.authjwtrefresh.repository.MemberRepository;
import com.example.authjwtrefresh.security.JwtTokenProvider;
import com.example.authjwtrefresh.service.MemberService;
import com.example.authjwtrefresh.service.RefreshTokenService;

/**
 * 회원가입, 로그인(토큰 2종 발급), 리프레시(액세스 토큰 재발급), 로그아웃(리프레시 토큰 폐기)을 담당합니다.
 * 네 주소(/api/auth/**) 모두 액세스 토큰 없이 접근할 수 있도록 SecurityConfig 에서 허용합니다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;
	private final RefreshTokenService refreshTokenService;

	public AuthController(MemberService memberService,
						  MemberRepository memberRepository,
						  AuthenticationManager authenticationManager,
						  JwtTokenProvider tokenProvider,
						  RefreshTokenService refreshTokenService) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
		this.refreshTokenService = refreshTokenService;
	}

	/** 회원가입: 성공 201, 아이디 중복 400. */
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
	 * 로그인: 아이디/비밀번호가 맞으면 액세스 토큰 + 리프레시 토큰을 함께 발급합니다.
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));

			String role = authentication.getAuthorities().iterator().next()
					.getAuthority().replace("ROLE_", "");

			String accessToken = tokenProvider.createAccessToken(authentication.getName(), role);
			String refreshToken = refreshTokenService.issue(authentication.getName());

			return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken, role));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(401)
					.body(new MessageResponse("아이디 또는 비밀번호가 올바르지 않습니다."));
		}
	}

	/**
	 * 리프레시: 유효한 리프레시 토큰을 주면 "새 액세스 토큰 + 새 리프레시 토큰"을 발급합니다(회전).
	 * 재로그인 없이 로그인 상태를 이어갈 수 있는 핵심 엔드포인트입니다.
	 */
	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
		try {
			// 1) 리프레시 토큰 검증 (DB에 있고 만료되지 않았는지)
			RefreshToken refreshToken = refreshTokenService.validateAndGet(request.refreshToken());

			// 2) 새 액세스 토큰을 만들려면 현재 권한이 필요 → 회원을 다시 조회
			//    (권한은 관리자가 바뀌었을 수 있으니 토큰에 저장된 값이 아니라 DB의 최신 값을 씀)
			Member member = memberRepository.findByUsername(refreshToken.getUsername())
					.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

			String newAccessToken = tokenProvider.createAccessToken(member.getUsername(), member.getRole().name());

			// 3) 리프레시 토큰도 새 것으로 회전 (옛 토큰은 폐기됨)
			String newRefreshToken = refreshTokenService.rotate(request.refreshToken(), member.getUsername());

			return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken, member.getRole().name()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(401).body(new MessageResponse(e.getMessage()));
		}
	}

	/** 로그아웃: 리프레시 토큰을 폐기해 더 이상 재발급받지 못하게 합니다. */
	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(@RequestBody RefreshRequest request) {
		refreshTokenService.delete(request.refreshToken());
		return ResponseEntity.ok(new MessageResponse("로그아웃되었습니다. (리프레시 토큰 폐기됨)"));
	}
}
