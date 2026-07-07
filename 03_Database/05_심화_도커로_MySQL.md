# 05. (심화·선택) 도커로 MySQL 띄우기 — 진짜 서버형 DB 체험

> **이 문서에서 배우는 것**
> - Docker로 명령 한 줄 만에 MySQL 서버를 띄우는 방법
> - 02에서 개념으로 배운 권한(GRANT)을 직접 실습 — 계정을 만들고, 권한을 주고, 거부당해 보기
> - H2에서 작성한 JDBC 코드가 **상수 3줄만 바꾸면 MySQL에서 그대로 도는** 것 확인 (JDBC 표준의 힘)

이 문서는 **선택 심화**입니다. 01~04를 마쳤고 시간 여유가 있는 분만 진행하세요.
Docker Desktop 설치가 필요하므로, 설치가 어려운 환경이면 건너뛰어도 다음 학습(스프링)에 지장 없습니다.

지금까지 쓴 H2는 내 프로그램이 직접 파일을 여는 **임베디드** 방식이었습니다.
실무의 DB는 [02 문서](./02_접속과_권한.md)에서 배운 대로 **항상 켜져 있는 별도 서버**입니다.
오늘 그 "진짜 서버"를 내 PC에 띄워 봅니다.

---

## 1. Docker, 3줄 요약

- **Docker**는 프로그램을 "설치" 없이 **격리된 상자(컨테이너)로 실행**하는 도구입니다.
- MySQL을 직접 설치하면 이것저것 설정이 많지만, Docker로는 **명령 한 줄**이면 끝나고, 지울 때도 흔적 없이 사라집니다.
- 그래서 실무에서 개발용 DB·테스트 환경을 띄울 때 사실상 표준으로 쓰입니다.

준비: [Docker Desktop](https://www.docker.com/products/docker-desktop/)을 설치하고 실행해 두세요.
(회사 PC는 설치 정책이 있을 수 있으니 사수에게 먼저 확인!)
터미널에서 `docker --version`이 출력되면 준비 완료입니다.

---

## 2. MySQL 서버 띄우기 — 명령 한 줄

```powershell
docker run --name intern-mysql -e MYSQL_ROOT_PASSWORD=root1234 -e MYSQL_DATABASE=introdb -p 3306:3306 -d mysql:8.4
```

옵션을 해부해 봅시다. **02에서 배운 접속 요소들이 여기서 결정됩니다.**

| 옵션 | 의미 |
|---|---|
| `--name intern-mysql` | 컨테이너 이름 (이후 명령에서 이 이름으로 지칭) |
| `-e MYSQL_ROOT_PASSWORD=root1234` | 관리자(root) 비밀번호 설정 |
| `-e MYSQL_DATABASE=introdb` | 시작하면서 introdb 데이터베이스를 만들어 둠 |
| `-p 3306:3306` | 내 PC의 3306 포트 → 컨테이너의 3306 포트 연결 |
| `-d mysql:8.4` | mysql 8.4 버전 이미지를 백그라운드(-d)로 실행 |

처음 한 번은 이미지를 내려받느라 시간이 걸립니다. 준비 확인:

```powershell
docker exec intern-mysql mysqladmin ping -uroot -proot1234
# mysqld is alive 가 나오면 준비 완료
```

> ⚠️ **`port is already allocated` 에러가 나면** — 내 PC에 이미 MySQL이 설치되어 3306을 쓰고 있는 것입니다.
> `-p 3307:3306`으로 바꿔 띄우고, 이후 JDBC URL도 `localhost:3307`로 쓰면 됩니다.
> (접속 5요소 중 "포트"가 왜 필요한지 몸으로 배우는 순간입니다!)

---

## 3. 권한 실습 — 02에서 배운 것을 진짜로

지금부터가 이 문서의 핵심입니다. **관리자(root)와 애플리케이션 계정(intern)을 분리**해 봅니다.

### 3-1. root로 접속해서 계정 만들기

컨테이너 안의 MySQL CLI에 root로 접속합니다.

```powershell
docker exec -it intern-mysql mysql --default-character-set=utf8mb4 -uroot -proot1234
```

(`--default-character-set=utf8mb4`는 CLI에서 한글이 `?`로 깨지지 않게 하는 옵션입니다.)

`mysql>` 프롬프트가 뜨면, 계정을 만들고 **필요한 권한만** 줍니다.

```sql
-- 애플리케이션용 계정 생성
CREATE USER 'intern'@'%' IDENTIFIED BY 'intern1234';

-- introdb 안에서 데이터 조작(DML)만 허용. 테이블 생성/삭제 권한은 안 줌!
GRANT SELECT, INSERT, UPDATE, DELETE ON introdb.* TO 'intern'@'%';

-- 확인
SHOW GRANTS FOR 'intern'@'%';
```

```text
GRANT USAGE ON *.* TO `intern`@`%`
GRANT SELECT, INSERT, UPDATE, DELETE ON `introdb`.* TO `intern`@`%`
```

- `'intern'@'%'` — intern이라는 사용자가 **어디서든(%)** 접속하는 것을 허용한다는 뜻입니다. 실무에서는 `%` 대신 특정 IP 대역으로 좁히기도 합니다.
- `introdb.*` — introdb 데이터베이스 안의 모든 테이블에 대해서만.

### 3-2. 테이블은 관리자가 만든다

구조 변경(DDL)은 관리자의 일입니다. root로 접속한 김에 테이블을 만듭니다.
**[03 문서](./03_SQL_기초.md)에서 H2에 만들었던 DDL과 완전히 동일합니다.**

```sql
USE introdb;

CREATE TABLE IF NOT EXISTS intro (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    VARCHAR(4000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

exit
```

### 3-3. 거부당해 보기 — 최소 권한 원칙의 체감

이번엔 **intern 계정으로** 접속해서, 하면 안 되는 일을 시도해 봅니다.

```powershell
docker exec -it intern-mysql mysql --default-character-set=utf8mb4 -uintern -pintern1234 introdb
```

```sql
DROP TABLE intro;
```

```text
ERROR 1142 (42000): DROP command denied to user 'intern'@'localhost' for table 'intro'
```

**거부됐습니다.** intern 계정으로는 실수로라도 테이블을 날릴 수 없습니다.
02에서 말한 "신입이 운영 DB에서 사고"가 왜 권한으로 막히는지, 이 에러 메시지가 그 답입니다.
물론 허용된 일(SELECT/INSERT/UPDATE/DELETE)은 잘 됩니다 — `SELECT * FROM intro;`로 확인해 보세요.

---

## 4. JDBC 스와프 — 상수 3줄만 바꿔 그대로 실행

이제 [04 문서](./04_JDBC_실습.md)의 자바 코드를 MySQL에 연결해 봅니다. 필요한 것은 딱 두 가지입니다.

**① MySQL 드라이버** — H2 드라이버 대신 MySQL 드라이버(jar)가 필요합니다.
[샘플/jdbc/lib](./샘플/jdbc/lib/)에 `mysql-connector-j-8.4.0.jar`가 준비되어 있습니다.
(직접 받는다면 [Maven Central](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar))

**② 접속 상수 3줄 수정** — `Step03_Insert.java`, `Step04_Select.java` 상단의 상수만 바꿉니다.

```java
static final String URL = "jdbc:mysql://localhost:3306/introdb";  // h2 → mysql
static final String USER = "intern";                              // sa → intern
static final String PASSWORD = "intern1234";                      // "" → intern1234
```

컴파일하고, classpath의 jar만 MySQL 드라이버로 바꿔 실행합니다.

```powershell
javac -encoding UTF-8 -d out src/Step03_Insert.java src/Step04_Select.java
java -cp "out;lib/mysql-connector-j-8.4.0.jar" Step03_Insert
java -cp "out;lib/mysql-connector-j-8.4.0.jar" Step04_Select
```

```text
저장 완료! (1건 반영)
== 자기소개서 목록 ==
[1] 성장하는 개발자 김철수입니다 - 김철수 (2026-07-07 02:03:14.0)
```

**SQL도, 자바 로직도 한 글자도 바꾸지 않았습니다.** 접속 정보와 드라이버만 갈아 끼웠는데
파일 DB(H2)에서 서버 DB(MySQL)로 옮겨졌습니다 — 이것이 **JDBC가 "표준"이라는 말의 의미**입니다.
스프링에서 H2로 개발하다가 운영에서 MySQL/Oracle로 바꾸는 일이 가능한 것도 같은 원리입니다.

> 🕐 작성시각이 9시간 전으로 보인다면 정상입니다. 컨테이너의 시간대가 UTC(세계 표준시)라서 그렇습니다.
> "서버의 시간대" 문제는 실무에서도 단골 이슈인데, 오늘은 "이런 게 있구나"까지만 알아두세요.

---

## 5. 뒷정리

실습이 끝나면 컨테이너를 정리합니다.

```powershell
docker stop intern-mysql     # 정지 (데이터 유지, docker start intern-mysql로 재개)
docker rm -f intern-mysql    # 완전 삭제
```

> ⚠️ `docker rm`을 하면 **저장했던 데이터도 함께 사라집니다.** 컨테이너는 기본적으로 일회용이기 때문입니다.
> 데이터를 컨테이너 밖에 보존하는 방법(볼륨, `-v` 옵션)이 있지만, 그건 도커를 본격적으로 배울 때의 주제입니다.

---

## 정리

- Docker로 MySQL 서버를 한 줄에 띄웠다 지울 수 있습니다 — 개발용 DB의 표준적인 사용 방식.
- 계정과 권한을 직접 만들어 보고, **권한 없는 작업이 거부되는 것**(ERROR 1142)을 체험했습니다.
- JDBC 코드는 드라이버와 접속 상수만 바꾸면 DB 제품이 바뀌어도 그대로 동작합니다.

여기까지 왔다면 데이터베이스 모듈 완주입니다. 🎉
이제 [스프링부트 교육](../Spring/SpringBoot/README.md)에서 `repository.save()` 한 줄을 만났을 때,
그 아래에서 벌어지는 모든 일(접속, SQL, 바인딩, 트랜잭션, 권한)을 아는 사람이 되었습니다.

---

⬅️ 이전: [04. JDBC 실습](./04_JDBC_실습.md) | 처음으로: [README](./README.md)
