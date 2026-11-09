# 05. HTTP는 TCP 위의 텍스트다

> 🎯 목표: HTTP 요청·응답이 **TCP 연결 위로 오가는 "정해진 형식의 글자"** 일 뿐이라는 것을, 브라우저 없이 **직접 손으로 타이핑해서** 확인한다.
> 그리고 keep-alive(연결 재사용)와 HTTPS(TLS)가 그 위에 어떻게 얹히는지 본다.

---

## 도입

- 컨트롤러에 `@GetMapping("/intro/new")` 라고 쓰면, 브라우저는 **정확히 무엇을** 보내길래 그 메서드가 호출될까?
- `curl`, Postman, 브라우저는 서로 다른 프로그램인데 왜 같은 서버와 대화할 수 있을까?

답: 모두 **같은 형식의 텍스트(HTTP)** 를 **TCP 연결** 위로 보내기 때문입니다.

---

## 1. HTTP 메시지의 생김새

### 요청 (클라이언트 → 서버)
```
GET /intro/new HTTP/1.1          ← 시작 줄: 메서드  경로  버전
Host: localhost                  ← 헤더 (이름: 값), 여러 줄 가능
                                 ← 빈 줄 = "헤더 끝"
(본문: GET은 보통 없음, POST는 폼 데이터 등)
```

### 응답 (서버 → 클라이언트)
```
HTTP/1.1 200                     ← 상태 줄: 버전  상태코드
Content-Type: text/html;charset=UTF-8
Transfer-Encoding: chunked
                                 ← 빈 줄
<!DOCTYPE html> ...              ← 본문 (HTML)
```

- 줄 끝은 **`\r\n`(CRLF)** 입니다. socket-lab의 `TcpClient` 가 Enter마다 `\r\n` 을 붙이는 이유입니다.
- HTTP/1.1에서는 **`Host` 헤더가 필수**입니다(한 서버가 여러 도메인을 서비스할 수 있어서).

| 상태 코드 | 의미 | 예 |
|---|---|---|
| 2xx | 성공 | 200 OK |
| 3xx | 다른 곳으로 가라 | 302 (intro 앱의 `redirect:/`) |
| 4xx | **클라이언트(요청)** 잘못 | 400 형식 오류, 404 없는 경로, 405 지원하지 않는 메서드 |
| 5xx | **서버(앱)** 잘못 | 500 처리 중 예외 |

---

## 2. 실습 A — HTTP를 손으로 보내기 ⭐

### 2-1. 준비
1. 스프링 intro 앱 실행 (`Spring/SpringBoot/샘플/intro` 에서 `.\gradlew.bat bootRun`)
2. (선택) Wireshark 루프백 캡처 시작

### 2-2. 접속해서 타이핑하기

```powershell
cd 07_Network\샘플\socket-lab
java "-Dfile.encoding=UTF-8" TcpClient.java localhost 8080
```
```
[연결 시도] localhost:8080 ...
[연결 성립] 내 주소 127.0.0.1:55608  →  서버 127.0.0.1:8080
```

이제 아래 두 줄을 **직접 타이핑**하고, 마지막에 **Enter를 한 번 더**(빈 줄) 누릅니다.

```
GET /intro/new HTTP/1.1
Host: localhost

```

실제 응답:
```
HTTP/1.1 200
Content-Type: text/html;charset=UTF-8
Content-Language: ko-KR
Transfer-Encoding: chunked
Date: Fri, 06 Nov 2026 08:06:53 GMT

6df
<!DOCTYPE html>
<html lang="ko">
...
```

**방금 여러분이 브라우저가 하는 일을 손으로 했습니다.** 브라우저도 내부적으로 이 텍스트를 보내고, 응답 본문(HTML)을 화면에 그릴 뿐입니다.

| 응답에서 볼 것 | 의미 |
|---|---|
| `HTTP/1.1 200` 뒤에 `OK` 가 없음 | 정상입니다. 톰캣은 상태 문구(OK 등)를 생략합니다. 숫자가 본체 |
| `Content-Language: ko-KR` | 앱이 한국어 응답이라고 알려 줌 |
| `Transfer-Encoding: chunked` 와 `6df` | 본문 전체 길이를 미리 알리지 않고 **조각(chunk)으로** 보내는 방식. `6df` 는 다음 조각의 크기(16진수 = 1759바이트) |

### 2-3. 연결이 바로 끊기지 않는다 — keep-alive
응답을 받은 뒤에도 `[서버가 연결을 닫았습니다]` 가 바로 나오지 않습니다. **같은 연결로 다음 요청을 또 보낼 수 있습니다.** 바로 이어서 다시 입력해 보세요.

```
GET / HTTP/1.1
Host: localhost

```

아무것도 보내지 않고 기다리면, 서버가 일정 시간 뒤 연결을 닫습니다.
> 실측: 스프링부트 3.5 기본 설정(내장 톰캣)에서 **약 60초** 동안 요청이 없자 서버가 연결을 닫았습니다. 이 시간은 설정으로 바꿀 수 있습니다.

종료는 Ctrl+Z 후 Enter.

### 2-4. 일부러 틀려 보기

| 보낸 요청 | 실측 응답 | 왜? |
|---|---|---|
| `GET / HTTP/1.1` 만 쓰고 `Host` 줄 없이 빈 줄 | **400** | HTTP/1.1은 Host 헤더 필수 |
| `GET /nope HTTP/1.1` + Host | **404** | 그런 경로의 컨트롤러가 없음 |
| `get / HTTP/1.1` + Host (소문자) | **405** | 메서드 이름은 **대소문자를 구분**. `get` 은 모르는 메서드 |
| `GET /intro/999 HTTP/1.1` + Host | **500** | 없는 글 번호 → 서비스가 예외를 던졌는데 처리하지 않음 (앱 문제) |

> 💡 **4개 모두 TCP 연결은 정상**이었습니다. 연결 위에서 주고받은 **HTTP 내용**이 문제였을 뿐입니다. 이 구분이 [06 문서](./06_에러로_원인찾기.md)의 핵심입니다.

---

## 3. 실습 B — Wireshark로 HTTP 보기

1. 루프백 캡처 중에 브라우저로 `http://localhost:8080/intro/new` 접속 → 정지
2. 디스플레이 필터 `tcp.port == 8080`
3. `GET /intro/new` 패킷에 마우스 오른쪽 → **Follow → TCP Stream**

빨간 글씨(요청)와 파란 글씨(응답)로 **위 실습 A와 똑같은 텍스트**가 보입니다. 브라우저가 보낸 헤더가 훨씬 많다는 것(User-Agent, Accept, Cookie 등)도 확인해 보세요.
같은 TCP 연결 안에서 CSS 파일 요청 등이 이어지는 것도 볼 수 있습니다(keep-alive).

---

## 4. 실습 C — curl로 연결 재사용 확인

```powershell
curl.exe -v -o NUL -o NUL http://localhost:8080/ http://localhost:8080/intro/new
```

실측 출력 중 `*` 로 시작하는 줄 (curl 버전에 따라 문구는 조금 다름):
```
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Established connection to localhost (::1 port 8080) from ::1 port 55609      ← TCP 연결 (3-way handshake)
* Request completely sent off
* Connection #0 to host localhost:8080 left intact                              ← 연결을 끊지 않고 둠
* Reusing existing http: connection with host localhost                         ← 두 번째 요청은 같은 연결 재사용!
* Request completely sent off
* Connection #0 to host localhost:8080 left intact
```

- 요청 2개에 **연결은 1번**(`Connection #0` 하나)만 맺었습니다.
- `localhost` 가 **IPv6 `::1`** 로 먼저 해석되어 접속한 것도 보입니다([02 문서](./02_IP주소와_포트.md)).
- `>` 로 시작하는 줄은 보낸 HTTP 요청, `<` 는 받은 응답입니다(위 출력에서는 생략).

---

## 5. HTTPS = TCP + TLS + HTTP

HTTP 텍스트는 **누구나 읽을 수 있습니다**(실습 B에서 직접 읽었듯이). 그래서 비밀번호·쿠키를 보내는 서비스는 **HTTPS** 를 씁니다.

```
HTTP  :  [ TCP 연결 ] → HTTP 텍스트
HTTPS :  [ TCP 연결 ] → [ TLS handshake: 암호 열쇠 합의, 인증서 확인 ] → 암호화된 HTTP
```

### 실습 D — HTTPS handshake 보기
1. Wireshark에서 **이더넷/Wi-Fi 어댑터** 캡처 시작
2. `curl.exe -s -o NUL https://www.naver.com` → 정지
3. 디스플레이 필터 `tls` (Client Hello만 보려면 `tls.handshake.type == 1`)

실측 캡처 결과 (TLS 버전과 패킷 수는 서버·환경에 따라 다름):
```
TCP      59508 → 443 [SYN]                                      ┐
TCP      443 → 59508 [SYN, ACK]                                 ├ TCP 3-way handshake 먼저
TCP      59508 → 443 [ACK]                                      ┘
TLSv1.2  Client Hello (SNI=www.naver.com)                       ┐ "이런 암호 방식을 쓸 수 있어. 접속할 곳은 www.naver.com"
TLSv1.2  Server Hello                                           │ "이 방식으로 하자"
TLSv1.2  Certificate, ..., Server Hello Done                    ├ TLS handshake: 인증서 전달, 열쇠 합의
TLSv1.2  Client Key Exchange, Change Cipher Spec, ...           │
TLSv1.2  New Session Ticket, Change Cipher Spec, ...            ┘
TLSv1.2  Application Data                                       ← 여기부터 HTTP가 암호화되어 오감 (내용 안 보임)
```

**관찰 포인트:**
- HTTPS도 **TCP 3-way handshake가 먼저**입니다. TLS는 그 위에서 한 번 더 handshake 합니다(연결 비용이 더 큼 → 04 문서 8절).
- `Application Data` 는 Follow TCP Stream으로 봐도 **알아볼 수 없는 바이트**입니다. 암호화가 동작한다는 증거입니다.
- ⚠️ **`SNI=www.naver.com` 은 암호화 전에 보입니다.** HTTPS여도 **"어느 사이트에 접속했는지"** 는 네트워크 관리자가 볼 수 있습니다. **내용**만 숨겨집니다.

> Windows에 기본 설치된 curl은 `-v` 로도 TLS 세부 단계를 거의 보여 주지 않습니다(schannel 사용). TLS 과정은 Wireshark로 보는 것이 확실합니다.

---

## 6. 실무 연결

| 상황 | 포인트 |
|---|---|
| Postman·curl로 API 테스트 | 결국 위와 같은 텍스트를 보내는 도구. **헤더(Content-Type 등)를 정확히** 맞추는 것이 핵심 |
| 외부 API 호출 클라이언트(RestClient 등) | 요청마다 새 연결을 만들지 않도록 **연결 재사용(keep-alive, 커넥션 풀)** 설정을 확인 |
| 개인정보를 다루는 서비스 | 비밀번호·고유식별정보 같은 개인정보를 네트워크로 보낼 때는 암호화(HTTPS 등)가 요구됨. 세부 기준은 [법규 유의사항 PDF](../99_참고자료/05_법규_컴플라이언스/서비스_개발운영_법규_유의사항_인턴초급개발자.pdf)와 공식 고시로 확인 |
| WebSocket (실시간 채팅) | HTTP로 시작해 `Upgrade` 요청 후 **같은 TCP 연결을 계속 열어 두고** 양방향 통신 ([ETC/실시간채팅](../ETC/)) |

---

## 7. 함정

| 증상 | 원인 | 해결 |
|---|---|---|
| 손으로 보낸 요청에 응답이 안 옴 | 마지막 **빈 줄**(Enter 한 번 더)을 안 보냄 → 서버는 헤더가 더 올 줄 알고 기다림 | Enter 한 번 더 |
| 400 Bad Request | `Host` 헤더 누락, 오타, 줄 앞 공백 | 형식 확인 |
| PowerShell에서 파이프로 요청을 넣으면 400 | PowerShell이 입력 앞에 보이지 않는 문자(BOM)를 붙일 수 있음 | **직접 타이핑**하기 |
| HTTPS가 Wireshark에서 안 보임 | 루프백 어댑터를 골랐음 | 외부 사이트는 **이더넷/Wi-Fi 어댑터** |

---

## 체크리스트

- [ ] HTTP 요청의 시작 줄, 헤더, 빈 줄, 본문 구조를 안다
- [ ] TcpClient로 스프링 앱에 GET 요청을 직접 보내 200 응답을 받았다
- [ ] 400 / 404 / 405 / 500 을 일부러 만들어 보고, 모두 TCP 연결은 정상이었음을 이해했다
- [ ] curl `-v` 에서 연결 재사용(keep-alive)을 확인했다
- [ ] HTTPS = TCP + TLS + HTTP 이고, SNI(접속 도메인)는 보인다는 것을 안다

➡️ 다음: [06. 에러로 원인 찾기](./06_에러로_원인찾기.md)
