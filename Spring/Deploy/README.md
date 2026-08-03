# 배포하기 — Docker → GitHub Actions(CI/CD) → AWS

지금까지 만든 앱은 **내 PC에서만** 돌았습니다. 취업 포트폴리오에서 가장 강력한 한 방은
**"인터넷에서 접속되는 데모 URL"** 입니다. 이 모듈은 앱을 **바깥 세상에 올리는 길**을 안내합니다.

> 💡 **왜 중요한가?** 채용 조사에서 **"실제 배포·운영 경험"은 AWS 자격증보다 우선하는 신입 차별화 요소**로 꼽혔습니다.
> "돌아가는 서비스 URL"은 면접관에게 "이 사람은 실제로 만들어 봤다"는 확신을 줍니다.

---

## 이 모듈에서 배우는 것

1. **로컬 실행** → **Docker/compose** → **클라우드(AWS)** 로 이어지는 배포 3단계
2. 배포용 **Dockerfile(멀티스테이지)** 과 **docker-compose(앱+MySQL)**
3. **GitHub Actions** 로 push마다 자동 테스트(CI) / 자동 배포(CD)
4. **AWS EC2 + RDS** 배포 개요와 **보안·비용 주의점**

완성 예시 파일: [샘플/](./샘플/) — `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `github-workflows/`
> 이 파일들을 여러분 프로젝트(예: 캡스톤) **루트**에 복사해 쓰세요. `github-workflows/` 안의 파일은 **`.github/workflows/`** 로 옮겨야 GitHub가 인식합니다.

> 🔗 개념이 처음이라면 먼저 참고자료 PDF를 보세요:
> [컨테이너·Docker·쿠버네티스 입문](../../99_참고자료/), [Dockerfile·docker-compose 실습](../../99_참고자료/)

---

## 1. 배포 3단계 로드맵

```
[1] 로컬 실행         ./gradlew bootRun            내 PC (localhost:8080)
        │
[2] 컨테이너화        docker compose up --build    내 PC, 하지만 "어디서든 똑같이" 도는 형태
        │
[3] 클라우드 배포     AWS EC2 + RDS                인터넷에서 접속되는 진짜 서버
```

한 번에 3단계로 뛰지 말고 **아래에서 위로** 올라가세요. 각 단계가 다음 단계의 토대입니다.

---

## 2. Dockerfile — 앱을 "이미지"로 포장 (멀티스테이지)

[샘플/Dockerfile](./샘플/Dockerfile) 은 **2단계(멀티스테이지)** 구조입니다.

| 단계 | 베이스 이미지 | 하는 일 |
|---|---|---|
| **build** | `eclipse-temurin:17-jdk` | 소스를 컴파일해 실행 `jar` 생성 |
| **runtime** | `eclipse-temurin:17-jre` | 만들어진 jar만 담아 **가벼운 최종 이미지** 완성 |

- **왜 나누나?** 컴파일엔 무거운 JDK가 필요하지만, 실행엔 가벼운 JRE면 충분합니다. 최종 이미지에서 JDK·소스를 빼 **크기를 크게 줄입니다.**
- **보안**: `root` 대신 전용 사용자(`spring`)로 실행합니다.

> ⚠️ **함정(실측)**: 비루트 사용자로 바꾸면 `/app` 이 root 소유라 앱이 로그·H2 파일을 못 만들고 죽습니다.
> → `RUN chown -R spring:spring /app` 로 소유권을 넘겨야 합니다.

```bash
# 이미지 빌드 & 실행 (앱 단독, H2로)
docker build -t myapp .
docker run -p 8080:8080 myapp
```

---

## 3. docker-compose — 앱 + MySQL 한 번에

실제 서비스는 앱 혼자가 아니라 **DB와 함께** 돕니다. [샘플/docker-compose.yml](./샘플/docker-compose.yml) 은 앱 + MySQL을 한 번에 띄웁니다.

```bash
docker compose up --build      # 시작 (앱: localhost:8080, DB: 3306)
docker compose down            # 종료  (-v 를 붙이면 DB 데이터까지 삭제)
```

**꼭 기억할 2가지 (실무 단골 실수):**
- ⭐ **접속 호스트는 `localhost` 가 아니라 서비스 이름 `db`** 입니다 → `jdbc:mysql://db:3306/introdb`.
  컨테이너끼리는 compose가 만든 내부 네트워크에서 **서비스 이름**으로 통신합니다.
- ⭐ `depends_on` + `healthcheck` 로 **DB가 준비된 뒤 앱을 시작**합니다. (DB가 뜨기 전에 앱이 붙으면 커넥션 실패)

---

## 4. CI — GitHub Actions로 "push마다 자동 테스트"

[샘플/github-workflows/ci.yml](./샘플/github-workflows/ci.yml) 을 `.github/workflows/ci.yml` 로 두면,
**push/PR 때마다 GitHub가 자동으로 `./gradlew test`** 를 돌립니다.

- [Testing 모듈](../Testing/)에서 만든 테스트들이 여기서 자동 실행됩니다 → **깨진 코드가 merge되는 것을 막습니다.**
- PR에 초록/빨강 체크가 표시되어, 리뷰 전에 문제를 발견합니다.
- 이력서에 "CI로 테스트 자동화"를 쓸 수 있는 근거가 됩니다.

> 💡 gradle 프로젝트가 저장소 하위 폴더에 있으면 각 step에 `working-directory:` 를 지정하세요(파일 주석 참고).

---

## 5. CD — AWS EC2 + RDS 배포 (개요)

가장 흔한 신입 친화 구성은 **EC2(서버) + RDS(관리형 MySQL)** 입니다.

```
[사용자] → (인터넷) → [EC2: 스프링부트 앱 실행] → [RDS: MySQL]
```

### 준비 체크리스트
1. **RDS(MySQL) 생성** — 퍼블릭 액세스 **끄기**(EC2에서만 접근). DB 계정/비번 준비.
2. **EC2(Ubuntu) 생성** — 프리티어 `t2.micro`/`t3.micro`. JDK 17 설치.
3. **보안 그룹(방화벽)** — 앱 포트(8080 또는 80)만 열기. SSH(22)는 **내 IP만**. RDS는 EC2 보안그룹에서만 허용.
4. **앱 실행 방식** — `java -jar app.jar` 를 **systemd 서비스**로 등록(재부팅·크래시 시 자동 재시작).
5. **자동 배포** — [샘플/github-workflows/deploy.yml](./샘플/github-workflows/deploy.yml) 템플릿으로 main push 시 jar 전송 + 재시작.

### GitHub Secrets 등록 (deploy.yml이 참조)
저장소 → Settings → Secrets and variables → Actions 에서:
`EC2_HOST`(서버 IP), `EC2_USER`(예: ubuntu), `EC2_SSH_KEY`(EC2 접속 개인키 내용)

> deploy.yml은 **그대로는 안 도는 템플릿**입니다. 위 준비를 마치고 값·시크릿을 채워야 동작합니다.

---

## 6. ⚠️ 보안 · 비용 주의 (실서비스 전에 반드시)

> ⚠️ **비밀정보를 코드/깃에 절대 커밋하지 마세요.** DB 비번·AWS 키·SSH 키는 **GitHub Secrets**나 서버 환경변수로.
> 실수로 커밋했다면 즉시 키를 폐기·재발급하세요(깃 히스토리에 영원히 남습니다).

> ⚠️ **보안 그룹을 `0.0.0.0/0`(전체 개방) 으로 열지 마세요.** 특히 SSH(22)·DB(3306)는 반드시 출처를 제한.
> DB(RDS)는 **퍼블릭 노출 금지**, EC2를 통해서만 접근.

> ⚠️ **비용**: 프리티어는 조건·기간 한도가 있습니다. 안 쓰는 EC2/RDS는 **중지 또는 삭제**. 예산 알림(Billing Alarm)을 걸어 두세요.

> ⚠️ **HTTPS**: 외부 공개 서비스는 HTTP가 아니라 **HTTPS** 로. (도메인 + 무료 인증서 Let's Encrypt, 또는 로드밸런서/CloudFront)

---

## 7. 이 모듈로 무엇을 증명하나 (포트폴리오 연결)

- "로컬에서만 돈다" → **"인터넷에서 접속되는 데모 URL이 있다"** 로 격상
- "빌드/배포를 손으로 한다" → **"push하면 자동 테스트·배포된다(CI/CD)"** 로 격상
- 아키텍처 그림 + "배포 수동 30분 → 자동 5분" 같은 **정량 개선**을 [캡스톤 README](../Capstone/)에 기록

---

## 🏢 실무(eGovFrame) 연결 포인트
공공/SI도 온프레미스에서 **클라우드(SaaS) 전환**이 진행 중이라, 컨테이너·CI/CD 이해의 가치가 커지고 있습니다.
전통적으로는 **WAR를 외장 톰캣/WAS에 배포**하지만, 원리(빌드 산출물 → 서버에 올려 실행 → 재시작)는 동일합니다.
(스프링부트 내장 톰캣 vs 외장 WAS 차이는 [참고자료: 웹서버·WAS 동작원리](../../99_참고자료/) 참고)
