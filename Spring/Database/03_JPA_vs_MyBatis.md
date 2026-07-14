# 03. JPA vs MyBatis — 같은 앱을 MyBatis로 다시 만들어 보기

> **이 문서에서 배우는 것**
> - JPA와 MyBatis의 근본 차이: **"SQL을 누가 쓰는가"**
> - 스프링부트에서 MyBatis를 설정하는 방법 (의존성 → 설정 → 매퍼)
> - 01에서 만든 자기소개서 앱을 **리포지토리 계층만 갈아끼워** MyBatis로 재구현
> - `#{}` vs `${}` — 실무에서 절대 어기면 안 되는 보안 규칙

이 문서가 중요한 이유가 있습니다. **회사 실무(eGovFrame)는 JPA가 아니라 MyBatis를 씁니다.**
01~02에서 JPA로 익힌 구조가 MyBatis에서 어떻게 대응되는지 알고 나면,
입사 후 실무 코드를 열었을 때 "아, 이건 그거구나"가 됩니다.

완성본은 [샘플/intro-mybatis](./샘플/intro-mybatis/)에 있습니다 — **화면도, URL도, 기능도
intro-jpa와 완전히 동일**하고, DB에 접근하는 방법만 다릅니다.

---

## 1. 근본 차이 — "SQL을 누가 쓰는가"

여러분은 이미 세 가지 방식으로 DB에 접근해 봤습니다. 한 줄로 세우면 이렇습니다.

```text
        SQL 작성   값 바인딩   결과→객체 변환   접속/자원 관리
JDBC      나         나           나              나          ← DB 교육 04
MyBatis   나        자동          자동            자동         ← 오늘
JPA      자동       자동          자동            자동         ← 01~02
```

- **JPA**는 "객체를 보고 SQL을 만들어 주는" 도구입니다. `save()`만 부르면 INSERT가 나갑니다.
- **MyBatis**는 "내가 쓴 SQL을 객체에 연결해 주는" 도구입니다. SQL은 100% 내 손으로 쓰되,
  JDBC에서 지긋지긋했던 바인딩·결과 매핑·자원 정리를 대신해 줍니다.
- 즉 MyBatis는 **JDBC와 JPA의 중간**에 있습니다. SQL 주도권은 개발자에게, 반복 작업은 프레임워크에게.

### 한눈 비교표

| 항목 | JPA (Spring Data JPA) | MyBatis |
|---|---|---|
| SQL 작성 | 프레임워크가 자동 생성 | **개발자가 직접** (매퍼 XML) |
| 기본 CRUD | `JpaRepository` 상속만으로 공짜 | 메서드마다 SQL을 씀 |
| 수정(U) | **변경 감지** — 값만 바꾸면 UPDATE | `UPDATE`문을 직접 쓰고 직접 호출 |
| 테이블 생성 | `ddl-auto=update`로 자동 가능 | 없음 — `schema.sql`이나 DDL로 직접 |
| 결과 → 객체 매핑 | 자동 | 자동 (`map-underscore-to-camel-case` 설정) |
| 복잡한 조회(통계, 다중 JOIN) | 별도 문법(JPQL 등) 학습 필요 | **그냥 SQL이라 자유로움** — 강점! |
| DB 제품 교체 | 방언(Dialect)을 자동 처리 | SQL이 그 DB 문법에 묶임 — 직접 관리 |
| 배우는 비용 | 개념(영속성, 변경 감지 등)이 많음 | SQL만 알면 진입이 쉬움 |
| 주로 쓰는 곳 | 신규 서비스, 스타트업에서 흔함 | **국내 SI·공공(eGovFrame)의 사실상 표준** |

> ⚖️ 어느 쪽이 "더 좋은 기술"이냐의 문제가 아닙니다. **단순 CRUD가 많으면 JPA가 편하고,
> 손으로 다듬은 복잡한 SQL이 많으면 MyBatis가 편합니다.** 그래서 실무에서는 프로젝트 성격과
> 팀의 표준에 따라 고릅니다 — 우리 회사(공공 SI)에서는 MyBatis입니다.

---

## 2. 무엇이 바뀌고, 무엇이 그대로인가

레이어드 아키텍처를 배운 보람이 여기서 나옵니다. **바뀌는 것은 리포지토리 계층뿐입니다.**

```text
Controller  ──  동일 (한 글자도 안 바뀜)
Service     ──  거의 동일 (update 한 곳만 다름 — 6절)
─────────────────────────────────────────────
Repository  →   Mapper 인터페이스 + 매퍼 XML   ← 여기만 교체!
─────────────────────────────────────────────
템플릿(html) ──  동일
DB(테이블)   ──  동일 (intro 테이블 그대로)
```

파일 단위로 보면 이렇게 대응됩니다.

| intro-jpa (JPA) | intro-mybatis (MyBatis) |
|---|---|
| `repository/IntroRepository.java` (상속만) | `mapper/IntroMapper.java` + **`resources/mapper/IntroMapper.xml`** |
| 엔티티 `Intro.java` (@Entity, @Id, @Column) | `Intro.java` — **어노테이션 없는 순수 자바 클래스** |
| `spring.jpa.hibernate.ddl-auto=update` | **`schema.sql`** + `spring.sql.init.mode=always` |
| `spring.jpa.show-sql=true` | `logging.level.<매퍼 패키지>=trace` |

---

## 3. 설정 ① — 의존성 추가

`build.gradle`에서 JPA 스타터 자리에 MyBatis 스타터가 들어갑니다.

```groovy
dependencies {
	// JPA 자리(spring-boot-starter-data-jpa)에 MyBatis 스타터가 들어왔습니다.
	// 스프링부트가 버전을 관리해 주지 않는 외부 스타터라서 버전을 직접 적습니다.
	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.1'

	implementation 'org.springframework.boot:spring-boot-h2console'
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-webmvc'
	// ... (H2, MySQL 드라이버 등은 01과 동일)
}
```

두 가지 포인트:

1. **버전을 직접 적습니다** — MyBatis는 스프링 팀이 아닌 MyBatis 팀이 만드는 스타터라서,
   스프링부트의 자동 버전 관리 대상이 아닙니다. (스타터 4.x가 스프링부트 4용입니다.
   start.spring.io에서 "MyBatis Framework"를 검색해 추가해도 됩니다.)
2. JPA 관련은 전부 뺐습니다 — `starter-data-jpa`가 없으니 `ddl-auto` 같은 JPA 설정도 더는 없습니다.

---

## 4. 설정 ② — application.properties

datasource 부분(H2 접속, H2 콘솔)은 01과 완전히 같고, **JPA 블록이 MyBatis 블록으로** 바뀝니다.

```properties
# ===== 테이블 생성 (schema.sql) =====
# JPA의 ddl-auto=update가 없으므로, 시작할 때 schema.sql을 실행해 테이블을 만듭니다.
# DDL에 IF NOT EXISTS가 있어서 매번 실행해도 안전하고, 데이터는 유지됩니다.
spring.sql.init.mode=always

# ===== MyBatis =====
# 매퍼 XML 파일들의 위치
mybatis.mapper-locations=classpath:mapper/*.xml
# DB의 밑줄표기(created_at)를 자바의 낙타표기(createdAt) 필드에 자동 연결
# (JPA는 기본 동작이었지만, MyBatis에서는 이 한 줄을 켜야 합니다!)
mybatis.configuration.map-underscore-to-camel-case=true

# ===== SQL 로그 =====
# 매퍼가 실행하는 SQL과 바인딩 값을 콘솔에 보여줍니다. (JPA의 show-sql에 해당)
logging.level.com.example.intromybatis.mapper=trace
```

그리고 `resources/schema.sql` — "테이블 자동 생성도 사실 JPA의 기능이었구나"를 깨닫는 지점입니다.
MyBatis에서는 [DB 교육 03](../../03_Database/03_SQL_기초.md)에서 배운 DDL을 직접 준비합니다.

```sql
CREATE TABLE IF NOT EXISTS intro (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    VARCHAR(4000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

> 💡 **MySQL 프로필은?** [02 문서](./02_MySQL로_전환하기.md)와 같은 방식으로
> `application-mysql.properties`에 접속 정보만 적고, `spring.sql.init.mode=never`로
> schema.sql 실행을 끕니다(테이블은 관리자가 만드니까 — 02와 같은 논리).
> 다만 JPA의 `validate` 같은 "시작할 때 검사"는 MyBatis에 없어서,
> 테이블이 없으면 기동은 되고 **첫 SQL 실행 때** 에러가 납니다.

---

## 5. 코드 — 매퍼 인터페이스와 XML

### 도메인 클래스: 어노테이션이 전부 사라졌다

```java
/** @Entity, @Id, @Column이 전부 사라졌습니다! */
public class Intro {
    private Long id;
    private String name;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    // getter/setter (01과 동일)
}
```

MyBatis에서 이 클래스는 SQL 결과를 담는 **그릇**일 뿐입니다.
테이블과의 연결은 클래스가 아니라 **SQL이** 담당하기 때문에, 아무 표식도 필요 없습니다.

### 매퍼 인터페이스 — 리포지토리의 자리

```java
@Mapper
public interface IntroMapper {

    List<Intro> findAll();        // [R] 전체 목록
    Intro findById(Long id);      // [R] 한 건 (없으면 null)
    int insert(Intro intro);      // [C] 저장
    int update(Intro intro);      // [U] 수정
    int deleteById(Long id);      // [D] 삭제
}
```

`JpaRepository`를 상속하면 메서드가 공짜로 생기던 JPA와 달리,
**메서드를 선언하고 그 SQL을 아래 XML에 직접 씁니다.**

### 매퍼 XML — SQL의 본체 (`resources/mapper/IntroMapper.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.intromybatis.mapper.IntroMapper">

    <!-- [R] 전체 목록: 최신 글이 위로. 정렬도 SQL에 직접 씁니다. -->
    <select id="findAll" resultType="com.example.intromybatis.domain.Intro">
        SELECT id, name, title, content, created_at
        FROM intro
        ORDER BY id DESC
    </select>

    <!-- [R] 한 건 조회 -->
    <select id="findById" resultType="com.example.intromybatis.domain.Intro">
        SELECT id, name, title, content, created_at
        FROM intro
        WHERE id = #{id}
    </select>

    <!-- [C] 저장: DB가 매긴 번호를 intro.id로 돌려받습니다
         (JDBC 실습의 RETURN_GENERATED_KEYS와 같은 기능!) -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO intro (name, title, content, created_at)
        VALUES (#{name}, #{title}, #{content}, #{createdAt})
    </insert>

    <!-- [U] 수정 -->
    <update id="update">
        UPDATE intro
        SET name = #{name},
            title = #{title},
            content = #{content}
        WHERE id = #{id}
    </update>

    <!-- [D] 삭제 -->
    <delete id="deleteById">
        DELETE FROM intro
        WHERE id = #{id}
    </delete>

</mapper>
```

읽는 규칙은 세 가지뿐입니다.

1. `namespace` = 매퍼 인터페이스의 풀 경로, 각 태그의 `id` = **메서드 이름**과 일치
2. `resultType` : SELECT 결과 한 줄을 어떤 클래스에 담을지
3. `#{name}` : 파라미터(또는 객체의 필드)를 안전하게 바인딩

이 SQL들, 어디서 봤죠? **DB 교육에서 여러분이 직접 쓰던 그 SQL 그대로**입니다.
MyBatis의 학습 곡선이 완만한 이유가 이것입니다 — 새로 배울 것은 "연결 규칙"뿐입니다.

### ⚠️ `#{}` vs `${}` — 절대 어기면 안 되는 규칙

MyBatis에는 값을 넣는 문법이 두 개 있는데, 성격이 완전히 다릅니다.

| | 동작 | 결과 |
|---|---|---|
| `#{id}` | PreparedStatement의 `?` 바인딩 ([DB 교육 04](../../03_Database/04_JDBC_실습.md)에서 배운 그것) | **안전** |
| `${id}` | 문자열을 SQL에 **그대로 이어붙임** | **SQL Injection 구멍** |

`${}`에 사용자 입력이 들어가면, 입력창에 `1 OR 1=1` 같은 것을 넣어 남의 데이터를
통째로 읽거나 지우는 공격(SQL Injection)이 가능해집니다.
**값은 무조건 `#{}`** — `${}`는 테이블명·컬럼명처럼 `?`로 바인딩할 수 없는 자리에,
그것도 사용자 입력이 아닌 값만 넣습니다. 실무 코드 리뷰에서 가장 먼저 보는 항목 중 하나입니다.

---

## 6. 서비스 — 딱 한 곳, update가 다르다

서비스의 다른 메서드는 호출 대상만 `introRepository` → `introMapper`로 바뀌는데,
**수정(U)만은 코드의 모양 자체가 다릅니다.** 나란히 놓고 보세요.

```java
// ===== JPA (intro-jpa) — 변경 감지 =====
@Transactional
public void update(Long id, String name, String title, String content) {
    Intro intro = findById(id);
    intro.setName(name);
    intro.setTitle(title);
    intro.setContent(content);
    // save() 없음! 값이 바뀐 것을 JPA가 감지해 UPDATE를 만들어 줌
}

// ===== MyBatis (intro-mybatis) — 직접 호출 =====
@Transactional
public void update(Long id, String name, String title, String content) {
    Intro intro = findById(id);   // 존재 확인 (없으면 예외)
    intro.setName(name);
    intro.setTitle(title);
    intro.setContent(content);
    introMapper.update(intro);    // ← 이 호출이 없으면 아무 일도 일어나지 않습니다!
}
```

MyBatis에는 변경 감지가 없습니다. 객체의 값을 아무리 바꿔도 DB는 모릅니다 —
**XML에 쓴 UPDATE문을 직접 호출해야** 반영됩니다.
(`@Transactional`의 역할, 즉 메서드 단위 커밋/롤백은 두 쪽 다 똑같습니다.)

또 하나: JPA의 `findById`는 `Optional`을 줬지만 MyBatis는 없으면 **null**을 줍니다.
그래서 서비스에서 null 검사를 직접 합니다. ([샘플의 IntroService.java](./샘플/intro-mybatis/src/main/java/com/example/intromybatis/service/IntroService.java) 참고)

---

## 7. 실행 — 로그로 확인하기

```powershell
cd 샘플/intro-mybatis
.\gradlew.bat bootRun
# MySQL로: .\gradlew.bat bootRun --args='--spring.profiles.active=mysql'
```

`http://localhost:8080` — 화면과 기능은 intro-jpa와 **완전히 같습니다.**
다른 것은 콘솔 로그입니다. (아래는 실제 실행 출력)

```text
c.e.i.mapper.IntroMapper.insert  : ==>  Preparing: INSERT INTO intro (name, title, content, created_at) VALUES (?, ?, ?, ?)
c.e.i.mapper.IntroMapper.insert  : ==> Parameters: 김철수(String), 성장하는 개발자 김철수입니다(String), ...
c.e.i.mapper.IntroMapper.insert  : <==    Updates: 1
c.e.i.mapper.IntroMapper.update  : ==>  Preparing: UPDATE intro SET name = ?, title = ?, content = ? WHERE id = ?
c.e.i.mapper.IntroMapper.update  : ==> Parameters: 김철수(String), 수정된 제목입니다(String), 내용도 수정했습니다.(String), 1(Long)
c.e.i.mapper.IntroMapper.update  : <==    Updates: 1
c.e.i.mapper.IntroMapper.deleteById : ==>  Preparing: DELETE FROM intro WHERE id = ?
c.e.i.mapper.IntroMapper.deleteById : ==> Parameters: 1(Long)
c.e.i.mapper.IntroMapper.deleteById : <==    Updates: 1
```

`Preparing`(내가 쓴 SQL) → `Parameters`(바인딩된 값) → `Updates/Total`(결과 건수).
**이 로그 형식, 입사하면 매일 보게 됩니다.** eGovFrame 프로젝트의 콘솔이 정확히 이렇게 생겼습니다.

---

## 8. eGovFrame과의 연결 — 오늘 배운 것이 실무에서 이렇게 보인다

| 오늘 (스프링부트 + MyBatis) | 회사 실무 (eGovFrame) |
|---|---|
| **매퍼 XML의 SQL** | **거의 그대로 동일** — 실무의 핵심 산출물 |
| `@Mapper` 인터페이스 | Mapper 인터페이스 또는 DAO 클래스 |
| `application.properties`의 mybatis 설정 | `context-mapper.xml` 등 XML 설정 파일 |
| `#{}` 바인딩, `resultType` 매핑 | 동일 |
| 동적 SQL(`<if>`, `<foreach>` — 다음 단계 주제) | 검색 조건 조립 등에 매우 많이 사용 |

설정 파일의 생김새(properties vs XML)만 다를 뿐, **매퍼 XML을 읽고 쓸 수 있으면
실무 코드의 절반은 읽을 수 있습니다.** 실무에서 진짜 자주 쓰는 동적 SQL(`<if>`, `<foreach>`)은
이 문서의 범위를 넘으니, [MyBatis 공식 문서(한국어)](https://mybatis.org/mybatis-3/ko/dynamic-sql.html)를 다음 학습으로 추천합니다.

---

## 정리

- **JPA는 SQL을 만들어 주고, MyBatis는 내 SQL을 연결해 줍니다.** MyBatis는 JDBC와 JPA의 중간.
- 설정은 세 가지: 스타터 의존성(버전 직접), `mapper-locations` + `map-underscore-to-camel-case`, 그리고 `schema.sql`(ddl-auto의 빈자리).
- 바뀐 것은 리포지토리 계층뿐 — 컨트롤러·서비스·화면은 그대로. **계층을 나눈 덕분입니다.**
- MyBatis에는 변경 감지가 없습니다. **UPDATE는 직접 호출.**
- 값 바인딩은 무조건 `#{}`. `${}`에 사용자 입력을 넣는 순간 보안 사고입니다.

이제 여러분은 같은 앱을 JPA와 MyBatis로 한 번씩 만들어 본 사람입니다.
두 샘플의 코드를 나란히 열어 놓고 비교해 보는 것이 최고의 복습입니다.

---

⬅️ 이전: [02. MySQL로 전환하기](./02_MySQL로_전환하기.md) | 처음으로: [README](./README.md)
