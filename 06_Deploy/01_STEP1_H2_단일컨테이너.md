# STEP 1 — H2 앱 하나를 컨테이너로 실행하기

> 📁 실습 폴더: [샘플/step1-h2](./샘플/step1-h2/)
> 🎯 목표: **코드를 한 줄도 바꾸지 않고**, 기존 intro 앱(H2 파일 DB)을 Docker 이미지로 만들어 컨테이너로 실행한다.

```
 내 PC                                   Docker
┌──────────────┐  ① bootJar   ┌─────────────────────────┐
│ 소스 코드     │ ───────────▶ │ build/libs/intro-...jar │
└──────────────┘              └────────────┬────────────┘
                                ② docker build (Dockerfile)
                                           ▼
                              ┌─────────────────────────┐
                              │ 이미지  intro-h2:1.0     │
                              └────────────┬────────────┘
                                ③ docker run -p 8080:8080
                                           ▼
 브라우저 localhost:8080 ───▶ ┌─────────────────────────┐
                              │ 컨테이너 intro-h2         │
                              │  java -jar app.jar       │
                              │  /app/data/introdb (H2)  │
                              └─────────────────────────┘
```

---

## 1. 원본과 무엇이 같고, 무엇이 추가됐나

`step1-h2` 는 [Spring/SpringBoot/샘플/intro](../Spring/SpringBoot/샘플/intro/) 를 그대로 복사한 프로젝트입니다.

| 구분 | 내용 |
|---|---|
| **그대로** | 모든 자바 코드, 템플릿, `application.properties` (H2 파일 모드 `jdbc:h2:./data/introdb`, H2 콘솔 켜짐) |
| **추가** | `Dockerfile`, `Dockerfile.multistage`, `.dockerignore` — 세 파일뿐입니다 |

Docker를 쓴다고 앱 코드를 바꿀 필요는 없습니다. **"앱을 어떤 상자에 담을지"만 따로 정의**하는 것이 Dockerfile입니다.

---

## 2. Dockerfile 한 줄씩 읽기

[샘플/step1-h2/Dockerfile](./샘플/step1-h2/Dockerfile) (주석은 줄였습니다)

```dockerfile
FROM eclipse-temurin:17-jre                         # ① 출발점: 자바17 실행환경이 깔린 리눅스
WORKDIR /app                                        # ② 작업 폴더
ENV TZ=Asia/Seoul                                   # ③ 시간대: 한국
RUN useradd -r -s /bin/false spring                 # ④ 앱 전용 사용자 만들기
COPY build/libs/intro-0.0.1-SNAPSHOT.jar app.jar    # ⑤ 내 PC의 jar → 이미지 안으로
RUN mkdir -p /app/data && chown -R spring:spring /app  # ⑥ 폴더 소유권 넘기기
USER spring                                         # ⑦ 이후로는 spring 사용자로 실행
EXPOSE 8080                                         # ⑧ 8080 포트를 쓴다는 표시
ENTRYPOINT ["java", "-jar", "app.jar"]              # ⑨ 컨테이너 시작 시 실행할 명령
```

| 지시어 | 하는 일 | 여기서 왜 이렇게? |
|---|---|---|
| `FROM` | 베이스 이미지 지정 | 컴파일은 PC에서 끝났으니 무거운 JDK 대신 **JRE** 만 사용 |
| `WORKDIR` | 이후 명령의 기준 폴더 | H2 URL이 `./data/introdb`(상대경로)라서 DB 파일이 **`/app/data/`** 에 생김 |
| `ENV` | 환경변수 설정 | 컨테이너 기본 시간대는 **UTC** → 안 바꾸면 작성 시각이 9시간 느리게 표시됨 (아래 함정 ③) |
| `RUN` | 이미지를 만드는 중에 명령 실행 | 사용자 생성, 폴더 생성·권한 변경 |
| `COPY` | PC 파일 → 이미지 | `*.jar` 대신 **정확한 파일명** 사용 (아래 함정 ②) |
| `USER` | 실행 사용자 변경 | root로 돌리면 앱이 털렸을 때 피해가 커짐 → **보안 기본기** |
| `EXPOSE` | 사용 포트 **표시** (문서 역할) | 실제로 포트를 여는 건 `docker run -p` |
| `ENTRYPOINT` | 컨테이너 시작 명령 | `bootRun` 이 아니라 **`java -jar`** 로 실행 |

> 💡 `RUN` 과 `ENTRYPOINT` 의 차이: `RUN` 은 **이미지를 만들 때 한 번**, `ENTRYPOINT` 는 **컨테이너를 켤 때마다** 실행됩니다.

### .dockerignore

`docker build` 를 하면 폴더 전체(빌드 컨텍스트)가 Docker 엔진으로 전송됩니다. [`.dockerignore`](./샘플/step1-h2/.dockerignore) 는 보내지 않을 파일 목록입니다.
특히 **`data/`(내 PC의 H2 DB 파일 = 개인 데이터)** 가 이미지에 섞여 들어가지 않게 막는 것이 중요합니다.

---

## 3. 실습

PowerShell을 열고 실습 폴더로 이동합니다.

```powershell
cd 06_Deploy\샘플\step1-h2
```

### 3-1. jar 빌드 (내 PC에서)

```powershell
.\gradlew.bat bootJar
dir build\libs          # intro-0.0.1-SNAPSHOT.jar (약 55MB) 가 보이면 성공
```

> 이 jar 하나에 앱 코드 + 스프링 + 내장 톰캣이 모두 들어 있습니다. `java -jar build\libs\intro-0.0.1-SNAPSHOT.jar` 로도 실행됩니다.

### 3-2. 이미지 빌드

```powershell
docker build -t intro-h2:1.0 .
docker images intro-h2
```

- `-t intro-h2:1.0` : 이미지 이름(`intro-h2`)과 태그(`1.0`, 버전 표시)
- 마지막 **`.`** : 빌드 컨텍스트 = 현재 폴더 (**빠뜨리기 쉬움!**)
- 첫 빌드는 베이스 이미지(`eclipse-temurin:17-jre`)를 내려받느라 시간이 걸립니다. 크기는 약 **400MB**.

### 3-3. 컨테이너 실행

```powershell
docker run -d --name intro-h2 -p 8080:8080 intro-h2:1.0
docker ps
docker logs -f intro-h2      # "Started IntroApplication" 이 보이면 Ctrl+C
```

| 옵션 | 의미 |
|---|---|
| `-d` | 백그라운드 실행 (터미널을 돌려줌) |
| `--name intro-h2` | 컨테이너 이름. 이후 `logs`, `stop` 등에 사용 |
| `-p 8080:8080` | **`내PC포트:컨테이너포트`**. 이게 없으면 브라우저에서 접속할 수 없음 |

### 3-4. 브라우저로 확인

`http://localhost:8080` 접속 → 자기소개서를 한 건 등록해 보세요. 한글이 잘 저장되고 작성 시각이 **현재 한국 시간**이면 성공입니다.

### 3-5. 컨테이너 안 들여다보기

```powershell
docker exec -it intro-h2 bash
```
```bash
whoami            # spring  ← root 가 아님
pwd               # /app
ls -l data        # introdb.mv.db  ← H2 DB 파일이 컨테이너 안에 생겼습니다
exit
```

---

## 4. 실험 ① — 컨테이너를 지우면 데이터는?

```powershell
docker rm -f intro-h2                                       # 컨테이너 삭제
docker run -d --name intro-h2 -p 8080:8080 intro-h2:1.0     # 같은 이미지로 다시 실행
```

브라우저를 새로고침하면 **방금 쓴 글이 사라져 있습니다.**
DB 파일이 **컨테이너 안**(`/app/data`)에 있었기 때문입니다. 이미지는 그대로지만 컨테이너가 새로 만들어졌습니다.

**해결: 볼륨(volume)** — DB 폴더를 컨테이너 밖 저장소에 연결합니다.

```powershell
docker rm -f intro-h2
docker run -d --name intro-h2 -p 8080:8080 -v intro-h2-data:/app/data intro-h2:1.0
```

- `-v intro-h2-data:/app/data` = "`intro-h2-data` 라는 볼륨을 컨테이너의 `/app/data` 에 연결"
- 글을 등록한 뒤 `docker rm -f intro-h2` → 위 명령으로 다시 실행 → **글이 남아 있습니다.**
- `docker volume ls` 로 볼륨 목록을 확인할 수 있습니다.

> 💡 **교훈:** 컨테이너는 "언제든 버리고 새로 만드는 것"입니다. 남겨야 할 데이터는 **반드시 볼륨**에 두세요.
> 이 문제가 STEP 2에서 **DB를 아예 별도 컨테이너로 분리**하는 이유로 이어집니다.

---

## 5. 실험 ② — H2 콘솔 접속하기

원본처럼 `http://localhost:8080/h2-console` 에 접속하면 이런 메시지가 나옵니다.

```
Sorry, remote connections ('webAllowOthers') are disabled on this server.
```

**왜?** H2 콘솔은 보안상 **같은 컴퓨터(localhost)에서 온 요청만** 받습니다.
그런데 컨테이너 입장에서 브라우저는 "내 PC → Docker 가상 네트워크"를 거쳐 온 **외부 요청**입니다.

**해결: 이미지를 고치지 않고 환경변수로 설정을 덮어쓰기**

```powershell
docker rm -f intro-h2
docker run -d --name intro-h2 -p 8080:8080 -v intro-h2-data:/app/data `
  -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true intro-h2:1.0
```

- 스프링부트는 `spring.h2.console.settings.web-allow-others` 같은 설정을 **환경변수 `SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS`** 로도 받습니다. (점·하이픈 → 밑줄, 대문자)
- 콘솔 로그인: JDBC URL `jdbc:h2:./data/introdb`, 사용자 `sa`, 비밀번호 없음

> ⚠️ **실습용으로만 켜세요.** 이 설정은 누구나 DB 콘솔에 접속할 수 있게 만듭니다. 외부에 공개되는 서버에서는 절대 켜지 마세요.
> 💡 **"설정은 이미지 밖에서 주입한다"** — 이 아이디어가 STEP 2의 핵심입니다.

---

## 6. 심화 — 멀티스테이지 빌드

지금 방식은 **내 PC에서 jar를 먼저 만들어야** 합니다. 그래서 PC마다 JDK 버전이 다르면 결과가 달라질 수 있습니다.
[`Dockerfile.multistage`](./샘플/step1-h2/Dockerfile.multistage) 는 **jar 빌드까지 Docker 안에서** 합니다.

```dockerfile
# 1단계: JDK 이미지에서 빌드
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew \
 && ./gradlew bootJar -x test --no-daemon

# 2단계: JRE 이미지에 jar 만 복사
FROM eclipse-temurin:17-jre
...
COPY --from=build /workspace/build/libs/intro-0.0.1-SNAPSHOT.jar app.jar
```

```powershell
docker build -f Dockerfile.multistage -t intro-h2:multi .    # -f 로 Dockerfile 이름 지정
```

| | 단일 스테이지 (`Dockerfile`) | 멀티스테이지 (`Dockerfile.multistage`) |
|---|---|---|
| jar 빌드 위치 | 내 PC | Docker 안 (1단계) |
| 내 PC에 JDK 필요 | 필요 | **불필요** |
| 결과의 일관성 | PC 환경에 따라 다를 수 있음 | **누가 빌드해도 동일** |
| 최종 이미지 크기 | 약 400MB | 약 400MB (**JDK·소스·Gradle 캐시는 1단계에 버려짐**) |
| 빌드 시간 | 빠름 | 첫 빌드가 느림 (Gradle·라이브러리 다운로드) |

> **STEP 2·3의 앱 이미지는 멀티스테이지를 기본으로 씁니다.** compose 가 `--build` 한 번으로 소스부터 이미지까지 만들 수 있기 때문입니다.
> `sed -i 's/\r$//' gradlew` 는 Windows에서 편집되어 줄바꿈이 CRLF가 된 `gradlew` 도 리눅스에서 실행되게 하는 안전장치입니다.

---

## 7. 이 단계의 함정 (실측으로 확인한 것들)

| # | 증상 | 원인 | 해결 |
|---|---|---|---|
| ① | 앱이 시작하자마자 죽음, 로그에 H2 파일 생성 권한 오류 | `USER spring` 으로 바꿨는데 `/app` 이 root 소유 | `mkdir -p /app/data && chown -R spring:spring /app` |
| ② | `COPY` 단계에서 실패 (파일이 여러 개) | `gradlew build` 를 쓰면 실행 불가능한 `-plain.jar` 도 생김 | `*.jar` 대신 **정확한 파일명** 사용 |
| ③ | 작성 시각이 한국 시간보다 **9시간 느림** | 컨테이너 기본 시간대가 UTC | `ENV TZ=Asia/Seoul` |
| ④ | `docker run` 에서 `port is already allocated` | IDE의 `bootRun` 이나 다른 컨테이너가 8080 사용 중 | 그 앱을 끄거나 `-p 8081:8080` 처럼 PC 쪽 포트 변경 |
| ⑤ | `Conflict. The container name "/intro-h2" is already in use` | 같은 이름의 컨테이너가 남아 있음 (정지 상태 포함) | `docker rm -f intro-h2` 후 다시 실행 |
| ⑥ | 브라우저 접속 불가 | `-p 8080:8080` 누락 | 컨테이너 삭제 후 `-p` 붙여 다시 실행 |
| ⑦ | 코드를 고쳤는데 반영 안 됨 | 이미지는 빌드 시점의 jar로 고정 | `bootJar` → `docker build` → 컨테이너 재생성 |

---

## 8. 체크리스트 & 정리

- [ ] `docker images` 에 `intro-h2:1.0` 이 있다
- [ ] 브라우저에서 등록·목록·상세가 동작하고 시간이 한국 시간이다
- [ ] 볼륨 없이 지우면 데이터가 사라지고, 볼륨을 쓰면 남는 것을 확인했다
- [ ] `-p 내PC:컨테이너`, `-v 볼륨:경로`, `-e 환경변수` 의 의미를 설명할 수 있다

```powershell
docker rm -f intro-h2                 # STEP 2 로 가기 전에 8080 을 비웁니다
docker volume rm intro-h2-data        # (선택) 볼륨까지 삭제
```

➡️ 다음: [STEP 2 — MySQL로 바꾸고 컨테이너 2개 통신시키기](./02_STEP2_MySQL_컨테이너간_통신.md)
