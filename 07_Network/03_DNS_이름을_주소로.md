# 03. DNS — 이름을 주소로

> 🎯 목표: `naver.com`, `intro-db` 같은 **이름이 IP 주소로 바뀌는 과정(DNS)** 을 이해하고, 직접 조회·캡처·조작해 본다.

---

## 도입

- 06_Deploy에서 앱에 `DB_HOST=intro-db` 라고만 적었는데 DB를 찾아갔습니다. **이름은 누가 IP로 바꿔 줬을까?**
- 컴퓨터는 IP로만 통신합니다. 그런데 우리는 `223.130.200.219` 대신 `naver.com` 을 입력합니다.

**DNS(Domain Name System)** = 이름을 IP로 바꿔 주는 **인터넷의 전화번호부**입니다.

---

## 1. DNS 조회는 어떤 순서로 일어나나

```
 브라우저에 naver.com 입력
   │
   ├─① 캐시 확인 (브라우저 → OS)          이미 알면 여기서 끝 (가장 빠름)
   ├─② hosts 파일 확인                   C:\Windows\System32\drivers\etc\hosts
   └─③ DNS 서버에 질문 (UDP 53번 포트)    ipconfig /all 의 "DNS 서버" (통신사·회사·8.8.8.8 등)
          │  DNS 서버도 모르면 대신 물어봐 줌 (재귀 조회)
          ├─ 루트 서버:     ".com 은 저쪽에 물어봐"
          ├─ .com 서버:     "naver.com 은 저쪽에 물어봐"
          └─ naver.com 담당 서버: "223.130.200.219 야"   ← 최종 답
```

- **캐시와 TTL**: 받은 답은 **TTL(유효 시간)** 동안 저장해 두고 재사용합니다. 그래서 두 번째 접속이 더 빠릅니다.
- DNS는 주로 **UDP 53번**을 씁니다. 질문 하나, 답 하나로 끝나는 짧은 통신이라 연결을 맺는 비용(TCP)을 아끼는 것입니다(04 문서).

### 자주 보는 레코드 종류

| 레코드 | 의미 | 예 |
|---|---|---|
| **A** | 이름 → **IPv4** 주소 | `example.com → 172.66.147.243` |
| **AAAA** | 이름 → **IPv6** 주소 | `example.com → 2606:4700:...` |
| **CNAME** | 이름 → **다른 이름** (별명) | `www.example.com → example.com` |
| **PTR** | IP → 이름 (역방향) | `8.8.8.8 → dns.google` |

---

## 2. 실습 — nslookup으로 직접 물어보기

```powershell
nslookup naver.com
```

실제 출력 예 (DNS 서버와 IP는 환경마다 다름):
```
Server:  dns.google
Address:  8.8.8.8

Non-authoritative answer:
Name:    naver.com
Addresses:  223.130.200.219
          223.130.192.247
          223.130.200.236
          223.130.192.248
```

| 줄 | 의미 |
|---|---|
| `Server / Address` | 내가 질문한 DNS 서버 |
| `Non-authoritative answer` | 담당 서버가 아니라 **중간 DNS 서버가 전달해 준(캐시된) 답** 이라는 뜻. 오류가 아님 |
| IP가 여러 개 | 큰 서비스는 **서버 여러 대에 접속을 나누려고** IP를 여러 개 돌려줌 |

```powershell
ipconfig /displaydns      # 내 PC에 캐시된 DNS 답 목록 (방금 조회한 이름을 찾아보세요)
```

---

## 3. 실습 — Wireshark로 DNS 패킷 보기

1. Wireshark에서 **이더넷/Wi-Fi 어댑터**(루프백 아님)를 골라 캡처 시작
2. `nslookup example.com` 실행 → 정지
3. 디스플레이 필터 `dns`

실제 캡처 결과:
```
Standard query 0x0001 PTR 8.8.8.8.in-addr.arpa                           ← DNS 서버 이름을 먼저 물어봄 (nslookup의 습관)
Standard query response 0x0001 PTR ... PTR dns.google
Standard query 0x0002 A example.com                                      ← IPv4 주소 질문
Standard query response 0x0002 A example.com A 172.66.147.243 A 104.20.23.154
Standard query 0x0003 AAAA example.com                                   ← IPv6 주소 질문
Standard query response 0x0003 AAAA example.com AAAA 2606:4700:10::ac42:93f3 ...
```

패킷을 클릭해 ④를 펼치면 **`User Datagram Protocol, Dst Port: 53`** 이 보입니다. DNS가 **UDP** 위에서 동작한다는 증거입니다.

> ⚠️ **캡처하다 보면 내가 실행하지 않은 DNS 질의도 섞여 보입니다.** PC의 다른 프로그램(메신저, 백신, 업데이트 등)도 계속 이름을 조회하기 때문입니다.
> 즉 캡처 파일에는 **내 PC가 어떤 서비스와 통신하는지가 다 드러납니다.** [00 문서](./00_Wireshark_설치와_사용법.md)의 "캡처 파일 공유 금지"가 이런 이유입니다.

---

## 4. 실습 — 내 마음대로 이름 붙이기

스프링 intro 앱(`bootRun`)을 띄운 상태에서 진행합니다.

### 4-1. 관리자 권한 없이: `curl --resolve`

```powershell
curl.exe -s -o NUL -w "%{http_code} %{remote_ip}`n" --resolve myintro.local:8080:127.0.0.1 http://myintro.local:8080/
# 200 127.0.0.1

curl.exe -s -o NUL -w "exit=%{exitcode}`n" http://myintro.local:8080/
# exit=6   ← curl 오류 6 = "Couldn't resolve host" (DNS 실패)
```

`--resolve` 는 **"이 이름은 이 IP로 쳐 줘"** 를 curl 한 번에만 적용합니다. DNS를 거치지 않으니 없는 이름도 접속됩니다.

### 4-2. (선택, 관리자 권한 필요) hosts 파일

hosts 파일은 **DNS 서버보다 먼저** 확인하는 "내 PC 전용 전화번호부"입니다.

1. 메모장을 **관리자 권한으로 실행** → `C:\Windows\System32\drivers\etc\hosts` 열기
2. 맨 아래에 한 줄 추가 후 저장
   ```
   127.0.0.1   myintro.local
   ```
3. 브라우저에서 `http://myintro.local:8080` → intro 앱이 뜸
4. ⚠️ **실습이 끝나면 추가한 줄을 반드시 지우고 저장하세요.** 잊으면 나중에 원인 모를 접속 문제를 겪습니다.

> 💡 **`nslookup` 은 hosts 파일을 보지 않습니다.** DNS 서버에 직접 묻는 도구이기 때문입니다.
> 그래서 hosts에 넣은 이름은 `nslookup` 으로는 "없다"고 나옵니다. OS 방식대로 확인하려면 `ping -n 1 myintro.local` 을 쓰세요(첫 줄에 해석된 IP가 나옴).

---

## 5. 실습 — Docker의 내장 DNS (선택)

[06_Deploy STEP 3](../06_Deploy/03_STEP3_docker_compose.md) 을 `docker compose up -d` 로 띄운 상태에서:

```powershell
docker compose exec app getent hosts db         # 172.22.0.2   db
docker compose exec app cat /etc/resolv.conf    # nameserver 127.0.0.11
```

- 컨테이너의 DNS 서버는 **`127.0.0.11`** 입니다. Docker가 컨테이너마다 넣어 주는 **내장 DNS** 입니다.
- 같은 네트워크의 **서비스·컨테이너 이름 → 그 컨테이너의 IP** 로 답해 줍니다. 그래서 `DB_HOST=db` 가 동작했습니다.
- 사용자가 만든 네트워크(compose 기본 네트워크, `docker network create`)에서만 이름 해석이 됩니다. 06_Deploy STEP 2에서 `--network` 를 빼면 실패했던 이유입니다.

---

## 6. 실무 연결

| 상황 | 포인트 |
|---|---|
| 설정 파일에 IP 대신 **도메인·서비스 이름** 쓰기 | 서버가 바뀌어도 DNS만 바꾸면 됨. AWS RDS도 접속 주소가 IP가 아닌 이름 |
| 서버 이전 후 "일부 사용자만 옛 서버로 가요" | 각자 캐시의 **TTL** 이 아직 남아서. 이전 전에 TTL을 짧게 줄여 두는 것이 정석 |
| "갑자기 외부 API 호출이 전부 실패" | `UnknownHostException` 이면 **DNS부터** 의심 (06 문서) |
| 개발 서버를 이름으로 접속 | hosts 파일 (팀원마다 설정해야 해서 임시용으로만) |

---

## 7. 함정

| 증상 | 원인 | 해결 |
|---|---|---|
| hosts를 고쳤는데 반영 안 됨 | 저장 실패(관리자 권한 아님) 또는 캐시 | 관리자 권한 메모장, `ipconfig /flushdns` (관리자 권한이 필요할 수 있음), 브라우저 재시작 |
| hosts에 넣었는데 nslookup은 "없다"고 함 | nslookup은 hosts를 안 봄 | `ping -n 1 이름` 으로 확인 |
| 컨테이너 이름으로 접속 실패 | 기본 bridge 네트워크에 있음 | 사용자 정의 네트워크 / compose 사용 |
| DNS 서버를 바꿨더니 사내 시스템 접속 안 됨 | 사내 도메인은 **사내 DNS 서버만** 알고 있음 | 회사 PC의 DNS 설정은 임의로 바꾸지 않기 |

---

## 체크리스트

- [ ] 캐시 → hosts → DNS 서버 순서를 설명할 수 있다
- [ ] `nslookup` 결과의 Non-authoritative answer, 여러 개의 IP를 해석할 수 있다
- [ ] Wireshark에서 DNS가 UDP 53을 쓰는 것을 확인했다
- [ ] `curl --resolve` 또는 hosts 파일로 이름을 직접 붙여 봤다
- [ ] Docker의 서비스 이름이 IP로 바뀌는 원리(내장 DNS 127.0.0.11)를 안다

➡️ 다음: [04. TCP·UDP와 3-way handshake](./04_TCP_UDP_3way_handshake.md)
