# 04. JDBC 실습 — 자바 코드로 데이터베이스 다루기

> **이 문서에서 배우는 것**
> - JDBC가 무엇이고, 자바 프로그램이 DB와 대화하는 표준 절차 (접속 → SQL 실행 → 결과 읽기 → 닫기)
> - Connection, PreparedStatement, ResultSet 3인방의 역할
> - SQL Injection이 무엇이고 `?` 바인딩이 왜 필수인지
> - 트랜잭션(commit/rollback) — "전부 성공 아니면 전부 취소"
> - 마지막에: 이 고생을 스프링이 어떻게 대신해 주는지

[03 문서](./03_SQL_기초.md)에서는 SQL을 콘솔에 **직접** 입력했습니다.
하지만 실제 서비스에서 사용자는 SQL을 모릅니다 — 화면의 "등록" 버튼을 누를 뿐이죠.
**사용자 대신 프로그램이 SQL을 실행하게 만드는 것**, 그 표준 방법이 JDBC(Java Database Connectivity)입니다.

완성 코드는 [샘플/jdbc](./샘플/jdbc/) 폴더에 단계별로 들어 있습니다. **직접 타이핑을 권장**하지만, 막히면 참고하세요.

---

## 0. 준비 — 프로젝트 구조와 실행 방법

이번 실습은 IDE 없이 **메모장 수준의 편집기 + 터미널**로도 가능합니다. (물론 IDE를 써도 됩니다.)

```text
샘플/jdbc/
├── lib/
│   └── h2-2.3.232.jar     # H2 드라이버 (01 문서에서 받은 그 파일)
├── src/
│   ├── Step01_Connect.java        # 1단계: 접속
│   ├── Step02_CreateTable.java    # 2단계: 테이블 생성
│   ├── Step03_Insert.java         # 3단계: 저장
│   ├── Step04_Select.java         # 4단계: 조회
│   ├── Step05_IntroApp.java       # 5단계: 완성판 콘솔 앱
│   └── Step06_Transaction.java    # 6단계: 트랜잭션
└── data/                  # 실행하면 생기는 DB 파일 (git에 안 올림)
```

컴파일과 실행 (PowerShell, `샘플/jdbc` 폴더에서):

```powershell
javac -encoding UTF-8 -d out src/Step01_Connect.java     # 컴파일 → out 폴더에 .class 생성
java -cp "out;lib/h2-2.3.232.jar" Step01_Connect         # 실행
```

`-cp`(classpath)는 "실행에 필요한 코드가 어디 있는지"의 목록입니다.
**내 코드(out)와 H2 드라이버(jar)를 세미콜론으로 나란히** 적었습니다 — 스프링에서 `build.gradle`에 의존성 한 줄을 쓰면
Gradle이 대신해 주던 일이 바로 이 classpath 관리입니다. (macOS/리눅스는 `;` 대신 `:`)

> 💡 **드라이버란?** JDBC는 "표준 규격"이고, 각 DB 회사가 그 규격에 맞춰 만든 부품이 드라이버입니다.
> H2 드라이버(h2-*.jar), MySQL 드라이버(mysql-connector-j-*.jar)처럼 DB마다 하나씩 있습니다.
> 콘센트 규격(JDBC)은 같고, 플러그(드라이버)만 DB마다 다른 셈입니다.

---

## 1. Step 1 — 접속: Connection

가장 작은 성공부터. **접속만** 해 봅니다. [`src/Step01_Connect.java`](./샘플/jdbc/src/Step01_Connect.java)

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Step01_Connect {

    // 접속 정보 3요소 (02 문서에서 배운 그것!)
    static final String URL = "jdbc:h2:./data/introdb";
    static final String USER = "sa";
    static final String PASSWORD = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("접속 성공!");
            System.out.println("DB 제품명: " + conn.getMetaData().getDatabaseProductName());

        } catch (SQLException e) {
            System.out.println("접속 실패: " + e.getMessage());
        }
    }
}
```

실행 결과:

```text
접속 성공!
DB 제품명: H2
```

읽는 법:

- **DriverManager.getConnection(URL, USER, PW)** — JDBC URL을 보고 알맞은 드라이버를 찾아 접속을 만들어 줍니다. H2 콘솔 로그인 화면에서 손으로 입력하던 세 칸을 코드로 넘긴 것뿐입니다.
- **Connection** — DB와 연결된 통로. 이후 모든 작업이 이 통로로 오갑니다.
- **try (...) { }** — *try-with-resources*. 괄호 안에서 만든 자원(접속)을 블록이 끝날 때 **자동으로 닫아** 줍니다. 접속을 닫지 않고 쌓이면 DB가 새 접속을 거부하는 장애로 이어집니다. **"연 것은 반드시 닫는다"** — JDBC의 제1 수칙이고, try-with-resources가 그걸 잊지 않게 해줍니다.
- **SQLException** — DB 작업 중 문제가 생기면 던져지는 예외. URL 오타, 서버 다운, 문법 오류가 전부 이걸로 옵니다. 메시지를 **읽는** 습관이 디버깅의 절반입니다.

---

## 2. Step 2 — 자바에서 SQL 실행하기

[`src/Step02_CreateTable.java`](./샘플/jdbc/src/Step02_CreateTable.java) — 03 문서에서 콘솔에 쳤던 DDL을 자바가 실행합니다.

```java
String sql = """
        CREATE TABLE IF NOT EXISTS intro (
            id         BIGINT AUTO_INCREMENT PRIMARY KEY,
            name       VARCHAR(50)  NOT NULL,
            title      VARCHAR(200) NOT NULL,
            content    VARCHAR(4000),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;

try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
     Statement stmt = conn.createStatement()) {

    stmt.execute(sql);
    System.out.println("intro 테이블 준비 완료!");
}
```

- `"""..."""`는 자바의 여러 줄 문자열(텍스트 블록)입니다. SQL을 그대로 담기 좋습니다.
- `IF NOT EXISTS` — 이미 테이블이 있으면 조용히 넘어갑니다. 여러 번 실행해도 안전.
- **Statement**는 SQL 심부름꾼입니다. 단, 다음 단계에서 보듯 **값이 들어가는 SQL에는 쓰면 안 됩니다.**

---

## 3. Step 3 — INSERT와 SQL Injection 이야기

[`src/Step03_Insert.java`](./샘플/jdbc/src/Step03_Insert.java)

```java
String sql = "INSERT INTO intro (name, title, content) VALUES (?, ?, ?)";

try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
     PreparedStatement ps = conn.prepareStatement(sql)) {

    ps.setString(1, "김철수");                        // 첫 번째 ?
    ps.setString(2, "성장하는 개발자 김철수입니다");     // 두 번째 ?
    ps.setString(3, "안녕하세요. JDBC로 저장한 첫 자기소개서입니다.");

    int rows = ps.executeUpdate();
    System.out.println("저장 완료! (" + rows + "건 반영)");
}
```

### 왜 문자열을 이어붙이지 않고 `?`를 쓰나요?

이렇게 쓰고 싶은 유혹이 듭니다:

```java
// ❌ 절대 금지
String sql = "INSERT INTO intro (name, title) VALUES ('" + name + "', '" + title + "')";
```

사용자가 이름 칸에 이런 값을 입력했다고 해 봅시다:

```text
철수'); DELETE FROM intro; --
```

이어붙인 SQL은 이렇게 됩니다:

```sql
INSERT INTO intro (name, title) VALUES ('철수'); DELETE FROM intro; --', '제목')
```

**INSERT가 끝나자마자 DELETE가 실행되어 테이블이 통째로 비워집니다.**
사용자 입력이 데이터가 아니라 **SQL 명령으로 해석되어 버리는 것** — 이것이 **SQL Injection**이며,
수십 년째 웹 해킹 순위 최상위권에 있는 고전적이고 여전히 현역인 공격입니다.

`PreparedStatement`의 `?` 바인딩은 입력값을 **어떤 경우에도 "그냥 글자"로만** 취급합니다.
위의 악성 입력도 그대로 이름 칸에 저장될 뿐, 명령으로 실행되지 않습니다.

> 🔒 **규칙으로 외우세요: 값이 들어가는 SQL은 무조건 PreparedStatement + `?`.**
> 문자열 덧셈으로 SQL을 만드는 코드는 코드리뷰에서 무조건 반려됩니다.
> (참고로 MyBatis의 `#{}`, JPA의 파라미터 바인딩도 내부적으로는 전부 이 방식입니다.)

메서드 구분도 기억해 두세요: **조회는 `executeQuery()`, 변경(INSERT/UPDATE/DELETE)은 `executeUpdate()`** 입니다.

---

## 4. Step 4 — SELECT와 ResultSet

[`src/Step04_Select.java`](./샘플/jdbc/src/Step04_Select.java)

```java
String sql = "SELECT id, name, title, created_at FROM intro ORDER BY id DESC";

try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {

    while (rs.next()) {   // 다음 행으로 이동. 행이 있으면 true
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String title = rs.getString("title");

        System.out.printf("[%d] %s - %s%n", id, title, name);
    }
}
```

실행 결과:

```text
== 자기소개서 목록 ==
[1] 성장하는 개발자 김철수입니다 - 김철수 (2026-07-07 10:55:26.36)
```

**ResultSet**은 SELECT 결과 표 위를 한 행씩 이동하는 커서입니다.

- `rs.next()` — 커서를 다음 행으로. 처음엔 표의 "위쪽 바깥"에 있어서 **첫 next()가 1행으로 이동**입니다.
- `rs.getString("name")` — 현재 행에서 열 이름으로 값을 꺼냅니다. 타입에 맞춰 `getLong`, `getTimestamp` 등을 씁니다.
- `while (rs.next()) { ... }` — 표 전체 순회의 표준 관용구.

여기까지 왔으면 **콘솔 SQL로 하던 모든 일을 자바로 할 수 있게 된 것**입니다.

---

## 5. Step 5 — 완성판: 자기소개서 콘솔 앱

[`src/Step05_IntroApp.java`](./샘플/jdbc/src/Step05_IntroApp.java)는 Step 1~4를 하나로 합친 완성판입니다.

```text
==== 자기소개서 관리 ====
1. 목록  2. 등록  3. 상세 보기  0. 종료
선택> 2
이름: 이영희
제목: 꼼꼼함이 무기인 이영희입니다
자기소개(한 줄): 안녕하세요!
등록 완료!
```

코드에서 눈여겨볼 점 두 가지:

1. **목록/등록/상세가 각각 메서드로 분리**되어 있습니다. "메뉴를 보여주는 코드"와 "DB를 다루는 코드"를 섞지 않는 것 —
   스프링에서 배울 Controller-Service-Repository 분리의 축소판입니다.
2. 상세 보기에서 **없는 번호(999)를 입력해 보세요.** "해당 번호의 자기소개서가 없습니다"가 나옵니다.
   `rs.next()`가 false를 반환하는 경우를 처리한 것입니다 — 조회 결과가 없는 경우의 처리는 실무에서 언제나 필요합니다.

> ✅ **직접 해보기**: 메뉴 4번으로 "삭제" 기능을 추가해 보세요.
> 필요한 것은 이미 다 배웠습니다 — `DELETE FROM intro WHERE id = ?` + `executeUpdate()`.

---

## 6. Step 6 — 트랜잭션: 전부 성공 아니면 전부 취소

계좌이체를 생각해 봅시다. "A 계좌 출금"과 "B 계좌 입금"은 SQL 두 개지만, **하나만 성공하면 절대 안 됩니다.**
이렇게 여러 작업을 **한 묶음**으로 다루는 것이 **트랜잭션(Transaction)** 입니다.

[`src/Step06_Transaction.java`](./샘플/jdbc/src/Step06_Transaction.java)는 자기소개서 2건을 한 묶음으로 저장하되,
2건째에 일부러 오류(NOT NULL 위반)를 심어 두었습니다.

```java
try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

    conn.setAutoCommit(false);   // 지금부터는 commit 전까지 "임시 저장" 상태

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        // 1건째: 정상 INSERT → 성공 (아직 임시)
        // 2건째: title에 null → SQLException!
        ...
        conn.commit();           // 여기 도달해야 진짜 저장
    } catch (SQLException e) {
        conn.rollback();         // 묶음 전체 취소 — 1건째도 함께 사라짐
        System.out.println("오류 발생 → 롤백했습니다: " + e.getMessage());
    }
}
```

실행 결과:

```text
1건째 INSERT 성공 (아직 임시 상태)
오류 발생 → 롤백했습니다: NULL not allowed for column "TITLE"; ...
Step04_Select를 실행해 보세요. 1건째도 저장되지 않았습니다!
```

실제로 Step04를 다시 실행하면 **1건째도 없습니다.** 이것이 롤백입니다.

- JDBC는 기본이 **자동커밋**(SQL 하나마다 즉시 확정)이라, 묶음이 필요할 때 `setAutoCommit(false)`로 수동 모드로 바꿉니다.
- 03 문서의 "WHERE 빼먹은 UPDATE" 같은 사고도, 트랜잭션 안이었다면 rollback으로 되돌릴 수 있습니다.
  (commit해 버린 뒤에는 못 돌립니다 — 그래서 습관이 중요합니다.)

---

## 7. 그런데... 뭔가 이상하지 않나요?

Step 5 코드를 다시 보세요. 목록/등록/상세 메서드마다 이 코드가 **복사-붙여넣기**처럼 반복됩니다.

```java
try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ...
} catch (SQLException e) { ... }
```

실무 프로그램에는 이런 메서드가 수백 개 필요합니다. 매번 접속을 열고, 바인딩하고, 결과를 객체로 옮겨 담고, 닫고, 예외를 처리하고...
**"필요한 건 SQL과 결과 딱 두 가지인데, 주변 절차가 90%"** — 이것이 JDBC의 한계이자, 다음 도구들이 등장한 이유입니다.

| 도구 | 반복 작업을 줄이는 방식 | 어디서 쓰나 |
|---|---|---|
| **JDBC** (지금 배운 것) | 없음 — 전부 직접 | 모든 것의 기반 |
| **MyBatis** | SQL은 XML에 따로 쓰고, 접속/바인딩/결과 매핑은 자동 | **회사 실무(eGovFrame)** |
| **JPA (Spring Data JPA)** | SQL 자체도 자동 생성 (`save()` 한 줄 → INSERT) | 스프링부트 교육, 최근 서비스 개발 |

그리고 스프링부트에서는 접속 정보조차 코드에서 사라집니다. 여러분이 상수로 박아 둔 URL/USER/PASSWORD가:

```properties
# ../Spring/SpringBoot/ 실습의 application.properties
spring.datasource.url=jdbc:h2:./data/introdb
spring.datasource.username=sa
spring.datasource.password=
```

**설정 파일 세 줄**이 되고, 접속 관리(커넥션 풀)·트랜잭션(commit/rollback)까지 스프링이 대신합니다.
스프링 교육에서 `repository.save(intro)` 한 줄을 만나면 떠올려 주세요 —
**그 한 줄 뒤에서 오늘 여러분이 직접 했던 모든 일이 벌어지고 있다**는 것을요.

---

## 정리

- JDBC 작업 절차: **접속(Connection) → SQL 준비(PreparedStatement) → 실행(executeQuery/Update) → 결과(ResultSet) → 닫기(try-with-resources)**
- 값이 들어가는 SQL은 **무조건 `?` 바인딩** — SQL Injection은 실존하는 위협입니다.
- 여러 SQL을 한 묶음으로: **setAutoCommit(false) → commit / rollback**
- JDBC의 반복 코드를 줄이려고 MyBatis(회사 실무)와 JPA(스프링 교육)가 존재합니다.

다음 문서는 **선택 심화**입니다. Docker로 진짜 서버형 DB(MySQL)를 띄워, 02에서 배운 권한을 실습하고
"드라이버와 URL만 바꾸면 같은 코드가 그대로 도는" JDBC 표준의 힘을 확인합니다.

---

⬅️ 이전: [03. SQL 기초](./03_SQL_기초.md) | 다음: [05. (심화) 도커로 MySQL](./05_심화_도커로_MySQL.md) ➡️
