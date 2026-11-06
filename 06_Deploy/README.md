# 06. Deploy — 스프링부트 앱을 Docker로 빌드하고 실행하기

[스프링부트 교육](../Spring/SpringBoot/README.md)에서 만든 **자기소개서 서비스(intro)** 를 Docker 컨테이너로 실행합니다.
"내 PC에서만 도는 앱"을 **"어디서든 똑같이 도는 이미지"** 로 바꾸는 것이 이 모듈의 목표입니다.

```
STEP 1  [ 앱 + H2 ]                       컨테이너 1개      docker build / docker run
           │  DB를 앱 밖으로 분리
           ▼
STEP 2  [ 앱 ] ──intro-net──▶ [ MySQL ]    컨테이너 2개      이미지 2개 빌드 + 네트워크 + run 2번
           │  손으로 친 명령을 파일 하나로
           ▼
STEP 3  docker-compose.yml                 컨테이너 2개      docker compose up 한 줄
```

> 💡 **왜 배우나?** 신입 채용공고에서 Docker는 대표적인 **우대 사항**입니다.
> 무엇보다 "제 PC에서는 되는데요?" 문제를 없애 주고, 클라우드 배포(AWS 등)의 출발점이 됩니다.

---

## 학습 순서

| 순서 | 문서 | 핵심 내용 | 예상 소요 |
|---|---|---|---|
| 1 | [STEP 1 — H2 앱 하나를 컨테이너로](./01_STEP1_H2_단일컨테이너.md) | Dockerfile 작성, `docker build`/`run`, 포트 매핑, 볼륨, 멀티스테이지 | 1~1.5시간 |
| 2 | [STEP 2 — MySQL로 바꾸고 컨테이너 2개 통신시키기](./02_STEP2_MySQL_컨테이너간_통신.md) | 설정 외부화(환경변수), DB 이미지 빌드, **사용자 정의 네트워크**, 컨테이너 간 통신 | 1.5~2시간 |
| 3 | [STEP 3 — docker compose로 한 번에 올리기](./03_STEP3_docker_compose.md) | `docker-compose.yml`, `.env`, healthcheck, `depends_on` | 1시간 |

**반드시 순서대로** 진행하세요. STEP 2·3은 앞 단계에서 손으로 해 본 일을 "자동화"하는 구조라, 건너뛰면 compose가 무엇을 대신해 주는지 보이지 않습니다.

---

## 시작 전 준비물

| 준비물 | 확인 방법 | 비고 |
|---|---|---|
| **Docker Desktop** | `docker version` 에 Client와 **Server**가 모두 나와야 함 | Server가 안 나오면 Docker Desktop 실행 |
| **Docker Compose v2** | `docker compose version` | Docker Desktop에 포함. `docker-compose`(하이픈)가 아니라 `docker compose`(띄어쓰기) |
| **JDK 17** | `java -version` | STEP 1의 jar 빌드에만 필요 |
| **선수 학습** | [스프링부트 샘플 intro](../Spring/SpringBoot/샘플/intro/)를 `bootRun`으로 실행해 본 경험 | 앱 구조를 알아야 컨테이너 문제를 구분할 수 있음 |
| **개념 (권장)** | [컨테이너·Docker·쿠버네티스 입문 PDF](../99_참고자료/04_배포_인프라/컨테이너_Docker_쿠버네티스_입문_인턴초급개발자.pdf) | 이미지/컨테이너 개념이 처음이면 먼저 읽기 |

> ⚠️ **8080 포트를 비워 두세요.** IDE에서 `bootRun` 중인 앱이 있으면 컨테이너가 8080을 못 씁니다.
> 명령어 예시는 **Windows PowerShell 기준**입니다. macOS/리눅스는 `.\gradlew.bat` → `./gradlew`, 줄 끝 `` ` `` → `\` 로 바꾸세요.

---

## 꼭 알아야 할 용어 5개

| 용어 | 한 줄 설명 | 비유 |
|---|---|---|
| **Dockerfile** | 이미지를 만드는 **설계도**(텍스트 파일) | 요리 레시피 |
| **이미지(Image)** | 앱 + 실행 환경(자바 등)을 묶은 **읽기 전용 꾸러미** | 밀키트 |
| **컨테이너(Container)** | 이미지를 실행한 **프로세스**. 하나의 이미지로 여러 개 띄울 수 있음 | 밀키트로 만든 요리 |
| **네트워크(Network)** | 컨테이너끼리 **이름으로** 통신하게 해 주는 가상 네트워크 | 사내 내선 전화망 |
| **볼륨(Volume)** | 컨테이너가 지워져도 남는 **데이터 저장소** | 외장 하드 |

**가장 중요한 사실 하나:** 컨테이너는 **지우면 안의 파일도 같이 사라집니다.** DB 데이터처럼 남겨야 하는 것은 볼륨에 둡니다. (STEP 1에서 직접 확인합니다)

---

## 폴더 구조

```
06_Deploy/
├─ README.md                          ← 지금 보는 문서
├─ 01_STEP1_H2_단일컨테이너.md
├─ 02_STEP2_MySQL_컨테이너간_통신.md
├─ 03_STEP3_docker_compose.md
└─ 샘플/
   ├─ step1-h2/                       ← 원본 intro 프로젝트 + Dockerfile (DB 설정은 원본과 동일)
   │  ├─ Dockerfile                   ← 기본: 내 PC에서 만든 jar를 복사
   │  ├─ Dockerfile.multistage        ← 심화: jar 빌드까지 Docker 안에서
   │  └─ .dockerignore
   ├─ step2-mysql/
   │  ├─ app/                         ← intro 프로젝트를 MySQL용으로 수정 + Dockerfile(멀티스테이지)
   │  └─ db/                          ← MySQL 이미지용 Dockerfile + 설정(conf/intro.cnf)
   └─ step3-compose/
      ├─ docker-compose.yml           ← step2-mysql 의 app, db 를 그대로 가져다 씀
      ├─ .env.example                 ← 비밀번호 예시 (복사해서 .env 로 사용)
      └─ .gitignore                   ← .env 를 Git 에서 제외
```

---

## 세 단계 한눈에 비교

| 구분 | STEP 1 | STEP 2 | STEP 3 |
|---|---|---|---|
| DB | H2 (앱 안에 파일로) | MySQL (별도 컨테이너) | MySQL (별도 컨테이너) |
| 컨테이너 수 | 1 | 2 | 2 |
| 앱 코드 변경 | **없음** | 드라이버·접속 설정 변경 | **없음** (STEP 2 그대로) |
| 앱이 DB를 찾는 주소 | `./data/introdb` (파일) | `intro-db` (컨테이너 이름) | `db` (서비스 이름) |
| 실행 명령 | `docker build` + `docker run` | 빌드 2번 + 네트워크 + `run` 2번 | `docker compose up` |
| 데이터 보존 | 볼륨 `-v` (선택) | 볼륨 `-v` | compose `volumes:` |
| 배우는 것 | 이미지·컨테이너·포트·볼륨 | 설정 외부화·네트워크·컨테이너 간 통신 | 선언형 실행·healthcheck·`.env` |

---

## 자주 쓰는 명령 모음

```powershell
docker build -t 이름:태그 .            # 현재 폴더의 Dockerfile 로 이미지 빌드
docker images                          # 이미지 목록
docker run -d --name 이름 -p 8080:8080 이미지   # 백그라운드(-d)로 컨테이너 실행
docker ps        /  docker ps -a       # 실행 중인 / 모든 컨테이너 목록
docker logs -f 컨테이너                # 로그 실시간 보기 (Ctrl+C 로 빠져나옴)
docker exec -it 컨테이너 bash          # 컨테이너 안에 들어가기 (exit 로 나옴)
docker stop 컨테이너  /  docker rm 컨테이너     # 정지 / 삭제
docker volume ls  /  docker network ls          # 볼륨 / 네트워크 목록

docker compose up -d --build           # (STEP 3) 빌드 + 전체 실행
docker compose ps  /  docker compose logs -f app
docker compose down   /  docker compose down -v # 종료 / 종료 + 볼륨 삭제
```

---

## 실습을 모두 마친 뒤 청소

```powershell
docker rm -f intro-h2 intro-app intro-db           # STEP 1·2 컨테이너 (없으면 에러 메시지만 나옴)
docker network rm intro-net
docker volume rm intro-h2-data intro-mysql-data
cd 샘플\step3-compose; docker compose down -v      # STEP 3
docker rmi intro-h2:1.0 intro-h2:multi intro-app:1.0 intro-db:1.0   # 이미지까지 지우려면
```

---

## 🏢 실무(eGovFrame) 연결 포인트

공공·SI 현장은 전통적으로 **WAR 파일을 외장 톰캣/WAS에 배포**합니다. 컨테이너는 이 과정을 바꿔 놓습니다.

| 전통 방식 | 컨테이너 방식 |
|---|---|
| 서버마다 JDK·톰캣 버전을 손으로 맞춤 | 실행 환경이 이미지에 들어 있어 **어디서든 동일** |
| 운영 서버에 접속해 설정 파일 수정 | 이미지는 그대로, **환경변수로 설정 주입** (STEP 2에서 실습) |
| 배포 절차 문서를 보고 사람이 수행 | `Dockerfile`·`docker-compose.yml` **자체가 절차서** |

공공 부문도 클라우드 전환이 진행 중이라 컨테이너를 이해하면 유리합니다. 다만 현장마다 환경이 다르니, 투입 전에 해당 프로젝트의 배포 방식을 꼭 확인하세요.
(내장 톰캣과 외장 WAS의 차이 → [웹서버·WAS 동작원리 PDF](../99_참고자료/02_웹_백엔드/웹서버_WAS_동작원리_인턴초급개발자.pdf))

---

## 다음 단계

- [Spring/Deploy](../Spring/Deploy/README.md) — GitHub Actions로 **자동 테스트(CI)·자동 배포(CD)**, AWS EC2 + RDS 배포 개요
- [Dockerfile·docker-compose 실습 PDF](../99_참고자료/04_배포_인프라/Dockerfile_docker-compose_스프링부트_인턴초급개발자.pdf) — Dockerfile 지시어와 compose 키 총정리표

---

> ✅ **검증 기록 (2026-11-06)**: Windows 11 + Docker 29.5.2 + Compose v5.1.3 환경에서 세 단계의 모든 명령을 실제로 실행해 확인했습니다.
> 한글·이모지 저장, 작성 시각(KST) 표시, 볼륨 데이터 보존, compose healthcheck 순서 제어까지 확인했습니다.
> 다른 버전에서는 출력 메시지가 조금 다를 수 있습니다.
