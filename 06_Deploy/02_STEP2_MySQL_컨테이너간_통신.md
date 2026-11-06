# STEP 2 — MySQL로 바꾸고, 컨테이너 2개를 통신시키기

> 📁 실습 폴더: [샘플/step2-mysql/app](./샘플/step2-mysql/app/) (앱), [샘플/step2-mysql/db](./샘플/step2-mysql/db/) (DB)
> 🎯 목표: 앱과 DB를 **각각의 이미지**로 빌드하고, 두 컨테이너를 **같은 네트워크**에 연결해 서로 통신하게 한다.

```
 브라우저 ──localhost:8080──▶ ┌──────────── 네트워크: intro-net ────────────┐
                              │                                             │
                              │  ┌───────────────┐  intro-db:3306  ┌──────────────┐
                              │  │ intro-app     │ ──────────────▶ │ intro-db     │
                              │  │ (스프링부트)   │                 │ (MySQL 8.4)  │
                              │  └───────────────┘                 └──────┬───────┘
                              └─────────────────────────────────────────┼─────┘
                                                                 볼륨 intro-mysql-data
```

---

## 1. 왜 DB를 따로 떼어 내나?

STEP 1에서는 H2 DB가 **앱 컨테이너 안**에 있었습니다. 이 구조에는 한계가 있습니다.

| 문제 | 설명 |
|---|---|
| 생명주기가 묶임 | 앱을 새 버전으로 바꾸려고 컨테이너를 교체하면 DB도 같이 영향을 받음 |
| 확장 불가 | 사용자가 늘어 앱 컨테이너를 2개로 늘리면, 각자 **다른 DB 파일**을 쓰게 됨 |
| 실무와 다름 | 실제 서비스는 MySQL·Oracle 같은 **DB 서버**를 별도로 둠 |

그래서 **"컨테이너 하나에는 역할 하나"** 가 원칙입니다. 앱 컨테이너와 DB 컨테이너를 나누고, 네트워크로 연결합니다.

---

## 2. 앱 코드에서 바뀐 것 (3군데)

[샘플/step2-mysql/app](./샘플/step2-mysql/app/) 은 STEP 1 프로젝트에서 **DB 관련 설정만** 바꿨습니다. 자바 코드는 그대로입니다.

### ① build.gradle — 드라이버 교체

```groovy
// 변경 전
runtimeOnly 'com.h2database:h2'

// 변경 후
runtimeOnly 'com.mysql:mysql-connector-j'     // MySQL 드라이버
testRuntimeOnly 'com.h2database:h2'           // 테스트는 MySQL 없이 돌도록 H2 유지
```

### ② src/main/resources/application.properties — 접속 정보를 "환경변수"로

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:introdb}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USERNAME:intro}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
```

`${DB_HOST:localhost}` 는 **"환경변수 `DB_HOST` 가 있으면 그 값, 없으면 `localhost`"** 라는 뜻입니다.

> ⭐ **설정 외부화(Externalized Configuration)** — 이 단계에서 가장 중요한 개념입니다.
> 이미지를 **한 번만** 만들어 두고, 실행할 때 `-e DB_HOST=...` 로 접속 대상을 바꿉니다.
> 개발/테스트/운영 서버에 **같은 이미지**를 쓰고 설정만 다르게 줄 수 있으며, 비밀번호가 이미지에 남지 않습니다.

비밀번호는 일부러 기본값을 비워 두었습니다(`${DB_PASSWORD:}`). **코드에 비밀번호를 적지 않는 것**이 원칙입니다.

### ③ src/test/resources/application.properties — 테스트용 H2 (신규)

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL
...
```

이 파일이 없으면 `.\gradlew.bat test` 가 MySQL에 접속하려다 실패합니다. 테스트는 **외부 DB 없이도** 돌아야 CI(자동 테스트)에서 쓸 수 있습니다.

---

## 3. 이미지 2개의 Dockerfile

### 앱 — [app/Dockerfile](./샘플/step2-mysql/app/Dockerfile)

STEP 1 심화의 **멀티스테이지** 구조와 같습니다. 차이점은 단 하나, **H2 파일이 없으니 `data` 폴더를 만들지 않습니다.**

### DB — [db/Dockerfile](./샘플/step2-mysql/db/Dockerfile)

```dockerfile
FROM mysql:8.4                                         # 공식 MySQL 이미지 (8.4 = 장기지원 LTS)
ENV MYSQL_DATABASE=introdb                             # 처음 시작할 때 introdb 데이터베이스 자동 생성
COPY conf/intro.cnf /etc/mysql/conf.d/intro.cnf        # 우리 서비스용 설정 추가
EXPOSE 3306
```

[conf/intro.cnf](./샘플/step2-mysql/db/conf/intro.cnf) 에는 **문자셋 utf8mb4(한글·이모지)**, **시간대 +09:00(한국)** 이 들어 있습니다.

**공식 이미지를 그대로 쓰면 되는데 왜 DB 이미지를 직접 빌드하나요?**
→ "우리 서비스는 utf8mb4, 한국 시간" 같은 **팀의 약속을 이미지에 박아 두면** 누가 어디서 띄워도 같은 설정이 보장됩니다.

> ⚠️ **비밀번호(`MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`)는 Dockerfile에 절대 쓰지 않습니다.**
> Dockerfile의 `ENV` 는 이미지에 그대로 남아서, 이미지를 받은 누구나 `docker inspect` 로 볼 수 있습니다.

---

## 4. 실습

### 4-1. 이미지 2개 빌드

```powershell
cd 06_Deploy\샘플\step2-mysql

docker build -t intro-app:1.0 .\app      # 앱 이미지 (첫 빌드는 Gradle 다운로드로 몇 분 걸림)
docker build -t intro-db:1.0  .\db       # DB 이미지
docker images intro-*
```

> 경로 인자(`.\app`, `.\db`)가 **빌드 컨텍스트**입니다. 그 폴더 안의 `Dockerfile` 이 사용됩니다.

### 4-2. 네트워크 만들기

```powershell
docker network create intro-net
docker network ls
```

### 4-3. DB 컨테이너 실행 → 준비될 때까지 기다리기

```powershell
docker run -d --name intro-db --network intro-net `
  -e MYSQL_ROOT_PASSWORD=root1234 `
  -e MYSQL_USER=intro `
  -e MYSQL_PASSWORD=intro1234 `
  -v intro-mysql-data:/var/lib/mysql `
  intro-db:1.0

docker logs -f intro-db
```

로그에서 **`ready for connections ... port: 3306`** 이 보이면 `Ctrl+C` 로 빠져나옵니다. (보통 10~20초)

> ⚠️ `ready for connections ... port: 0` 이 **먼저 한 번** 나옵니다. 이건 초기화용 **임시 서버**라 아직 접속할 수 없습니다. 반드시 **`port: 3306`** 줄을 기다리세요.
> 🔐 `root1234`, `intro1234` 는 **실습용 예시**입니다. 실제 서비스에서는 절대 이런 비밀번호를 쓰지 마세요.

| 옵션 | 의미 |
|---|---|
| `--network intro-net` | 이 컨테이너를 `intro-net` 네트워크에 연결 |
| `-e MYSQL_USER / MYSQL_PASSWORD` | 앱이 쓸 계정 생성 (공식 이미지 기능, **처음 시작할 때만** 동작) |
| `-v intro-mysql-data:/var/lib/mysql` | MySQL 데이터 폴더를 볼륨에 보관 |
| (`-p` 없음) | **DB 포트를 PC에 열지 않습니다.** 앱은 네트워크 안에서 접속하므로 필요 없습니다 |

### 4-4. 앱 컨테이너 실행

```powershell
docker run -d --name intro-app --network intro-net -p 8080:8080 `
  -e DB_HOST=intro-db `
  -e DB_USERNAME=intro `
  -e DB_PASSWORD=intro1234 `
  intro-app:1.0

docker logs -f intro-app      # "Started IntroApplication" 확인 후 Ctrl+C
```

⭐ **`DB_HOST=intro-db`** — 앱은 DB를 **컨테이너 이름**으로 찾습니다. 같은 사용자 정의 네트워크에 있는 컨테이너는 Docker가 **이름 → IP** 를 자동으로 연결해 줍니다(내장 DNS).

### 4-5. 확인

1. 브라우저 `http://localhost:8080` 에서 글 등록 (한글·이모지 😀 도 넣어 보세요)
2. DB에 정말 들어갔는지 **DB 컨테이너 안에서** 직접 조회

```powershell
docker exec -it intro-db mysql -uintro -p introdb
```
```sql
-- 비밀번호 intro1234 입력 후
SELECT id, name, title, created_at FROM intro;
exit
```

`created_at` 이 한국 시간이고 한글·이모지가 깨지지 않았다면 성공입니다.

---

## 5. 핵심 개념 — "컨테이너 안의 localhost 는 자기 자신이다"

이 단계에서 가장 많이 틀리는 부분입니다.

| 어디서 | `localhost` 가 가리키는 것 | DB에 접속하는 올바른 주소 |
|---|---|---|
| 내 PC (`bootRun`) | 내 PC | `localhost:3306` (PC에 MySQL이 깔려 있다면) |
| **앱 컨테이너 안** | **앱 컨테이너 자신** (거기엔 MySQL이 없음!) | **`intro-db:3306`** (컨테이너 이름) |
| 브라우저 → 앱 | 내 PC → `-p` 로 연결된 앱 컨테이너 | — |

**포트 매핑(`-p`)은 "내 PC ↔ 컨테이너" 용입니다.** 컨테이너끼리는 같은 네트워크 안에서 **원래 포트(3306)** 로 바로 통신하므로 `-p` 가 필요 없습니다.

---

## 6. 실험 — 일부러 실패시켜 보기

에러 메시지를 미리 보아 두면 실무에서 원인을 빨리 찾습니다.

### 실험 ① `--network` 를 빼고 앱 실행

```powershell
docker run -d --name intro-app-wrong -e DB_HOST=intro-db -e DB_PASSWORD=intro1234 intro-app:1.0
docker logs intro-app-wrong
docker ps -a            # intro-app-wrong 이 Exited (1) 상태
docker rm intro-app-wrong
```

```
Caused by: com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

`--network` 를 빼면 기본(default bridge) 네트워크에 붙습니다. **기본 네트워크는 컨테이너 이름으로 찾아 주지 않습니다.** 그래서 `intro-db` 를 찾지 못해 앱이 종료됩니다. → **반드시 `docker network create` 로 만든 네트워크를 쓰세요.**

### 실험 ② DB가 준비되기 전에 앱부터 실행

DB 컨테이너를 지우고 다시 만든 직후(볼륨도 지우면 초기화가 더 오래 걸림)에 바로 앱을 실행하면 **같은 `Communications link failure`** 로 앱이 죽습니다.
**"컨테이너가 실행됨(Up)" ≠ "DB가 접속을 받을 준비가 됨"** 입니다. 이 순서 문제를 STEP 3에서 `healthcheck` 로 해결합니다.

---

## 7. (선택) Workbench 로 DB 보기

DB 내용을 GUI로 보고 싶다면 DB 컨테이너에 포트를 열어 다시 띄웁니다. 볼륨을 쓰므로 데이터는 유지됩니다.

```powershell
docker rm -f intro-db
docker run -d --name intro-db --network intro-net -p 3307:3306 `
  -e MYSQL_ROOT_PASSWORD=root1234 -e MYSQL_USER=intro -e MYSQL_PASSWORD=intro1234 `
  -v intro-mysql-data:/var/lib/mysql intro-db:1.0
```

Workbench 접속: Host `127.0.0.1`, Port **`3307`**, User `intro`.
> 3306 대신 **3307** 을 쓰는 이유: [03_Database 모듈](../03_Database/README.md)에서 PC에 설치한 MySQL이 이미 3306을 쓰고 있을 수 있습니다.

---

## 8. 이 단계의 함정

| # | 증상 | 원인 | 해결 |
|---|---|---|---|
| ① | `Communications link failure` 로 앱 종료 | 네트워크 미연결 / DB 준비 전 실행 / `DB_HOST` 오타 | `--network intro-net` 확인, DB 로그 `port: 3306` 확인 후 실행 |
| ② | `DB_HOST=localhost` 로 했더니 접속 실패 | 컨테이너 안의 localhost = 앱 자신 | 컨테이너 이름(`intro-db`) 사용 |
| ③ | `Access denied for user 'intro'` | 비밀번호 불일치. 특히 **볼륨에 예전 DB가 남아 있으면 `-e MYSQL_PASSWORD` 가 무시됨** | 같은 비밀번호 사용, 또는 `docker volume rm intro-mysql-data` 후 DB 재생성 |
| ④ | 한글이 `???` 로 저장 | DB 문자셋이 utf8mb4가 아님 | `intro.cnf` 의 `character-set-server=utf8mb4` |
| ⑤ | `.\gradlew.bat test` 실패 | 테스트가 MySQL에 접속 시도 | `src/test/resources/application.properties` (H2) |
| ⑥ | `port is already allocated` (8080) | STEP 1 컨테이너가 아직 실행 중 | `docker rm -f intro-h2` |

> 💡 **③ 은 실무에서도 자주 겪습니다.** MySQL 공식 이미지의 `MYSQL_*` 환경변수는 **데이터 폴더가 비어 있을 때(최초 1회)만** 적용됩니다.

---

## 9. 체크리스트 & 정리

- [ ] `docker images` 에 `intro-app:1.0`, `intro-db:1.0` 두 이미지가 있다
- [ ] 앱 컨테이너에서 DB 컨테이너로 **컨테이너 이름**으로 접속하는 원리를 설명할 수 있다
- [ ] `-p` 가 필요한 경우(PC↔컨테이너)와 필요 없는 경우(컨테이너↔컨테이너)를 구분할 수 있다
- [ ] 설정을 환경변수로 빼는 이유(이미지 재사용, 비밀번호 보호)를 설명할 수 있다

```powershell
docker rm -f intro-app intro-db
docker network rm intro-net
docker volume rm intro-mysql-data      # (선택) DB 데이터까지 삭제
```

이 단계에서 실행한 명령을 세어 보세요. **빌드 2번, 네트워크 1번, run 2번, 그리고 순서와 대기까지.**
매번 이걸 손으로 치는 건 번거롭고 실수하기 쉽습니다. → STEP 3에서 **파일 하나**로 바꿉니다.

➡️ 다음: [STEP 3 — docker compose로 한 번에 올리기](./03_STEP3_docker_compose.md)
