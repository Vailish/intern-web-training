# STEP 3 — docker compose로 한 번에 올리기

> 📁 실습 폴더: [샘플/step3-compose](./샘플/step3-compose/)
> 🎯 목표: STEP 2에서 손으로 친 명령들을 **`docker-compose.yml` 파일 하나**로 옮기고, `docker compose up` 한 줄로 앱 + DB를 실행한다.

```
 docker compose up -d --build
        │
        ├─ 네트워크 intro-stack_default 생성 (자동)
        ├─ 볼륨     intro-stack_mysql-data 생성 (자동)
        ├─ [db]  빌드 → 실행 → healthcheck 통과(healthy) 대기
        └─ [app] 빌드 → db 가 healthy 가 된 뒤 실행 → localhost:8080
```

**앱 코드와 Dockerfile은 STEP 2 그대로**입니다. compose 파일이 `../step2-mysql/app`, `../step2-mysql/db` 를 직접 가리킵니다.
달라지는 것은 **"실행하는 방법"** 뿐입니다.

---

## 1. STEP 2 명령 → compose 파일 대응표

| STEP 2에서 손으로 한 일 | `docker-compose.yml` 에서는 |
|---|---|
| `docker network create intro-net` | **자동** (프로젝트마다 기본 네트워크 생성) |
| `docker build -t intro-db:1.0 .\db` | `db.build: ../step2-mysql/db` + `db.image: intro-db:1.0` |
| `docker build -t intro-app:1.0 .\app` | `app.build: ../step2-mysql/app` + `app.image: intro-app:1.0` |
| `-e MYSQL_ROOT_PASSWORD=...` | `db.environment` (값은 `.env` 파일에서) |
| `-v intro-mysql-data:/var/lib/mysql` | `db.volumes` + 맨 아래 `volumes:` |
| `docker logs` 보며 `port: 3306` 기다리기 | `db.healthcheck` + `app.depends_on.condition: service_healthy` |
| `-e DB_HOST=intro-db` | `app.environment.DB_HOST: db` (**서비스 이름**) |
| `-p 8080:8080` | `app.ports` |
| 실행 순서 기억하기 (DB → 앱) | `depends_on` 이 알아서 |

> 💡 이런 방식을 **선언형(declarative)** 이라고 합니다. "어떻게 실행할지(명령 순서)"가 아니라 **"최종적으로 무엇이 떠 있어야 하는지"** 를 적어 두면 도구가 맞춰 줍니다. 쿠버네티스도 같은 사고방식입니다.

---

## 2. 준비 — `.env` 파일 만들기

비밀번호는 compose 파일에 직접 쓰지 않고 **`.env` 파일**에 둡니다. compose는 같은 폴더의 `.env` 를 자동으로 읽습니다.

```powershell
cd 06_Deploy\샘플\step3-compose
Copy-Item .env.example .env
notepad .env          # 비밀번호를 원하는 값으로 바꾸기 (실습은 그대로 둬도 동작)
```

```properties
# .env
MYSQL_ROOT_PASSWORD=change-me-root
MYSQL_USER=intro
MYSQL_PASSWORD=change-me
```

| 파일 | Git에 올리나? | 역할 |
|---|---|---|
| `.env.example` | ✅ 올림 | "어떤 변수가 필요한지" 알려주는 **견본** (가짜 값) |
| `.env` | ❌ **절대 안 올림** ([.gitignore](./샘플/step3-compose/.gitignore)에 등록) | 진짜 비밀번호 |

`.env` 없이 실행하면 compose가 **실행 전에 멈추고** 알려 줍니다. (`${변수:?메시지}` 문법 덕분)

```
error while interpolating services.db.environment.MYSQL_ROOT_PASSWORD:
required variable MYSQL_ROOT_PASSWORD is missing a value: .env 파일에 MYSQL_ROOT_PASSWORD 를 설정하세요
```

> 🔐 실수로 `.env` 를 커밋했다면 파일만 지워서는 소용없습니다(Git 기록에 남음). **비밀번호를 즉시 바꾸세요.**

---

## 3. docker-compose.yml 읽기

[샘플/step3-compose/docker-compose.yml](./샘플/step3-compose/docker-compose.yml) (주석 생략)

```yaml
name: intro-stack

services:
  db:
    build: ../step2-mysql/db
    image: intro-db:1.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:?.env 파일에 MYSQL_ROOT_PASSWORD 를 설정하세요}
      MYSQL_USER: ${MYSQL_USER:-intro}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:?.env 파일에 MYSQL_PASSWORD 를 설정하세요}
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "--silent"]
      interval: 5s
      timeout: 3s
      retries: 20
      start_period: 20s

  app:
    build: ../step2-mysql/app
    image: intro-app:1.0
    depends_on:
      db:
        condition: service_healthy
    environment:
      DB_HOST: db
      DB_USERNAME: ${MYSQL_USER:-intro}
      DB_PASSWORD: ${MYSQL_PASSWORD:?.env 파일에 MYSQL_PASSWORD 를 설정하세요}
    ports:
      - "8080:8080"
    restart: on-failure

volumes:
  mysql-data:
```

| 키 | 의미 |
|---|---|
| `name` | 프로젝트 이름. 컨테이너·네트워크·볼륨 이름 앞에 붙음 (`intro-stack-db-1`, `intro-stack_mysql-data`) |
| `services` | 띄울 컨테이너 목록. **서비스 이름(`db`, `app`)이 곧 네트워크 안의 주소**가 됨 |
| `build` | 이 폴더의 Dockerfile로 이미지 빌드 |
| `image` | 빌드한 이미지에 붙일 이름 (빌드 없이 쓰면 그 이미지를 내려받아 실행) |
| `environment` | 환경변수 (`-e`). `${A:-기본값}` = 없으면 기본값, `${A:?메시지}` = 없으면 에러 |
| `volumes` | 볼륨 연결 (`-v`). 맨 아래 `volumes:` 에 선언해야 compose가 만들어 줌 |
| `healthcheck` | 컨테이너가 **정말 일할 준비가 됐는지** 주기적으로 검사하는 명령 |
| `depends_on` + `condition: service_healthy` | db 가 **healthy** 가 될 때까지 app 시작을 미룸 |
| `ports` | `-p` 와 같음 |
| `restart: on-failure` | 에러로 죽으면 자동 재시작 (일시적인 문제 대비 보험) |

**DB에는 `ports` 가 없습니다.** 앱은 내부 네트워크로 `db:3306` 에 접속하므로 DB를 PC 밖에 노출할 이유가 없습니다. Workbench로 보고 싶을 때만 파일 안의 주석(`3307:3306`)을 풀어 쓰세요.

---

## 4. 실습

### 4-1. 실행

```powershell
docker compose up -d --build
```

```
 Container intro-stack-db-1 Started
 Container intro-stack-db-1 Waiting
 Container intro-stack-db-1 Healthy      ← DB 준비 완료를 기다림
 Container intro-stack-app-1 Starting    ← 그 다음에 앱 시작
 Container intro-stack-app-1 Started
```

`Waiting → Healthy → app Starting` 순서가 보이면 **healthcheck + depends_on 이 순서를 지켜 준 것**입니다. STEP 2에서 로그를 보며 기다리던 일을 compose가 대신했습니다.

| 옵션 | 의미 |
|---|---|
| `-d` | 백그라운드 실행 |
| `--build` | 실행 전에 이미지를 (다시) 빌드. **코드를 고쳤다면 꼭 붙이기** |

### 4-2. 상태와 로그 확인

```powershell
docker compose ps                 # db 가 (healthy), app 이 Up 인지
docker compose logs -f app        # 앱 로그만 보기 (Ctrl+C 로 빠져나옴)
docker compose logs db            # DB 로그
```

### 4-3. 브라우저와 DB에서 확인

`http://localhost:8080` 에서 글을 등록한 뒤 DB를 직접 조회합니다.

```powershell
docker compose exec db mysql -uintro -p introdb
```
```sql
-- .env 의 MYSQL_PASSWORD 입력
SELECT id, name, title, created_at FROM intro;
exit
```

> `docker compose exec` 는 컨테이너 이름(`intro-stack-db-1`) 대신 **서비스 이름(`db`)** 을 씁니다.

### 4-4. 종료와 재시작

```powershell
docker compose down               # 컨테이너·네트워크 삭제. 볼륨(데이터)은 남음
docker compose up -d              # 다시 실행 → 아까 쓴 글이 그대로 있음
docker compose down -v            # 볼륨까지 삭제 → 데이터 초기화
```

| 명령 | 컨테이너 | 네트워크 | 볼륨(데이터) |
|---|---|---|---|
| `docker compose stop` | 정지(남음) | 남음 | 남음 |
| `docker compose down` | 삭제 | 삭제 | **남음** |
| `docker compose down -v` | 삭제 | 삭제 | **삭제** ⚠️ |

---

## 5. 핵심 개념 — healthcheck 는 왜 `-h 127.0.0.1` 인가?

MySQL 공식 이미지는 처음 시작할 때 다음 순서로 동작합니다.

1. 초기화용 **임시 서버** 실행 (외부 접속 불가, 소켓 연결만 가능) → DB·계정 생성
2. 임시 서버 종료
3. **진짜 서버** 실행 (3306 포트로 접속 가능)

`mysqladmin ping` 을 `-h` 없이 쓰면 **소켓**으로 검사하므로 **1단계 임시 서버에도 "살아있음"** 이 나옵니다. 그러면 compose가 너무 일찍 healthy로 판단해 앱이 접속에 실패할 수 있습니다.
`-h 127.0.0.1` 을 붙이면 **TCP** 로 검사하므로 3단계 진짜 서버가 떠야만 성공합니다.

실측 결과 (새 볼륨으로 DB를 띄우면서 0.3초 간격으로 두 방식을 동시에 검사):

| 시점 | 소켓 검사 (`-h` 없음) | TCP 검사 (`-h 127.0.0.1`) |
|---|---|---|
| 초기화 시작 | 실패 | 실패 |
| **임시 서버 실행 중** | **성공** ← 잘못된 신호 | 실패 |
| 진짜 서버 실행 | 성공 | **성공** ← 올바른 신호 |

> 💡 healthcheck 는 "프로세스가 떠 있나?"가 아니라 **"실제로 일을 받을 수 있나?"** 를 검사해야 합니다. 실무 장애 대응에서도 중요한 관점입니다.

---

## 6. 실험

### 실험 ① 서비스 이름이 주소다
`docker-compose.yml` 의 `DB_HOST: db` 를 `DB_HOST: localhost` 로 바꾸고 `docker compose up -d` →
`docker compose logs app` 에서 `Communications link failure` 를 확인하세요. (STEP 2 함정 ②와 같은 원리) 확인 후 원래대로 되돌립니다.

### 실험 ② 코드 수정을 반영하려면
`step2-mysql/app/src/main/resources/templates/list.html` 의 제목 문구를 바꾼 뒤

```powershell
docker compose up -d              # → 반영 안 됨 (기존 이미지 재사용)
docker compose up -d --build      # → 반영됨 (이미지를 다시 빌드하고 app 컨테이너만 교체)
```

DB 컨테이너는 바뀐 게 없으니 **그대로 유지**되는 것도 확인해 보세요 (`docker compose ps` 의 Up 시간).

### 실험 ③ 데이터 보존
글을 등록 → `docker compose down` → `up -d` → 글이 남아 있음 → `down -v` → `up -d` → 글이 사라짐.

---

## 7. 이 단계의 함정

| # | 증상 | 원인 | 해결 |
|---|---|---|---|
| ① | `required variable ... is missing a value` | `.env` 파일 없음 | `Copy-Item .env.example .env` |
| ② | `Access denied for user 'intro'` | `.env` 비밀번호를 바꿨지만 **볼륨에 예전 계정이 남아 있음** | `docker compose down -v` 후 다시 `up` (데이터 초기화됨) |
| ③ | 코드를 고쳤는데 그대로 | `--build` 없이 `up` | `docker compose up -d --build` |
| ④ | `port is already allocated` | STEP 1·2 컨테이너나 `bootRun` 이 8080 사용 중 | `docker rm -f intro-h2 intro-app` 등으로 정리 |
| ⑤ | `docker-compose: command not found` 또는 오래된 문법 경고 | 구버전 v1 (`docker-compose`) | **`docker compose`** (띄어쓰기, v2) 사용 |
| ⑥ | `build` 경로를 못 찾음 | 다른 폴더에서 실행 | `step3-compose` 폴더 안에서 실행 (경로가 이 폴더 기준) |

---

## 8. 마무리 — 세 단계를 돌아보며

| | STEP 1 | STEP 2 | STEP 3 |
|---|---|---|---|
| 한 일 | 앱을 이미지로 | 앱·DB 분리 + 네트워크 | 전체를 파일 하나로 |
| 실행 명령 수 | 2 | 5 + 대기 | **1** |
| 핵심 교훈 | 컨테이너는 버리는 것, 데이터는 볼륨에 | 설정은 밖에서 주입, 컨테이너 이름이 주소 | 선언형, healthcheck로 준비 상태 확인 |

### 체크리스트
- [ ] `docker compose up -d --build` 한 줄로 앱 + DB가 뜬다
- [ ] 로그에서 `db Healthy → app Starting` 순서를 확인했다
- [ ] `.env` 와 `.env.example` 의 차이, `.env` 를 Git에 올리면 안 되는 이유를 설명할 수 있다
- [ ] `down` 과 `down -v` 의 차이를 설명할 수 있다
- [ ] STEP 2의 각 명령이 compose 파일의 어느 줄이 됐는지 짚을 수 있다

### 다음 단계
- **CI/CD**: push할 때마다 자동으로 테스트·빌드·배포 → [Spring/Deploy](../Spring/Deploy/README.md)
- **클라우드**: 이 compose 구성을 서버(EC2 등)에서 실행하거나, DB를 관리형 서비스(RDS 등)로 바꾸기
  → 앱 이미지는 그대로 두고 **`DB_HOST` 만 RDS 주소로 바꾸면** 됩니다. STEP 2의 설정 외부화가 바로 이걸 위한 것입니다.
- **포트폴리오**: 캡스톤 README에 "`docker compose up` 한 줄로 실행 가능"을 적으면 평가자가 바로 실행해 볼 수 있습니다 → [Spring/Capstone](../Spring/Capstone/README.md)
