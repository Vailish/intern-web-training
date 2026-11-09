# 07. 종합 — URL을 입력하면 무슨 일이 일어나나 (면접 대비)

> 🎯 목표: 01~06에서 따로 배운 내용을 **하나의 흐름**으로 꿰고, 면접 단골 질문에 **"직접 확인해 본 사람"** 으로 답할 수 있게 된다.

---

## 1. 전체 흐름 — `https://www.example.com/intro/new` 를 입력하면

```
 [브라우저]
   ⓪ URL 해석        https(→포트 443) / www.example.com(호스트) / /intro/new(경로)
   ① DNS            www.example.com → 203.0.113.10                         [03 문서]
   ② TCP 연결        SYN → SYN+ACK → ACK  (203.0.113.10:443)                 [04 문서]
   ③ TLS handshake  Client Hello(SNI) → Server Hello·인증서 → 키 합의          [05 문서]
   ④ HTTP 요청       GET /intro/new HTTP/1.1  Host: www.example.com (암호화됨) [05 문서]
        │
        ▼  (인터넷: IP로 길을 찾아 라우터들을 거침, NAT 통과)                   [01, 02 문서]
 [서버]
   ⑤ 웹서버/WAS      내장 톰캣이 443(또는 앞단 웹서버가 받아 8080으로 전달)        [웹서버·WAS PDF]
   ⑥ 스프링          DispatcherServlet → @GetMapping("/intro/new") 컨트롤러      [Spring 교육]
                     → 서비스 → 리포지토리 → DB (커넥션 풀에서 연결 빌려 씀)      [04 문서 8절]
   ⑦ HTTP 응답       HTTP/1.1 200 + HTML
        │
        ▼
 [브라우저]
   ⑧ 렌더링          HTML 해석 → CSS·JS·이미지 추가 요청 (같은 연결 재사용: keep-alive)
   ⑨ 연결 정리        한동안 요청이 없으면 FIN으로 종료 (4-way)                    [04 문서]
```

> 💡 각 단계에서 무엇이 실패하면 어떤 에러가 나는지는 [06 문서](./06_에러로_원인찾기.md)의 진단 순서와 정확히 같습니다. **흐름을 알면 진단도 할 수 있습니다.**

---

## 2. 실습 — curl 한 번에서 단계 찾기

```powershell
curl.exe -v -o NUL https://www.naver.com
```

실측 출력 중 `*` 줄에 단계를 표시하면 (curl 버전에 따라 문구는 조금 다름):

```
* Host www.naver.com:443 was resolved.                       ← ① DNS 완료
* IPv6: (none)
* IPv4: 23.49.204.248                                        ← ① 해석된 IP
*   Trying 23.49.204.248:443...                              ← ② TCP 연결 시도 (SYN)
* schannel: disabled automatic use of client certificate     ← ③ TLS 준비 (Windows의 TLS 라이브러리 schannel)
* ALPN: curl offers http/1.1                                 ← ③ TLS 중 "HTTP/1.1로 얘기하자" 협상
* ALPN: server accepted http/1.1
* Established connection to www.naver.com (23.49.204.248 port 443) from 192.168.x.x port 59481   ← ②③ 연결 완료
* using HTTP/1.x
* Request completely sent off                                ← ④ HTTP 요청 전송 완료
* Connection #0 to host www.naver.com:443 left intact        ← ⑨ keep-alive로 연결 유지
```

`>` 로 시작하는 줄(보낸 요청)과 `<` 로 시작하는 줄(받은 응답)도 찾아서 ④⑦에 표시해 보세요.

> 같은 시간에 Wireshark(이더넷 어댑터)로 캡처하면, ② SYN 3개 → ③ Client Hello·Server Hello → `Application Data` 순서를 패킷으로도 확인할 수 있습니다([05 문서](./05_HTTP는_TCP_위의_텍스트다.md) 실습 D).

---

## 3. 면접 답변 구조 — "URL을 입력하면?"

### 30초 버전
> "먼저 **DNS**로 도메인을 IP 주소로 바꿉니다. 그 IP의 443 포트로 **TCP 3-way handshake**를 해서 연결을 맺고, HTTPS라면 **TLS handshake**로 서버 인증서를 확인하고 암호화 키를 합의합니다. 그 위로 **HTTP 요청**을 보내면 서버의 WAS가 받아서 컨트롤러·서비스·DB를 거쳐 응답을 만들고, 브라우저는 받은 HTML을 **렌더링**하면서 CSS·JS를 추가로 요청합니다."

### 2분 버전에 덧붙일 것 (하나씩 깊이 들어가기)
1. **DNS**: 브라우저·OS 캐시 → hosts 파일 → DNS 서버(재귀 조회), 결과는 TTL 동안 캐시
2. **TCP**: SYN, SYN+ACK, ACK로 양쪽의 송수신과 시작 순서 번호를 확인. 연결 비용이 있어서 **keep-alive·커넥션 풀**로 재사용
3. **TLS**: 인증서로 서버가 진짜인지 확인 → 양쪽이 같은 대칭키를 만들어 이후 데이터를 암호화
4. **서버**: 내장 톰캣 → DispatcherServlet → 컨트롤러 → 서비스 → 리포지토리 → **커넥션 풀**에서 DB 연결을 빌려 SQL 실행
5. **응답과 렌더링**: 상태 코드, 추가 리소스 요청

> ⭐ **차별화 포인트**: "Wireshark로 3-way handshake와 RST를 직접 캡처해 봤다", "Docker 환경에서 localhost 문제를 겪고 내장 DNS로 해결했다" 처럼 **직접 확인한 경험**을 한 문장 섞으세요. 외운 답과 구분됩니다.

---

## 4. 면접 단골 질문 12선

먼저 스스로 답해 본 뒤 펼쳐서 비교하세요.

<details>
<summary><b>Q1. TCP와 UDP의 차이는?</b></summary>

TCP는 **연결을 맺고**(3-way handshake) 순서 번호·확인 응답·재전송으로 **순서와 도착을 보장**합니다. UDP는 연결 없이 **데이터그램을 바로 보내고** 도착·순서를 보장하지 않는 대신 가볍고 빠릅니다.
웹·DB 접속처럼 정확성이 중요하면 TCP, DNS·실시간 영상·게임처럼 **지연이 더 치명적이면** UDP를 씁니다. HTTP/3은 UDP 위의 QUIC을 씁니다.
*(경험: 서버가 꺼진 포트로 보냈을 때 TCP는 즉시 Connection refused, UDP는 send()가 에러 없이 끝나는 것을 확인)*
</details>

<details>
<summary><b>Q2. 3-way handshake 과정과, 왜 3번인가?</b></summary>

① 클라이언트 SYN(시작 번호 x) → ② 서버 SYN+ACK(x+1 확인, 자기 시작 번호 y) → ③ 클라이언트 ACK(y+1 확인).
양쪽 모두 **"내 송신이 상대에게 닿고, 상대의 송신이 나에게 온다"** 를 확인하고 **서로의 시작 순서 번호를 교환**하려면 최소 3번이 필요합니다. 2번이면 서버는 자기 SYN+ACK가 도착했는지 알 수 없습니다.
</details>

<details>
<summary><b>Q3. 연결 종료(4-way handshake)와 TIME_WAIT은?</b></summary>

TCP는 양방향을 각각 닫아서 FIN → ACK → FIN → ACK 4단계입니다(한쪽이 다 보냈어도 반대쪽은 아직 보낼 수 있음).
**먼저 끊은 쪽**은 TIME_WAIT 상태로 잠시 기다립니다. ① 마지막 ACK가 유실되면 상대가 FIN을 다시 보낼 때 응답하기 위해, ② 늦게 도착한 옛 연결의 패킷이 새 연결과 섞이지 않게 하기 위해서입니다. 짧은 연결을 대량으로 만들면 TIME_WAIT이 쌓여 문제가 되므로 연결을 재사용합니다.
</details>

<details>
<summary><b>Q4. OSI 7계층과 TCP/IP 4계층은?</b></summary>

TCP/IP 4계층: 응용(HTTP, DNS) / 전송(TCP, UDP, 포트) / 인터넷(IP) / 네트워크 접근(이더넷, MAC).
OSI의 응용·표현·세션 = TCP/IP 응용, 전송 = 전송, 네트워크 = 인터넷, 데이터링크·물리 = 네트워크 접근. 계층을 나누면 각 계층을 독립적으로 바꾸고 이해할 수 있습니다. L4·L7 로드밸런서의 숫자가 OSI 계층 번호입니다.
</details>

<details>
<summary><b>Q5. DNS는 어떻게 동작하나?</b></summary>

브라우저·OS 캐시 → hosts 파일 → 설정된 DNS 서버에 질의. DNS 서버는 모르면 루트 → TLD(.com) → 해당 도메인의 권한 있는 서버 순으로 대신 물어(재귀 조회) 답을 돌려주고, 결과는 **TTL** 동안 캐시됩니다. 주로 **UDP 53번** 포트를 씁니다.
</details>

<details>
<summary><b>Q6. HTTP와 HTTPS의 차이는?</b></summary>

HTTPS = HTTP + **TLS**. TCP 연결 후 TLS handshake에서 **인증서로 서버 신원을 확인**하고, 키 교환으로 양쪽이 같은 **대칭키**를 만들어 이후 데이터를 암호화합니다. 도청·변조·사칭을 막습니다.
단, 접속하는 도메인 이름(SNI)과 IP·포트는 보입니다. 내용이 숨겨지는 것입니다.
*(흔히 "비대칭키로 대칭키를 교환한다"고 요약하는데, 최신 TLS 1.3은 키 '교환'이 아니라 양쪽이 계산으로 같은 키를 '합의'하는 방식입니다. 이 정도까지 말하면 깊이가 드러납니다.)*
</details>

<details>
<summary><b>Q7. 공인 IP와 사설 IP, NAT란?</b></summary>

공인 IP는 인터넷에서 유일한 주소, 사설 IP(10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)는 내부망에서만 쓰는 주소입니다. **NAT**는 공유기·게이트웨이가 여러 사설 IP를 공인 IP로 바꿔 인터넷에 내보내는 기술로, 부족한 IPv4 주소를 아끼고 내부를 숨기는 효과가 있습니다. Docker 컨테이너 IP(172.x)도 사설 IP입니다.
</details>

<details>
<summary><b>Q8. 포트란? 서버는 포트 하나로 어떻게 여러 클라이언트를 받나?</b></summary>

포트는 한 컴퓨터 안에서 **어느 프로그램**에게 갈지 정하는 번호(0~65535)입니다. 연결은 **(출발지 IP, 출발지 포트, 목적지 IP, 목적지 포트)** 4개 값으로 구별됩니다. 클라이언트마다 IP나 임시 포트가 다르므로 서버는 8080 하나로 여러 연결을 동시에 구분할 수 있습니다.
</details>

<details>
<summary><b>Q9. keep-alive와 커넥션 풀은 왜 쓰나?</b></summary>

새 연결마다 TCP handshake(+TLS handshake, +DB 인증)라는 **왕복 비용**이 들고, 짧은 연결이 많으면 TIME_WAIT도 쌓입니다. 그래서 HTTP는 **keep-alive**로 한 연결에 여러 요청을 보내고, 앱 ↔ DB는 **커넥션 풀(HikariCP)** 로 연결을 미리 만들어 빌려 씁니다.
*(경험: curl -v에서 두 번째 요청이 "Reusing existing connection"인 것을 확인)*
</details>

<details>
<summary><b>Q10. Connection refused와 Connection timed out의 차이는?</b></summary>

**refused**는 상대 컴퓨터가 **RST로 응답**한 것, 즉 컴퓨터는 있지만 그 포트에서 기다리는 프로그램이 없는 것입니다(서버 프로세스·포트 확인). **timeout**은 SYN에 **아무 응답이 없는** 것으로, 방화벽·보안그룹 차단이나 잘못된 IP를 의심합니다.
*(경험: Wireshark에서 refused 때 SYN 직후 RST 패킷을 확인)*
</details>

<details>
<summary><b>Q11. GET과 POST의 차이는?</b></summary>

GET은 **조회**용으로 데이터를 URL 쿼리스트링에 담고, 같은 요청을 여러 번 해도 결과가 같은(**멱등**) 것이 원칙이라 캐시될 수 있습니다. POST는 **생성·처리**용으로 데이터를 본문(body)에 담고 멱등하지 않습니다.
주의: POST라고 데이터가 **암호화되는 것은 아닙니다**. 본문도 HTTP면 그대로 보이고, 보호는 HTTPS가 합니다.
</details>

<details>
<summary><b>Q12. 브라우저에서 localhost:8080은 되는데, 같은 Wi-Fi의 휴대폰에서 내 IP:8080으로는 안 된다. 왜?</b></summary>

① 서버가 `127.0.0.1` 에만 바인딩되어 있으면 루프백으로만 접속 가능(`netstat` 으로 `0.0.0.0:8080` 인지 확인) ② Windows 방화벽이 인바운드를 막고 있음 ③ 공용·회사 Wi-Fi의 기기 간 통신 차단. 하나씩 확인합니다.
*(경험: server.address=127.0.0.1로 실행하면 같은 PC에서도 내 IP로는 연결이 거부되는 것을 확인)*
</details>

---

## 5. 모듈 전체 자가 점검

아래 질문에 **막힘없이** 답할 수 있으면 이 모듈을 완료한 것입니다.

| # | 질문 | 관련 문서 |
|---|---|---|
| 1 | 패킷 하나를 열었을 때 보이는 4겹의 봉투와 각 주소는? | 01 |
| 2 | 컨테이너 안에서 `localhost` 로 DB 접속이 실패하는 이유는? | 02 |
| 3 | `0.0.0.0:8080` 과 `127.0.0.1:8080` 의 차이는? | 02 |
| 4 | `DB_HOST=db` 가 동작하는 원리는? | 03 |
| 5 | Wireshark에서 3-way handshake를 찾는 방법은? | 00, 04 |
| 6 | Connection refused는 어떤 패킷인가? | 04, 06 |
| 7 | 서버가 꺼져 있어도 UDP send()가 성공하는 이유는? | 04 |
| 8 | HTTP 요청을 손으로 보낼 때 꼭 필요한 줄 두 개와 빈 줄의 의미는? | 05 |
| 9 | HTTPS인데도 접속한 도메인이 보이는 이유는? | 05 |
| 10 | `Communications link failure` 를 보면 무엇부터 확인하나? | 06 |

---

## 다음 단계

- **배포 실습과 연결**: [06_Deploy](../06_Deploy/README.md)를 다시 보면 모든 명령이 네트워크 개념으로 읽힐 것입니다.
- **클라우드**: [Spring/Deploy](../Spring/Deploy/README.md)의 AWS 보안그룹·RDS 설정을 02(CIDR), 06(timeout) 관점으로 다시 읽어 보세요.
- **더 깊이**: HTTP/2·HTTP/3, 로드밸런서(L4/L7), 리버스 프록시(Nginx), CDN — 실무에서 만나면 이 모듈의 계층 그림 위에 얹어서 이해하면 됩니다.
