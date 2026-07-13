# 02. MySQL로 전환하기 — 코드는 그대로, 설정만 갈아끼우기

> **이 문서에서 배우는 것**
> - **프로필(profile)**: 같은 앱을 "개발용 설정 / 운영용 설정"으로 갈아끼우는 스프링의 방법
> - 01에서 만든 앱을 **자바 코드 수정 0줄**로 도커 MySQL에 연결
> - `ddl-auto=validate` — 운영 DB에서 테이블을 함부로 만들지 않는 이유 (최소 권한 원칙의 연장)

[DB 교육 05](../../03_Database/05_심화_도커로_MySQL.md)에서 JDBC 코드의 **상수 3줄**을 바꿔
H2 → MySQL로 갈아탔던 것을 기억하나요? 스프링부트에서는 그 3줄이 설정 파일에 있으므로,
**설정 파일 하나를 추가**하는 것으로 같은 일을 합니다. 재컴파일도 필요 없습니다.

이 문서는 [DB 교육 05](../../03_Database/05_심화_도커로_MySQL.md)를 마쳤다고 가정합니다.
(Docker Desktop, intern-mysql 컨테이너, intern 계정)

---

## 1. MySQL 서버 준비 — 05에서 만든 컨테이너 다시 켜기

05 실습 때 만든 컨테이너가 남아 있다면 다시 켜기만 하면 됩니다.

```powershell
docker start intern-mysql
docker exec intern-mysql mysqladmin ping -uroot -proot1234
# mysqld is alive 가 나오면 준비 완료
```

컨테이너를 지웠다면(`docker rm`) 05 문서의 2~3절을 다시 실행하세요. 요약하면:

```powershell
# ① 서버 띄우기 (3306이 이미 사용 중이면 -p 3307:3306 으로)
docker run --name intern-mysql -e MYSQL_ROOT_PASSWORD=root1234 -e MYSQL_DATABASE=introdb -p 3306:3306 -d mysql:8.4
```

```sql
-- ② root로 접속해서: 애플리케이션 계정 만들기 (DML만 허용!)
CREATE USER 'intern'@'%' IDENTIFIED BY 'intern1234';
GRANT SELECT, INSERT, UPDATE, DELETE ON introdb.* TO 'intern'@'%';

-- ③ 테이블은 관리자(root)가 만든다 — 05 문서 3-2절의 DDL 그대로
USE introdb;
CREATE TABLE IF NOT EXISTS intro (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    VARCHAR(4000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

> 📌 이 테이블 구조는 01에서 만든 `Intro` 엔티티와 **정확히 일치**합니다.
> (JPA가 `createdAt`을 `created_at`으로 바꿔 읽는 규칙까지 포함해서요.)
> 우연이 아니라, 그렇게 일치하도록 설계했기 때문에 전환이 가능한 것입니다.

---

## 2. 프로필 — 설정을 "세트"로 갈아끼우는 스위치

스프링부트는 `application-이름.properties` 파일을 만들어 두면,
실행할 때 `--spring.profiles.active=이름` 스위치로 그 설정 세트를 **덧입힐** 수 있습니다.

```text
application.properties          ← 항상 읽는 기본 설정 (H2)
application-mysql.properties    ← mysql 프로필을 켰을 때만 "위에 덮어쓰는" 설정
```

핵심은 **덮어쓴다**는 것입니다. mysql 파일에는 기본 설정과 *달라지는 항목만* 적으면 되고,
안 적은 항목(`show-sql` 등)은 기본값이 그대로 유지됩니다.

`src/main/resources/application-mysql.properties`를 새로 만듭니다.

```properties
# ===== MySQL 접속 정보 (DB 교육 02에서 배운 접속 5요소!) =====
# 도커를 3307 포트로 띄웠다면 localhost:3307 로 바꾸세요.
spring.datasource.url=jdbc:mysql://localhost:3306/introdb
spring.datasource.username=intern
spring.datasource.password=intern1234

# ===== JPA =====
# validate: 테이블을 만들거나 고치지 않고, 엔티티와 테이블이 맞는지 "검사만" 합니다.
spring.jpa.hibernate.ddl-auto=validate
```

이게 전부입니다. **자바 코드는 한 글자도 바꾸지 않습니다.**

> ⚠️ **드라이버는 왜 안 적나요?** — 스프링부트가 URL의 접두사(`jdbc:mysql://`)를 보고
> MySQL 드라이버를 알아서 고릅니다. 오히려 기본 설정 파일에
> `spring.datasource.driver-class-name=org.h2.Driver`를 적어 두면 **mysql 프로필에도 그 값이
> 상속되어** 다음 에러로 기동이 실패합니다. (실제로 겪기 쉬운 함정입니다)
>
> ```text
> Unable to determine Dialect without JDBC metadata (please set
> 'jakarta.persistence.jdbc.url' for common cases or 'hibernate.dialect' ...)
> ```
>
> 이 에러의 진짜 원인은 대부분 "DB 접속 실패"입니다. 드라이버/URL/서버 상태부터 확인하세요.

### 왜 `update`가 아니라 `validate`인가 — 권한과 연결됩니다

| | H2 (개발) | MySQL (운영 흉내) |
|---|---|---|
| 접속 계정 | `sa` (전능) | `intern` — **DML만 가능, DDL 불가** |
| 테이블 생성 | JPA가 자동 (`update`) | **관리자(root)가 DDL로** — 05 문서 3-2절 |
| ddl-auto | `update` | **`validate`** (검사만) |

intern 계정에는 05에서 일부러 CREATE/DROP 권한을 주지 않았습니다(최소 권한 원칙).
애초에 테이블을 만들 수 없는 계정이니, JPA에게도 "만들려 하지 말고 **맞는지 검사만 해라**"라고
지시하는 것이 `validate`입니다. 실무 운영 DB도 똑같습니다 — 스키마 변경은 DBA의 승인 절차를
거치고, 애플리케이션은 검증만 합니다.

> 🏢 **eGovFrame 연결 포인트** — 실무 프로젝트도 개발/운영 DB 설정을 파일로 분리합니다.
> eGovFrame에서는 `globals.properties`와 `context-datasource.xml`이 그 역할을 합니다.
> "코드는 그대로, 설정만 환경별로"라는 원리는 완전히 같습니다.

---

## 3. 실행 — 프로필 스위치 켜기

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=mysql'
```

시작 로그 첫 부분에서 프로필이 켜졌는지 꼭 확인하세요. (아래는 실제 실행 출력)

```text
c.example.introjpa.IntroJpaApplication   : The following 1 profile is active: "mysql"
...
c.example.introjpa.IntroJpaApplication   : Started IntroJpaApplication in 3.967 seconds
```

`Started ...`가 나왔다는 것은 `validate` 검사를 통과했다는 뜻이기도 합니다
(엔티티와 root가 만든 테이블이 일치!).

> 💡 도커를 **3307 포트**로 띄운 경우, 설정 파일을 고치지 않고 이렇게 덮어쓸 수도 있습니다.
>
> ```powershell
> .\gradlew.bat bootRun --args='--spring.profiles.active=mysql --spring.datasource.url=jdbc:mysql://localhost:3307/introdb'
> ```
>
> 명령줄 인자가 설정 파일보다 우선한다는 것도 함께 기억해 두세요.

### 눈으로 교차 확인

`http://localhost:8080`에서 자기소개서를 하나 등록한 뒤, **애플리케이션을 통하지 않고**
MySQL에 직접 물어봅니다. ([DB 교육 05](../../03_Database/05_심화_도커로_MySQL.md)에서 배운 CLI)

```powershell
docker exec -it intern-mysql mysql --default-character-set=utf8mb4 -uintern -pintern1234 introdb
```

```sql
SELECT id, name, title, created_at FROM intro;
```

```text
+----+--------+--------------------------------+---------------------+
| id | name   | title                          | created_at          |
+----+--------+--------------------------------+---------------------+
|  1 | 김철수 | MySQL에 저장된 자기소개서      | 2026-07-13 14:19:52 |
+----+--------+--------------------------------+---------------------+
```

브라우저에서 등록한 글이 진짜 MySQL 서버에 들어 있습니다.
수정·삭제 버튼도 눌러 보고, 그때마다 이 쿼리로 다시 확인해 보세요.
H2 때와 **완전히 같은 화면, 완전히 같은 코드**가 다른 DB 위에서 돌고 있습니다.

---

## 4. 안 될 때 — 단골 에러 사전

| 증상 (콘솔 메시지) | 원인 | 해결 |
|---|---|---|
| `Communications link failure` | MySQL 서버가 꺼져 있거나 포트가 다름 | `docker start intern-mysql`, 3307로 띄웠다면 URL의 포트 확인 |
| `Access denied for user 'intern'@...` | 계정/비밀번호 오타, 계정 미생성 | 05 문서 3-1절의 `CREATE USER`/`GRANT` 재확인 |
| `Schema validation: missing table [intro]` | `validate`가 검사했는데 테이블이 없음 | root로 접속해 1절 ③의 DDL 실행 (테이블은 관리자가!) |
| `Unable to determine Dialect without JDBC metadata` | DB 접속 자체가 실패 (드라이버 상속 함정 포함) | 2절의 ⚠️ 박스 — 기본 설정에 `driver-class-name`이 있으면 삭제 |
| 기동은 되는데 데이터가 안 보임 | 프로필이 안 켜져서 **H2를 보고 있음** | 시작 로그에 `profile is active: "mysql"` 있는지 확인 |

---

## 정리 — 무엇이 바뀌었고, 무엇이 안 바뀌었나

| | 바뀐 것 | 안 바뀐 것 |
|---|---|---|
| 파일 | `application-mysql.properties` 추가 (접속 4줄 + validate) | **자바 코드, 템플릿 전부** |
| DB | H2 파일 → MySQL 서버 | 테이블 구조, 데이터를 다루는 SQL |
| 계정 | `sa` → `intern` (DML만) | |

- JDBC 때 "상수 3줄"이던 전환이, 스프링에서는 **프로필 파일 하나**가 되었습니다.
- 운영형 DB에서는 `ddl-auto=validate` + 최소 권한 계정 — 테이블은 관리자가 만듭니다.
- 접속이 안 되면 늘 순서대로: **서버 살아있나 → 포트 맞나 → 계정 맞나 → 테이블 있나.**

여기까지 완주했다면, 여러분은 이제 "개발 DB로 만들고 운영 DB로 배포하는" 실무의
기본 사이클을 한 바퀴 돌아본 것입니다. 🎉

---

⬅️ 이전: [01. JPA로 H2 CRUD 완성하기](./01_JPA로_H2_CRUD.md) | 처음으로: [README](./README.md)
