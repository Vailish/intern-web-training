# STEP 4 — Docker Hub에 이미지 올리고, 받아서 실행하기

> 📁 실습 폴더: [샘플/step4-dockerhub](./샘플/step4-dockerhub/)
> 🎯 목표: STEP 2에서 만든 이미지를 **Docker Hub에 올리고(push)**, **소스코드가 없는 곳에서 이미지만 받아(pull)** 서비스를 실행한다.

```
 ┌──────── 만드는 쪽 (내 PC) ────────┐                    ┌──────── 실행하는 쪽 (짝꿍 PC / 서버) ────────┐
 │ 소스코드 + Dockerfile             │                    │ 소스코드 없음!                                │
 │                                   │   docker push      │                                               │
 │ docker build  → intro-app:1.0     │ ─────────────────▶ │                                               │
 │ docker tag    → 내아이디/intro-app │    Docker Hub      │ docker-compose.yml + .env  (파일 2개뿐)       │
 │ docker push                       │  (이미지 보관소)   │ docker compose pull  → 이미지 내려받기         │
 │                                   │ ◀───────────────── │ docker compose up -d → localhost:8080         │
 └───────────────────────────────────┘   docker pull      └───────────────────────────────────────────────┘
```

STEP 3까지는 **이미지를 만든 PC에서 그대로 실행**했습니다. 하지만 실제 배포에서는 **만드는 곳과 실행하는 곳이 다릅니다.**
운영 서버에 소스코드를 올려 빌드하지 않습니다. **완성된 이미지를 보관소에 올리고, 서버는 받아서 실행만** 합니다.

---

## 1. 왜 Docker Hub인가 — GitHub와 비교

| | GitHub | Docker Hub |
|---|---|---|
| 올리는 것 | **소스코드** | **완성된 이미지** (실행 환경 + jar) |
| 받는 쪽이 할 일 | JDK 설치 → 빌드 → 실행 | **바로 실행** (`docker run` / `compose up`) |
| 비유 | 레시피 공유 | **완성된 밀키트 택배** |
| 명령 | `git push` / `git clone` | `docker push` / `docker pull` |

이미지 보관소를 **레지스트리(Registry)** 라고 합니다. Docker Hub는 가장 널리 쓰이는 **공개 레지스트리**이고,
지금까지 받아 쓴 `mysql:8.4`, `eclipse-temurin:17-jre` 도 모두 Docker Hub에서 내려받은 것입니다.
(회사에서는 AWS ECR, GitHub Container Registry(ghcr.io), 사내 Harbor 같은 **비공개 레지스트리**도 많이 씁니다. 원리는 같습니다.)

---

## 2. 이미지 이름의 구조 — 이것만 알면 절반은 끝

```
docker.io / 내아이디 / intro-app : 1.0
──────────  ────────   ─────────   ───
레지스트리  사용자명    저장소       태그(버전)
(생략 가능) (네임스페이스) (repository)
```

| 쓴 이름 | Docker가 해석하는 진짜 이름 | 설명 |
|---|---|---|
| `mysql:8.4` | `docker.io/library/mysql:8.4` | 사용자명이 없으면 **공식 이미지(library)** |
| `내아이디/intro-app:1.0` | `docker.io/내아이디/intro-app:1.0` | 레지스트리를 생략하면 **Docker Hub** |
| `intro-app:1.0` | `docker.io/library/intro-app:1.0` | ⚠️ 이 이름으로 push하면 **공식 이미지 자리에 올리려는 것** → 거부됨 |
| `localhost:5000/study/intro-app:1.0` | 그대로 | 맨 앞에 주소가 있으면 **그 레지스트리** (7장에서 사용) |

그래서 push 전에 반드시 **`사용자명/` 이 붙은 이름**을 만들어야 합니다. 이 작업이 `docker tag` 입니다.

---

## 3. 준비 — 계정과 로그인

### 3-1. Docker Hub 가입
[hub.docker.com](https://hub.docker.com) 에서 가입합니다. **사용자명(Docker ID)** 이 이미지 이름 앞에 붙으니 기억해 두세요.

### 3-2. 액세스 토큰(PAT) 만들기 — 비밀번호 대신 사용
Docker Hub → 오른쪽 위 프로필 → **Account settings → Personal access tokens → Generate new token**
(메뉴 위치는 화면 개편에 따라 조금 다를 수 있습니다)

| 항목 | 권장 값 | 이유 |
|---|---|---|
| 설명 | `intern-training-pc` | 어느 PC용 토큰인지 알 수 있게 |
| 만료 | 30일 등 짧게 | 유출돼도 피해 기간이 짧음 |
| 권한 | **Repo Read & Write** | push(쓰기)+pull(읽기)면 충분. 삭제 권한(Delete)은 주지 않음 |

> 🔐 **토큰은 만들 때 딱 한 번만 보입니다.** 바로 복사해 두세요. 그리고 토큰을 **코드·문서·채팅에 붙여넣지 마세요.**
> 비밀번호 대신 토큰을 쓰는 이유: 유출되면 **그 토큰만 삭제**하면 되고, 권한을 좁힐 수 있으며, 2단계 인증(2FA)을 켠 계정은 CLI에서 비밀번호 로그인이 **안 됩니다.**

### 3-3. 로그인

```powershell
$HUB = "내아이디"            # ← 본인 Docker Hub 사용자명으로 바꾸기 (이후 명령에서 계속 씀)
docker login -u $HUB
# Password: 에 비밀번호 대신 "액세스 토큰"을 붙여넣기 (화면에 안 보이는 게 정상)
```
```
Login Succeeded
```

> 💡 `-u` 없이 `docker login` 만 치면 **웹 브라우저 로그인**(화면에 나온 코드를 브라우저에 입력)으로 진행됩니다. 둘 다 됩니다.
> ⚠️ **공용·교육장 PC라면 실습이 끝난 뒤 반드시 `docker logout`.** 로그인 정보가 PC에 남아 다음 사람이 내 계정으로 push할 수 있습니다.

### 3-4. 무료 플랜에서 알아둘 제한 (2026-11-09 Docker 공식 문서·요금 페이지 기준, 바뀔 수 있음)

| 항목 | 무료(Personal) | 이 실습에 주는 영향 |
|---|---|---|
| 공개(public) 저장소 | 무제한 (공정 사용 조건) | 이 실습은 **공개 저장소**로 진행 |
| 비공개(private) 저장소 | **1개** | 캡스톤처럼 숨기고 싶은 이미지에 아껴 쓰기 |
| pull 횟수 — 로그인 안 함 | **IP당 6시간에 100회** | ⚠️ 교육장은 **모두 같은 공인 IP** → 한도를 같이 씀 |
| pull 횟수 — 로그인 함 | 계정당 6시간에 200회 | 그래서 **실습 전 로그인 권장** |

> 공개 저장소에 올린 이미지는 **전 세계 누구나 받아서 열어 볼 수 있습니다.** → 6-2절 "이미지는 공개 문서다"를 꼭 읽으세요.

---

## 4. 실습 A — 올리는 쪽 (tag → push)

### 4-1. 올릴 이미지 준비
STEP 2(또는 3)에서 만든 `intro-app:1.0`, `intro-db:1.0` 을 그대로 씁니다. 지웠다면 다시 빌드합니다.

```powershell
docker images intro-*              # 둘 다 있는지 확인
# 없으면 ↓ (06_Deploy\샘플\step2-mysql 폴더에서)
docker build -t intro-db:1.0 .\db
docker build -t intro-app:1.0 .\app
```

### 4-2. Docker Hub용 이름 붙이기 (`docker tag`)

```powershell
docker tag intro-app:1.0 "$HUB/intro-app:1.0"
docker tag intro-db:1.0  "$HUB/intro-db:1.0"
docker images
```
```
REPOSITORY              TAG   IMAGE ID
intro-app               1.0   00327f78cf9c   ┐ IMAGE ID 가 같다!
내아이디/intro-app       1.0   00327f78cf9c   ┘ → 복사본이 아니라 "이름표(별명)"를 하나 더 붙인 것
intro-db                1.0   4902691b214c
내아이디/intro-db        1.0   4902691b214c
```

> 💡 `docker tag` 는 이미지를 복사하지 않습니다. 디스크 용량도 늘지 않습니다. **같은 이미지에 이름표만 하나 더** 붙입니다.

### 4-3. 올리기 (`docker push`)

```powershell
docker push "$HUB/intro-app:1.0"
docker push "$HUB/intro-db:1.0"
```
```
3a0b01922d80: Pushed
4328b2d1f062: Pushed
6e3c769591a8: Mounted from library/eclipse-temurin   ← 공식 이미지에 이미 있는 층은 올리지 않음
...
1.0: digest: sha256:225bb0e1...e78ac05e size: 2414
```

| 출력 | 의미 |
|---|---|
| `Pushed` | 이 층(layer)을 실제로 업로드함 |
| `Mounted from ...` / `Layer already exists` | 레지스트리에 **이미 있는 층** → 업로드 생략 (그래서 두 번째 push부터 빠름) |
| `digest: sha256:...` | 이 이미지 내용의 **지문**. 태그는 바뀔 수 있지만 digest는 내용이 같으면 영원히 같음 |

### 4-4. Docker Hub 웹에서 확인
`https://hub.docker.com/r/내아이디/intro-app` → **Tags** 탭에 `1.0` 이 보이면 성공입니다.
저장소가 **Public** 인지 확인하세요 (Private이면 짝꿍이 로그인 없이 받을 수 없습니다).

---

## 5. 실습 B — 받는 쪽 (pull → run)

### 5-1. "다른 PC" 만들기
- **권장: 짝 실습** — 짝꿍의 Docker Hub 아이디로 짝꿍 이미지를 받아 실행해 봅니다. 진짜 "남이 만든 이미지 배포"입니다.
- **혼자라면** — 내 PC의 로컬 이미지를 **모두 지워서** 다른 PC인 척합니다.

```powershell
docker compose -f ..\step3-compose\docker-compose.yml down        # STEP 3 이 떠 있으면 내리기 (8080 충돌 방지)
docker rm -f intro-app intro-db                                    # STEP 2 컨테이너가 남아 있으면 이미지가 안 지워짐
docker rmi intro-app:1.0 intro-db:1.0 "$HUB/intro-app:1.0" "$HUB/intro-db:1.0"
docker images intro-*            # 아무것도 안 나와야 함
```

### 5-2. 실행 폴더 살펴보기 — 소스가 없다

```
샘플/step4-dockerhub/
├─ docker-compose.yml     ← build 없음, image 만 있음
├─ .env.example           ← DOCKERHUB_USER, 태그, 비밀번호 견본
└─ .gitignore
```

**STEP 3과 다른 점은 딱 하나**입니다.

| | STEP 3 `docker-compose.yml` | STEP 4 `docker-compose.yml` |
|---|---|---|
| 앱 | `build: ../step2-mysql/app` + `image: intro-app:1.0` | `image: ${DOCKERHUB_USER}/intro-app:${APP_TAG}` |
| DB | `build: ../step2-mysql/db` + `image: intro-db:1.0` | `image: ${DOCKERHUB_USER}/intro-db:${DB_TAG}` |
| 필요한 것 | 소스코드 + Dockerfile + compose | **compose + .env 뿐** |
| 이미지 출처 | 이 PC에서 **빌드** | 레지스트리에서 **다운로드** |

### 5-3. `.env` 설정

```powershell
cd 06_Deploy\샘플\step4-dockerhub
Copy-Item .env.example .env
notepad .env
```
```properties
DOCKERHUB_USER=내아이디          # 짝 실습이면 짝꿍 아이디
APP_TAG=1.0
DB_TAG=1.0
MYSQL_ROOT_PASSWORD=change-me-root
MYSQL_USER=intro
MYSQL_PASSWORD=change-me
```

> 💡 **비밀번호는 이미지 안에 없습니다.** 이미지는 공개해도, 비밀번호는 **실행하는 쪽의 `.env`** 에만 있습니다. STEP 2에서 설정을 외부화한 덕분입니다.

### 5-4. 받아서 실행

```powershell
docker compose pull         # 이미지 내려받기 (up 이 알아서 하지만 눈으로 확인하려고 따로)
docker compose up -d
docker compose ps
```
```
 Image 내아이디/intro-db:1.0 Pulled
 Image 내아이디/intro-app:1.0 Pulled
 ...
 Container intro-hub-db-1 Healthy
 Container intro-hub-app-1 Started

SERVICE  IMAGE                    STATUS
app      내아이디/intro-app:1.0    Up
db       내아이디/intro-db:1.0     Up (healthy)
```

`http://localhost:8080` 에서 글을 등록해 보세요. **이 PC에는 소스코드도, JDK도, Gradle도 필요 없었습니다.**

> 🧩 compose 없이 하나만 받아 보고 싶다면 `docker pull "$HUB/intro-app:1.0"` 도 됩니다. `docker run` 은 로컬에 이미지가 없으면 **자동으로 pull** 합니다.

---

## 6. 새 버전 배포와 롤백 — 태그를 바꾸는 것이 곧 배포

### 6-1. 업데이트 흐름

```
[만드는 쪽]  코드 수정 → docker build -t intro-app:1.1 → docker tag → docker push "$HUB/intro-app:1.1"
[실행하는 쪽] .env 의 APP_TAG=1.1 로 수정 → docker compose up -d
```

```powershell
# 만드는 쪽 (step2-mysql 폴더): list.html 제목 문구 등을 고친 뒤
docker build -t intro-app:1.1 .\app
docker tag intro-app:1.1 "$HUB/intro-app:1.1"
docker push "$HUB/intro-app:1.1"          # 바뀐 층만 올라가서 1.0 때보다 훨씬 빠름

# 실행하는 쪽 (step4-dockerhub 폴더): .env 에서 APP_TAG=1.1 로 바꾼 뒤
docker compose up -d
docker compose ps
```
```
SERVICE  IMAGE                    STATUS
app      내아이디/intro-app:1.1    Up 3 seconds          ← 앱만 새 이미지로 교체
db       내아이디/intro-db:1.0     Up 45 seconds (healthy) ← DB는 그대로, 데이터도 그대로
```

### 6-2. 롤백 — 문제가 생기면 이전 태그로
`.env` 의 `APP_TAG=1.0` 으로 되돌리고 `docker compose up -d`. **끝입니다.**
이전 버전 이미지가 레지스트리에 그대로 남아 있으므로, 다시 빌드할 필요가 없습니다. 이것이 **태그에 버전을 붙이는 이유**입니다.

---

## 7. 꼭 알아야 할 개념 3가지

### 7-1. `latest` 는 "최신"이 아니다
`latest` 는 태그를 생략했을 때 붙는 **기본 이름표일 뿐**, Docker가 최신 버전을 찾아 주는 기능이 아닙니다.

| 문제 | 설명 |
|---|---|
| 덮어쓰기 | 오늘 push한 `latest` 가 어제의 `latest` 를 덮어씀 → **같은 이름, 다른 내용** |
| 갱신 안 됨 | 서버에 이미 `latest` 가 있으면 `compose up` 은 **다시 받지 않음** → "올렸는데 반영이 안 돼요" |
| 롤백 불가 | 어제 버전이 무엇이었는지 이름으로 찾을 수 없음 |

> ✅ 실무 습관: **버전(`1.1`)이나 Git 커밋 해시(`a1b2c3d`)로 태그**를 붙이고, `latest` 는 편의용 별명으로만 씁니다.
> 완벽하게 고정하고 싶으면 digest로 받습니다: `docker pull 내아이디/intro-app@sha256:225bb0e1...`

### 7-2. 이미지는 공개 문서다 — 비밀은 절대 넣지 않는다
공개 저장소의 이미지는 누구나 받아서 **안을 열어 볼 수 있습니다.** 직접 확인해 보세요.

```powershell
docker history --no-trunc "$HUB/intro-db:1.0" | Select-String "ENV MYSQL"
```
```
ENV MYSQL_DATABASE=introdb       ← Dockerfile 에 쓴 ENV 는 누구나 볼 수 있음
```

| 하지 말 것 | 대신 |
|---|---|
| Dockerfile에 `ENV DB_PASSWORD=...` | 실행 시 `-e` / compose `environment` / `.env` (STEP 2·3 방식) |
| `ARG` 로 비밀번호 전달 | `ARG` 값도 `docker history` 에 남음 |
| `.env`·키 파일이 `COPY . .` 에 딸려 들어감 | `.dockerignore` 에 `.env` 등록, 필요한 것만 `COPY` |

> 🔐 실수로 비밀이 들어간 이미지를 push했다면 **이미지 삭제만으로는 부족합니다**(이미 누가 받았을 수 있음). **비밀번호·키부터 즉시 교체**하세요.

### 7-3. CPU 아키텍처 — Mac(M 칩)에서 만든 이미지가 서버에서 안 돈다
이미지는 **CPU 종류(아키텍처)별로** 만들어집니다. 일반 Windows PC·대부분의 클라우드 서버는 `amd64`, Apple Silicon Mac은 `arm64` 입니다.

```powershell
docker image inspect intro-app:1.1 --format '{{.Os}}/{{.Architecture}}'   # linux/amd64
```

Mac에서 `docker build` 만 하고 push한 이미지를 amd64 PC/서버에서 받으면:
```
no matching manifest for linux/amd64 in the manifest list entries
```
(이미지 종류에 따라 받는 데는 성공하고 실행할 때 `exec format error` 로 죽기도 합니다)

해결: **두 아키텍처용을 한 번에 빌드해서 push** 합니다.
```powershell
docker buildx create --name multi --driver docker-container --use     # 처음 한 번
docker buildx build --platform linux/amd64,linux/arm64 -t "$HUB/intro-app:1.1" --push .\app
docker buildx imagetools inspect "$HUB/intro-app:1.1"                 # Platform: linux/amd64, linux/arm64 확인
```

> ⚠️ 기본 빌더로 하면 `Multi-platform build is not supported for the docker driver` 에러가 납니다 → 위처럼 `docker-container` 빌더를 만드세요.
> ⏱️ 다른 아키텍처는 **에뮬레이션으로 빌드**하므로 느립니다. 검증 PC(Windows, amd64)에서 Gradle 빌드 포함 약 **5분** 걸렸습니다.
> 팀원·서버가 모두 amd64면 굳이 필요 없지만, **Mac 사용자가 섞인 팀**이라면 꼭 알아야 합니다.
> 🧪 8장의 로컬 레지스트리(`localhost:5000`)로 buildx push를 연습할 때는 빌더를 `--driver-opt network=host` 옵션을 붙여 만들어야 합니다. (빌더도 컨테이너라서, 옵션이 없으면 빌더 자신의 localhost를 찾습니다)

---

## 8. 계정 없이 연습하기 — 로컬 레지스트리

교육장에서 Docker Hub 가입이 어렵거나, 공개 저장소에 올리기 부담스러우면 **내 PC에 레지스트리를 띄워** 똑같이 연습할 수 있습니다.
(이 문서의 모든 명령은 이 방법으로 실제 검증했습니다)

```powershell
docker run -d -p 5000:5000 --name intro-registry registry:2       # 레지스트리 서버 실행

docker tag intro-app:1.0 localhost:5000/study/intro-app:1.0       # 이름 맨 앞 = 레지스트리 주소
docker tag intro-db:1.0  localhost:5000/study/intro-db:1.0
docker push localhost:5000/study/intro-app:1.0
docker push localhost:5000/study/intro-db:1.0
curl.exe http://localhost:5000/v2/_catalog                         # {"repositories":["study/intro-app","study/intro-db"]}
```

받는 쪽 `.env` 에서 `DOCKERHUB_USER=localhost:5000/study` 로 바꾸면 5장 실습을 그대로 할 수 있습니다.

> ⚠️ 이 레지스트리는 **인증·암호화(HTTPS)가 없는 연습용**입니다. 다른 PC에 열어 주거나 실제 서비스에 쓰지 마세요.
> 연습 후: `docker rm -f -v intro-registry` (`-v` 를 붙여야 레지스트리 안의 이미지 저장 공간도 같이 지워집니다)

---

## 9. 이 단계의 함정

> ①②③④⑧ 은 검증 때 실제로 재현한 메시지이고, ⑥⑦ 은 Docker Hub 계정 상태에 따라 나는 메시지라 문구가 조금 다를 수 있습니다.

| # | 증상 | 원인 | 해결 |
|---|---|---|---|
| ① | push 시 `denied: requested access to the resource is denied` | `사용자명/` 없이 push (`intro-app:1.0` → 공식 이미지 자리), 로그인 안 함, 사용자명 오타, 토큰이 Read-only | `docker tag` 로 `"$HUB/intro-app:1.0"` 이름 붙이기, `docker login`, 토큰 권한 Read & Write |
| ② | pull 시 `pull access denied for ..., repository does not exist or may require 'docker login'` | 저장소 이름 오타, **Private 저장소**, 아직 push 안 함 | Docker Hub 웹에서 이름·Public 여부 확인 |
| ③ | `manifest for ...:1.1 not found: manifest unknown` | 그 **태그**가 레지스트리에 없음 | `.env` 의 태그 확인, 만드는 쪽에서 해당 태그 push 했는지 확인 |
| ④ | `no matching manifest for linux/amd64 in the manifest list entries` | Mac(arm64) 전용 이미지 | 7-3절 `buildx --platform linux/amd64,linux/arm64` |
| ⑤ | 새로 push했는데 실행 결과가 그대로 | **같은 태그**로 덮어쓰고, 받는 쪽엔 예전 이미지가 이미 있음 (`up` 은 있으면 다시 안 받음) | 새 태그 사용(권장) 또는 `docker compose pull` 후 `up -d` |
| ⑥ | `toomanyrequests: You have reached your pull rate limit` | 로그인 없이 pull, 교육장 전체가 같은 IP | `docker login` 후 다시 시도 |
| ⑦ | 로그인 시 `unauthorized: incorrect username or password` | 2단계 인증 계정에 비밀번호 입력, 토큰 만료·삭제 | 새 액세스 토큰 발급 후 토큰으로 로그인 |
| ⑧ | `required variable DOCKERHUB_USER is missing a value` | `.env` 없음 | `Copy-Item .env.example .env` |

---

## 10. 마무리

| | STEP 3 | STEP 4 |
|---|---|---|
| 이미지 출처 | 내 PC에서 빌드 | **레지스트리에서 다운로드** |
| 실행에 필요한 것 | 소스 + Dockerfile + compose | **compose + .env** |
| 새 버전 반영 | `up -d --build` | **태그 변경** + `up -d` |
| 롤백 | 소스를 되돌려 다시 빌드 | **이전 태그로** `up -d` |

### 체크리스트
- [ ] `docker.io/사용자명/저장소:태그` 각 부분의 의미를 설명할 수 있다
- [ ] `docker tag` 가 이미지를 복사하지 않는다는 것을 IMAGE ID로 확인했다
- [ ] 내(또는 짝꿍) 이미지를 **소스 없이** pull 해서 `localhost:8080` 으로 띄웠다
- [ ] 태그를 1.1로 바꿔 앱만 교체되고 DB 데이터는 남는 것을 확인했다
- [ ] `latest` 를 피하는 이유, 이미지에 비밀번호를 넣으면 안 되는 이유를 설명할 수 있다
- [ ] (공용 PC라면) `docker logout` 했다

### 청소

```powershell
docker compose down -v                                  # step4-dockerhub 폴더에서
docker rmi "$HUB/intro-app:1.0" "$HUB/intro-app:1.1" "$HUB/intro-db:1.0"
docker rm -f -v intro-registry                          # 8장 로컬 레지스트리를 썼다면
docker buildx rm multi                                  # 7-3절 빌더를 만들었다면
docker logout
```
> Docker Hub에 올린 저장소를 지우려면 웹에서 저장소 → **Settings → Delete repository**.

### 다음 단계
지금은 **사람이** build → tag → push 하고, **사람이** 서버에서 pull 합니다.
다음은 이 과정을 자동화하는 **CI/CD** 입니다: GitHub에 코드를 push하면 GitHub Actions가 테스트 → 이미지 빌드 → Docker Hub push까지 하고, 서버는 새 태그를 받아 실행합니다.
→ [Spring/Deploy](../Spring/Deploy/README.md)
