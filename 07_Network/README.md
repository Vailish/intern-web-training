# 07. Network — 백엔드 개발자를 위한 네트워크 기초

TCP/IP, UDP, 3-way handshake, IP 주소, 포트, DNS를 **"직접 보고, 일부러 실패시켜 보며"** 배웁니다.
면접용 암기가 아니라, **에러 메시지를 보고 어디가 고장 났는지 짚어내는 힘**을 기르는 것이 목표입니다.

> 🎯 **이 모듈을 마치면 할 수 있는 것 한 가지**
> `UnknownHostException`, `Connection refused`, `Connect timed out`, `404` 를 보고
> **"DNS 문제다 / 서버가 꺼져 있다 / 방화벽에 막혔다 / 앱 문제다"** 를 바로 구분할 수 있습니다.

---

## 왜 지금 네트워크인가?

[06_Deploy](../06_Deploy/README.md)에서 이미 이런 일을 겪었습니다.

| 겪은 일 | 이 모듈에서 풀리는 개념 | 문서 |
|---|---|---|
| 컨테이너 안에서 `localhost` 로 DB에 접속했더니 실패 | IP 주소, 루프백 | 02 |
| `port is already allocated` | 포트, LISTEN | 02 |
| `-p 8080:8080` 의 `0.0.0.0` 은 뭐지? | 바인딩 주소 | 02 |
| 컨테이너 이름 `intro-db` 만 써도 접속됨 | DNS | 03 |
| `Communications link failure` | TCP 연결 실패 | 04, 06 |
| healthcheck 는 왜 `-h 127.0.0.1` 이어야 했나 | 소켓 vs TCP | 04 |
| 스프링 로그의 `HikariPool` | 연결 비용과 커넥션 풀 | 04 |

---

## 학습 순서

| 순서 | 문서 | 핵심 질문 | 필수/선택 | 예상 시간 |
|---|---|---|---|---|
| 0 | [Wireshark 설치와 사용법](./00_Wireshark_설치와_사용법.md) | 네트워크를 어떻게 "눈으로" 보지? | **필수(준비)** | 30분 |
| 1 | [큰 그림 — 계층과 주소](./01_큰그림_계층과_주소.md) | 데이터는 어떤 봉투에 담겨 가나? | **필수** | 30분 |
| 2 | [IP 주소와 포트](./02_IP주소와_포트.md) | 어느 컴퓨터의, 어느 프로그램에게? | 권장 | 1시간 |
| 3 | [DNS — 이름을 주소로](./03_DNS_이름을_주소로.md) | `naver.com` 은 어떻게 IP가 되나? | 권장 | 40분 |
| 4 | [TCP·UDP와 3-way handshake](./04_TCP_UDP_3way_handshake.md) | "연결"이란 정확히 무엇인가? | **필수 (핵심)** | 1.5시간 |
| 5 | [HTTP는 TCP 위의 텍스트다](./05_HTTP는_TCP_위의_텍스트다.md) | 브라우저와 서버는 실제로 무슨 글자를 주고받나? | 권장 | 50분 |
| 6 | [에러로 원인 찾기](./06_에러로_원인찾기.md) | 이 에러는 어느 단계에서 났나? | **필수 (실무)** | 1시간 |
| 7 | [종합 — URL을 입력하면 (면접 대비)](./07_종합_URL을_입력하면_면접대비.md) | 처음부터 끝까지 설명할 수 있나? | 권장 | 40분 |

- **전체 약 6시간(하루).** 시간이 부족하면 **00 → 01 → 04 → 06** 만 먼저 하고, 나머지는 나중에 채우세요.
- 문서마다 **도입 질문 → 개념 → 실습 → 실무 연결 → 함정 → 체크리스트** 순서로 되어 있습니다.

---

## 준비물

| 준비물 | 확인 방법 | 비고 |
|---|---|---|
| **Wireshark** | 시작 메뉴에서 Wireshark 실행 | 설치는 [00 문서](./00_Wireshark_설치와_사용법.md). 설치에 관리자 권한 필요 |
| **JDK 17** | `java -version` | socket-lab 실행용 |
| **curl** | `curl.exe --version` | Windows 10 이상 기본 포함. ⚠️ PowerShell에서는 반드시 **`curl.exe`** (아래 참고) |
| **스프링 intro 앱** | [Spring/SpringBoot/샘플/intro](../Spring/SpringBoot/샘플/intro/) 를 `bootRun` | 02, 05, 06 실습에서 사용 |
| **Docker (선택)** | `docker version` | 02, 03의 컨테이너 실습. [06_Deploy STEP 3](../06_Deploy/03_STEP3_docker_compose.md) 환경을 그대로 씀 |

> ⚠️ **PowerShell의 `curl` 은 진짜 curl이 아닙니다.** Windows PowerShell 5.1 에서 `curl` 은 `Invoke-WebRequest` 의 별명입니다.
> 이 모듈의 모든 명령은 **`curl.exe`** 로 적었습니다. 그대로 따라 치세요.

---

## 실습 샘플 — socket-lab

[샘플/socket-lab](./샘플/socket-lab/) 에는 30~90줄짜리 **순수 자바 파일 4개**가 있습니다. Gradle 없이 바로 실행합니다.

| 파일 | 역할 | 주로 쓰는 문서 |
|---|---|---|
| `TcpEchoServer.java` | 받은 줄을 그대로 돌려주는 TCP 서버 (포트 9000) | 01, 04 |
| `TcpClient.java` | 입력한 줄을 보내는 범용 TCP 클라이언트. **연결 실패 시 실패 단계를 알려줌** | 04, 05, 06 |
| `UdpServer.java` / `UdpClient.java` | UDP 버전 (포트 9001) | 04 |

```powershell
cd 07_Network\샘플\socket-lab
java "-Dfile.encoding=UTF-8" TcpEchoServer.java
```

> ⚠️ **`"-Dfile.encoding=UTF-8"` 을 꼭 따옴표째 붙이세요.**
> - 빼면: 한글 Windows의 Java 17은 소스 파일을 MS949로 읽어서 **한글 메시지가 깨집니다.**
> - 따옴표 없이 쓰면: PowerShell 5.1이 `.` 에서 인자를 잘라 `Could not find or load main class .encoding=UTF-8` 에러가 납니다.

스프링은 네트워크를 전부 감춰 줍니다. 그래서 일부러 **스프링 없이** 소켓을 직접 다뤄 봅니다. `accept()`, `connect()`, `send()` 한 줄 한 줄이 Wireshark 화면의 어떤 패킷이 되는지 직접 연결해 보는 것이 이 모듈의 핵심 경험입니다.

---

## 🏢 실무 연결 포인트

| 실무 상황 | 필요한 네트워크 지식 |
|---|---|
| "운영 서버에서 DB 접속이 안 돼요" | refused인지 timeout인지 → 서버 문제 vs 방화벽 문제 구분 (06) |
| 공공·SI 현장의 망 분리, 방화벽 신청서 | 출발지 IP, 목적지 IP, **포트, 프로토콜(TCP/UDP)** 을 정확히 적어야 함 (02, 04) |
| AWS 보안그룹 설정 | CIDR(`10.0.0.0/16`), 포트 범위 (02) |
| 외부 API 호출이 느림 | DNS 조회, 연결 재사용(keep-alive), 커넥션 풀 (03, 04, 05) |

> 공공기관·SI 프로젝트에서는 서버 간 통신마다 **방화벽 오픈 신청**을 하는 경우가 많습니다. 이때 "어디서 어디로, 몇 번 포트, TCP"를 정확히 쓰지 못하면 일이 진행되지 않습니다. 이 모듈의 02·04가 바로 그 내용입니다.

---

## 더 공부하고 싶다면

- [웹서버·WAS 동작원리 PDF](../99_참고자료/02_웹_백엔드/웹서버_WAS_동작원리_인턴초급개발자.pdf) — 이 모듈 위층(웹서버·WAS 구조)
- [MDN — HTTP 개요](https://developer.mozilla.org/ko/docs/Web/HTTP/Overview) — HTTP 공식 수준 레퍼런스 (한국어)
- [Wireshark 사용자 가이드](https://www.wireshark.org/docs/wsug_html_chunked/) — 공식 문서 (영어)
- [GSD_CS_STUDY](https://github.com/GangSeongDokk/GSD_CS_STUDY) — CS 면접 스터디 자료 (개인 저장소, 참고용)

---

> ✅ **검증 기록 (2026-11-06)**: Windows 11, Java 17.0.12, Wireshark 4.6.7(tshark), Docker 29.5.2 환경에서
> 문서의 모든 실습을 실제로 실행했습니다. 문서에 실린 캡처 결과와 에러 메시지는 그 실행 결과입니다.
> 포트 번호, IP 주소, 시간 값은 실행할 때마다 다르게 나옵니다. 사내 네트워크나 OS 버전에 따라 일부 결과가 다를 수 있습니다.
