# 03. JWT 토큰 방식 — auth-jwt 만들기

> **이 문서에서 배우는 것**
> - **JWT(JSON Web Token)** 의 구조와 서명 원리
> - **무상태(stateless)**: 서버가 로그인 상태를 기억하지 않는다는 것의 의미
> - 로그인 시 토큰 발급, 이후 요청마다 토큰 검사(**인증 필터 직접 구현**)
> - **jjwt** 라이브러리 사용법과 함정
> - 완성본: [샘플/auth-jwt](./샘플/auth-jwt/)

---

## 1. 왜 JWT인가 — 세션 방식의 한계에서 출발

세션 방식(02 문서)은 서버가 로그인 상태를 **기억**했습니다. 이게 문제가 되는 순간이 있습니다.

- **서버가 여러 대**로 늘어나면? A 서버에 로그인했는데 다음 요청이 B 서버로 가면 B는 나를 모릅니다.
- **프론트/백이 분리**되어 있고, 상대가 브라우저가 아니라 **모바일 앱**이면? 쿠키 방식이 잘 안 맞습니다.

**JWT** 는 발상을 뒤집습니다.

> 서버가 기억하지 말고, **"내가 로그인했다"는 증명서를 클라이언트가 직접 들고 다니게** 하자.
> 단, 위조할 수 없도록 서버가 **서명**한 증명서로.

이 증명서가 JWT입니다. 서버는 매 요청마다 **토큰의 서명만 확인**하면 되므로 아무 것도 기억할 필요가 없습니다.
이것을 **무상태(stateless)** 라고 합니다.

---

## 2. JWT의 구조 — 점(.)으로 나뉜 3조각

JWT는 이렇게 생긴 긴 문자열입니다.

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTc4NH0.0Cq3r9MRs69U...
└──── header ────┘ └──────────── payload ────────────┘ └───── signature ─────┘
```

| 조각 | 담긴 내용 | 설명 |
|---|---|---|
| **Header** | 서명 알고리즘 | 예: `{"alg":"HS512"}` |
| **Payload** | 실제 정보(클레임) | 누구인지(`sub`), 권한(`role`), 만료시각(`exp`) 등 |
| **Signature** | 서명 | 비밀키로 header+payload를 서명한 값. **위변조 감지용** |

header와 payload는 **Base64로 인코딩**되어 있을 뿐입니다. 즉 **누구나 디코드해서 읽을 수 있습니다.**
([jwt.io](https://jwt.io) 에 붙여넣으면 내용이 다 보입니다.)

> ⚠️ **가장 중요한 오해 — "JWT는 암호화가 아니다"**
> Payload는 **암호화가 아니라 인코딩**입니다. 누구나 열어 볼 수 있으므로,
> **비밀번호·주민등록번호 같은 민감정보를 절대 넣으면 안 됩니다.**
> 서명이 막아 주는 것은 **위변조**(내용을 바꾸면 서명이 깨짐)일 뿐, **엿보기가 아닙니다.**

### 서명이 위조를 막는 원리

서명은 **서버만 아는 비밀키**로 만듭니다. 공격자가 payload의 `"role":"USER"` 를 `"ADMIN"` 으로
바꾸면, 바뀐 내용에 맞는 서명을 다시 만들어야 하는데 **비밀키가 없어서 만들 수 없습니다.**
그래서 서버가 검증할 때 서명이 맞지 않아 **즉시 거부**됩니다.

---

## 3. 프로젝트 만들기 — jjwt 의존성

의존성은 **Spring Security**, **Spring Web(webmvc)**, **Spring Data JPA**, **H2** 에
JWT 라이브러리 **jjwt** 를 더합니다. 화면(타임리프)은 필요 없습니다(API 서버니까).

```gradle
ext { jjwtVersion = '0.12.6' }

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    implementation "io.jsonwebtoken:jjwt-api:${jjwtVersion}"
    runtimeOnly   "io.jsonwebtoken:jjwt-impl:${jjwtVersion}"
    runtimeOnly   "io.jsonwebtoken:jjwt-jackson:${jjwtVersion}"

    runtimeOnly 'com.h2database:h2'
}
```

> ⚠️ **함정 — jjwt는 세 조각을 버전 맞춰 넣어야 합니다.**
> `jjwt-api`(코드에서 사용) + `jjwt-impl`(구현체) + `jjwt-jackson`(JSON 처리) 세 개를 **같은 버전**으로
> 넣어야 합니다. `impl`/`jackson` 을 빠뜨리면 컴파일은 되지만 **실행 중에** 토큰 생성이 실패합니다.

`Member` 엔티티·리포지토리·`CustomUserDetailsService`·`MemberService`(회원가입)는 **세션 샘플과 사실상 동일**합니다.
달라지는 것은 지금부터 나오는 **토큰 발급기·검사 필터·보안 설정** 세 가지입니다.

---

## 4. 토큰을 만들고 검증하는 도구 — `JwtTokenProvider`

JWT를 **생성/검증/해독**하는 핵심 클래스입니다.

```java
@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long validityMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-minutes}") long minutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityMillis = minutes * 60 * 1000;
    }

    // 로그인 성공 시: 토큰 발급
    public String createToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)                                  // 누구인지
                .claim("role", role)                                // 권한
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validityMillis)) // 만료
                .signWith(key)                                      // 서명
                .compact();
    }

    // 매 요청 시: 서명·만료 검증
    public boolean validateToken(String token) {
        try { parse(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public String getUsername(String token) { return parse(token).getSubject(); }
    public String getRole(String token)     { return parse(token).get("role", String.class); }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                   .parseSignedClaims(token).getPayload();
    }
}
```

> ⚠️ **함정 — 비밀키는 최소 32바이트(256비트)**
> `Keys.hmacShaKeyFor()` 는 키가 짧으면 예외를 던집니다. HS256 최소가 32바이트입니다.
> 그리고 jjwt는 **키 길이에 맞춰 알고리즘을 자동 선택**합니다 — 32바이트면 HS256, 64바이트면 HS512.
> (그래서 우리 샘플 토큰 헤더는 `HS512` 로 나옵니다. 설정의 `jwt.secret` 이 64바이트이기 때문입니다.)

> ⚠️ **함정 — jjwt 0.12.x 는 메서드 이름이 바뀌었습니다.**
> 옛날 자료의 `setSubject()`/`setExpiration()`/`parseClaimsJws()` 는 각각
> `subject()`/`expiration()`/`parseSignedClaims()` 로 바뀌었습니다. 검색 코드가 안 되면 이걸 의심하세요.

```properties
# application.properties — 실제 서비스에선 소스가 아니라 환경변수로 주입!
jwt.secret=this-is-a-demo-secret-key-please-change-in-production-1234567890
jwt.expiration-minutes=30
```

---

## 5. 매 요청마다 토큰 검사 — `JwtAuthenticationFilter`

세션 방식은 Security의 로그인 필터를 그대로 썼지만, JWT는 **우리가 필터를 만들어** 필터체인에 끼웁니다.
이 필터는 모든 요청에서 `Authorization` 헤더의 토큰을 꺼내 검사하고, 유효하면 "인증된 사용자"로 등록합니다.

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    // 생성자 생략

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(request); // "Bearer xxx" 에서 xxx 추출
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsername(token);
            String role     = tokenProvider.getRole(token);
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication); // 현재 요청 인증 등록
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;
    }
}
```

> 💡 **무상태의 실감**: 이 필터는 **DB를 전혀 조회하지 않습니다.** 토큰 안의 `role` 을 그대로 신뢰합니다.
> 토큰이 서명되어 위조가 불가능하기 때문입니다. 로그인 이후에는 `CustomUserDetailsService` 조차 타지 않습니다.

---

## 6. 보안 설정 — `SecurityConfig` (세션 방식과 대조)

JWT 설정의 3대 특징이 여기 다 있습니다.

```java
@Configuration
public class SecurityConfig {
    private final JwtTokenProvider tokenProvider;
    // 생성자 생략

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // 로그인 검증용 AuthenticationManager 를 빈으로 꺼내 둠 (AuthController 에서 사용)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                                              // ① CSRF 끔
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ② 세션 안 만듦
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> res.setStatus(401)) // 토큰 없음/무효 → 401
                .accessDeniedHandler((req, res, e) -> res.setStatus(403))      // 권한 부족 → 403
            )
            .addFilterBefore(new JwtAuthenticationFilter(tokenProvider),       // ③ 우리 필터 끼움
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

| # | 설정 | 세션 방식과의 차이 | 이유 |
|---|---|---|---|
| ① | `csrf().disable()` | 세션은 CSRF **켬** | 쿠키를 안 쓰므로 CSRF 공격 대상이 아님 |
| ② | `STATELESS` | 세션은 세션 **생성** | 서버가 상태를 기억하지 않음 |
| ③ | 커스텀 필터 추가 | 세션은 `formLogin` 사용 | 로그인 화면이 아니라 토큰으로 인증 |

> ⚠️ **함정 — 인증 실패 시 "로그인 페이지로 리다이렉트"하면 안 됩니다.**
> API 서버는 302 리다이렉트 대신 **401/403 상태코드**를 돌려줘야 합니다.
> `exceptionHandling` 으로 `authenticationEntryPoint`(401)와 `accessDeniedHandler`(403)를 지정하지 않으면
> 기본 동작이 엉뚱하게 나올 수 있습니다.

---

## 7. 회원가입 · 로그인(토큰 발급) API — `AuthController`

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // 의존성: MemberService, AuthenticationManager, JwtTokenProvider (생략)

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            String token = tokenProvider.createToken(auth.getName(), role);
            return ResponseEntity.ok(new LoginResponse(token, role)); // { "token": "...", "role": "ADMIN" }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new MessageResponse("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }
}
```

`AuthenticationManager` 가 `CustomUserDetailsService` + BCrypt로 비밀번호를 검증해 줍니다.
성공하면 권한을 꺼내 토큰에 담아 발급합니다.

---

## 8. 실행하고 눈으로 확인하기

```bash
cd Spring/Security/샘플/auth-jwt
./gradlew bootRun
```

### 방법 A — 데모 페이지(가장 쉬움)

브라우저에서 http://localhost:8080 접속. 버튼을 순서대로 누르면
**로그인 → 토큰 발급 → 토큰을 헤더에 실어 보호된 API 호출** 전 과정이 화면에 보입니다.

![JWT 데모](./images/jwt_demo_admin.png)

데모 페이지의 JS 한 줄이 이 방식의 전부입니다 — **보호된 API를 부를 때 토큰을 헤더에 싣습니다.**

```javascript
fetch('/api/me', { headers: { 'Authorization': 'Bearer ' + token } });
```

### 방법 B — 명령줄로 직접(원리 확인)

```bash
# 1) 로그인 → 토큰 받기
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# → {"token":"eyJhbGci...","role":"ADMIN"}

# 2) 받은 토큰으로 보호된 API 호출
curl -s http://localhost:8080/api/me \
  -H "Authorization: Bearer eyJhbGci..."
# → {"username":"admin","role":"ROLE_ADMIN"}

# 3) 토큰 없이 호출하면
curl -s http://localhost:8080/api/me
# → 401 (인증 필요)
```

### 실제 검증 결과 (이 문서 작성 시 전부 실행 확인)

| 상황 | 요청 | 결과 |
|---|---|---|
| 토큰 없음 | `GET /api/me` | **401** |
| user 토큰 | `GET /api/me` | 200 `{"role":"ROLE_USER"}` |
| user 토큰 | `GET /api/admin/dashboard` | **403**(권한 부족) |
| admin 토큰 | `GET /api/admin/dashboard` | **200** |
| 위변조 토큰 | `GET /api/me` | **401**(서명 불일치) |
| 잘못된 비밀번호 | `POST /api/auth/login` | **401** |

> ⚠️ **함정 — Windows 기본 curl 로 한글 JSON 을 보내면 400 이 납니다.**
> 회원가입(`displayName` 에 한글)처럼 **한글이 든 JSON 본문**을 Git Bash의 curl 로 보내면 인코딩 문제로 실패합니다.
> **데모 페이지(방법 A)** 를 쓰거나, PowerShell `Invoke-RestMethod`(UTF-8) / Python 으로 보내세요.
> (로그인은 아이디·비밀번호가 영문·숫자라 curl 로도 잘 됩니다.)

---

## 9. 토큰은 어디에 보관하고, 만료되면?

- **보관 위치**: 브라우저라면 보통 `localStorage` 또는 메모리에 둡니다. (데모는 메모리 변수)
  쿠키에 담기도 하는데, 그 경우 다시 CSRF를 고려해야 합니다. → 자세한 트레이드오프는 [04 문서](./04_세션_vs_JWT_비교.md).
- **만료(`exp`)**: 우리 샘플은 30분입니다. 지나면 서버가 검증에서 거부(401)합니다.
- **로그아웃/무효화의 딜레마**: 서버가 기억을 안 하므로, 한 번 발급된 토큰은 **만료 전까지 유효**합니다.
  강제 로그아웃하려면 별도 장치(블랙리스트, 짧은 만료 + 리프레시 토큰)가 필요합니다. → 04 문서에서 정리.

---

## 정리

- JWT는 **서명된 증명서**를 클라이언트가 들고 다니는 **무상태** 방식입니다.
- 로그인 시 토큰을 발급하고, 이후 요청은 `Authorization: Bearer` 헤더의 토큰을 **필터가 검사**합니다.
- Payload는 **암호화가 아니라 인코딩**이라 누구나 읽을 수 있습니다 — 민감정보 금지.
- CSRF 끔 + 세션 안 만듦 + 커스텀 필터 추가가 설정의 3대 특징입니다.

두 방식을 다 만들어 봤으니, 이제 **[04. 세션 vs JWT](./04_세션_vs_JWT_비교.md)** 에서 언제 무엇을 쓸지 정리합니다.
