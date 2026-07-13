# 01. JPA로 H2 CRUD 완성하기 — 스프링부트 4

> **이 문서에서 배우는 것**
> - 스프링부트 **4** 프로젝트 만들기 (3.5로 배웠던 것과 달라진 점 포함)
> - JPA의 핵심 그림: 엔티티 ↔ 테이블, 리포지토리 메서드 ↔ SQL
> - H2 데이터베이스를 연결하고 **등록(C)·조회(R)·수정(U)·삭제(D)** 를 전부 구현
> - `save()`를 부르지 않았는데 UPDATE가 나가는 **변경 감지(Dirty Checking)**

[스프링부트 교육 03](../SpringBoot/03_실습_자기소개서_만들기.md)에서 만든 자기소개서 앱은
등록과 조회(CR)까지였습니다. 이번에는 처음부터 새로 만들면서 **수정과 삭제(UD)** 를 채워
CRUD를 완성합니다. 완성본은 [샘플/intro-jpa](./샘플/intro-jpa/)에 있습니다.

---

## 1. JPA, 1분 복습 — "JDBC에서 내가 하던 일을 대신해 주는 것"

[DB 교육 04(JDBC)](../../03_Database/04_JDBC_실습.md)에서 저장 한 번 하려고 했던 일을 떠올려 보세요.

| JDBC에서 직접 하던 일 | JPA에서는 |
|---|---|
| 접속 열기 (`DriverManager.getConnection`) | 스프링부트가 설정 파일을 읽고 자동 처리 |
| SQL 문자열 작성 (`INSERT INTO intro ...`) | **메서드 이름으로 대신** (`save()`, `findAll()` ...) |
| 값 바인딩 (`ps.setString(1, ...)`) | 객체의 필드를 보고 자동 바인딩 |
| 결과를 객체로 옮겨 담기 (`rs.getString(...)`) | 테이블 한 줄 → 객체 하나로 자동 변환 |
| 자원 닫기, commit/rollback | 자동 (트랜잭션은 `@Transactional` 한 줄) |

이렇게 **"테이블의 줄(row) ↔ 자바 객체"를 자동으로 오가게 해 주는 기술**을
ORM(Object-Relational Mapping)이라 하고, 자바 진영의 표준 명세가 **JPA**,
그 대표 구현체가 **Hibernate**입니다. 스프링부트에서는 `spring-boot-starter-data-jpa`
하나만 넣으면 이 세트가 통째로 들어옵니다.

> 🏢 **eGovFrame 연결 포인트** — 회사 실무(eGovFrame)는 JPA 대신 **MyBatis**를 씁니다.
> MyBatis는 SQL을 개발자가 직접 쓰고(XML), 결과를 객체에 담는 부분만 자동화합니다.
> "리포지토리 계층이 무슨 일을 하는 자리인지"는 완전히 같아서, JPA로 구조를 익혀 두면
> 실무의 DAO/Mapper 코드도 같은 눈으로 읽을 수 있습니다.

---

## 2. 프로젝트 만들기 — start.spring.io 기본값 그대로

[start.spring.io](https://start.spring.io)에서 아래처럼 선택합니다.
이번에는 **Spring Boot 버전을 기본값(4.x) 그대로** 둡니다. (이 글 작성 시점 기본값: 4.1.0)

| 항목 | 값 |
|---|---|
| Project / Language | **Gradle - Groovy** / **Java** |
| Spring Boot | **4.x (기본값 그대로)** |
| Group / Artifact | `com.example` / `intro-jpa` |
| Package name | `com.example.introjpa` |
| Java | **17** |

**Dependencies (6개):**

| 의존성 | 왜 필요한가 |
|---|---|
| Spring Web | 컨트롤러, 내장 톰캣 |
| Thymeleaf | HTML 템플릿 |
| **Spring Data JPA** | 오늘의 주인공 |
| **H2 Database** | 개발용 DB |
| **MySQL Driver** | 02 문서에서 쓸 MySQL 접속 드라이버 (미리 넣어 둡니다) |
| Spring Boot DevTools | 코드 수정 시 자동 재시작 |

GENERATE로 zip을 받아 풀고 IDE로 여세요.

### 스프링부트 4에서 달라진 점 (3.5와 비교)

생성된 `build.gradle`을 열면 3.5 때와 이름이 조금 다릅니다. **역할은 그대로**이니 놀라지 마세요.

```groovy
dependencies {
	implementation 'org.springframework.boot:spring-boot-h2console'          // ← ①
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-webmvc'     // ← ②
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	runtimeOnly 'com.h2database:h2'
	runtimeOnly 'com.mysql:mysql-connector-j'
	testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'   // ← ③
	testImplementation 'org.springframework.boot:spring-boot-starter-thymeleaf-test'
	testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

1. **H2 웹 콘솔이 별도 모듈로 분리** — 4부터는 `spring-boot-h2console` 의존성이 있어야
   `/h2-console`이 열립니다. (start.spring.io에서 H2를 고르면 자동으로 넣어 줍니다)
2. `spring-boot-starter-web` → **`spring-boot-starter-webmvc`** 로 이름이 바뀌었습니다.
3. 테스트 스타터가 기술별(`-data-jpa-test`, `-webmvc-test`...)로 잘게 나뉘었습니다.

마지막으로, 한글 주석이 깨지지 않도록 `build.gradle` 맨 아래에 추가합니다. (3.5 때와 동일)

```groovy
// 소스 파일을 UTF-8로 읽도록 지정 (Windows에서 한글 주석이 깨지는 것 방지)
tasks.withType(JavaCompile).configureEach {
	options.encoding = 'UTF-8'
}
```

---

## 3. H2 연결 설정 — application.properties

`src/main/resources/application.properties`를 이렇게 채웁니다.

```properties
# 애플리케이션 이름
spring.application.name=intro-jpa

# ===== H2 데이터베이스 (파일 모드) =====
# 프로젝트 폴더 아래 data/introdb 파일에 데이터를 저장합니다.
# 파일 모드라서 서버를 껐다 켜도 데이터가 남아 있습니다.
# 드라이버 클래스는 적지 않습니다. 스프링부트가 URL(jdbc:h2:)을 보고 알아서 고릅니다.
spring.datasource.url=jdbc:h2:./data/introdb
spring.datasource.username=sa
spring.datasource.password=

# ===== H2 웹 콘솔 =====
# 브라우저에서 http://localhost:8080/h2-console 로 DB 내용을 직접 볼 수 있습니다.
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# ===== JPA =====
spring.jpa.hibernate.ddl-auto=update

# 콘솔에 실행되는 SQL을 보여줍니다. JPA가 만든 SQL을 눈으로 확인해 보세요.
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

접속 정보(URL, 사용자, 비밀번호)는 [DB 교육 02](../../03_Database/02_접속과_권한.md)에서 배운
"접속 5요소" 그대로입니다. JDBC 실습 때 자바 상수로 적던 것이 **설정 파일로 나왔을 뿐**입니다.

### `ddl-auto` — 테이블을 누가 만들 것인가

| 값 | 동작 | 언제 쓰나 |
|---|---|---|
| `create` | 시작할 때마다 테이블을 **지우고** 새로 만듦 | 데이터가 날아가도 되는 실험 |
| `update` | 없으면 만들고, 있으면 유지 (데이터 보존) | **개발 단계 (오늘 사용)** |
| `validate` | 만들지 않고 엔티티와 테이블이 맞는지 **검사만** | 운영 DB (02 문서에서 사용) |
| `none` | 아무것도 안 함 | 운영 DB |

> ⚠️ 개발하다 `create`로 바꿔 실험한 뒤 `update`로 되돌리는 걸 잊으면,
> 재시작할 때마다 데이터가 통째로 사라집니다. 단골 사고이니 기억해 두세요.

---

## 4. 엔티티 — 클래스 하나가 테이블 하나

`domain/Intro.java` — [DB 교육 03](../../03_Database/03_SQL_기초.md)에서 DDL로 직접 만들었던
`intro` 테이블을, 이번에는 **자바 클래스로 선언**합니다.

```java
@Entity
public class Intro {

    // 기본키(PK). DB가 1, 2, 3... 순서대로 번호를 자동으로 매겨줍니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 이름
    @Column(length = 50, nullable = false)
    private String name;

    // 자기소개서 제목
    @Column(length = 200, nullable = false)
    private String title;

    // 자기소개 내용. 길게 쓸 수 있도록 컬럼 길이를 넉넉하게 잡았습니다.
    @Column(length = 4000)
    private String content;

    // 작성 시각
    private LocalDateTime createdAt;

    public Intro() {
    }

    // getter / setter 는 지면상 생략 — 샘플 프로젝트에 전체 코드가 있습니다.
}
```

이 클래스가 DB 교육 때의 DDL과 어떻게 짝을 이루는지 비교해 보세요.

| 엔티티 (자바) | DDL (SQL) | 변환 규칙 |
|---|---|---|
| `class Intro` | `CREATE TABLE intro` | 클래스명 → 테이블명 |
| `@Id @GeneratedValue(IDENTITY)` | `id BIGINT AUTO_INCREMENT PRIMARY KEY` | PK + 자동 번호 |
| `@Column(length=50, nullable=false)` | `name VARCHAR(50) NOT NULL` | 제약조건 |
| `private LocalDateTime createdAt` | `created_at TIMESTAMP` | **camelCase → snake_case 자동 변환** |

`ddl-auto=update` 덕분에 앱을 처음 켜면 JPA가 이 클래스를 읽고 `CREATE TABLE`을 대신 실행해 줍니다.

---

## 5. 리포지토리 — 인터페이스 선언만으로 SQL이 공짜

`repository/IntroRepository.java`는 이게 전부입니다.

```java
public interface IntroRepository extends JpaRepository<Intro, Long> {
}
```

`JpaRepository<Intro, Long>`("Intro 엔티티를 다루고 PK 타입은 Long")을 상속하는 것만으로
CRUD 메서드가 자동으로 생깁니다. **오늘 쓸 메서드와 SQL의 대응**은 다음과 같습니다.

| 메서드 | 번역되는 SQL | CRUD |
|---|---|---|
| `save(intro)` | `INSERT INTO intro (...) VALUES (...)` | **C** |
| `findAll()` / `findById(id)` | `SELECT ... FROM intro [WHERE id = ?]` | **R** |
| (변경 감지 — 6절) | `UPDATE intro SET ... WHERE id = ?` | **U** |
| `deleteById(id)` | `DELETE FROM intro WHERE id = ?` | **D** |

---

## 6. 서비스 — CRUD 네 가지 업무 규칙

`service/IntroService.java`의 핵심부입니다. 조회(R)와 등록(C)은 스프링부트 교육 때와 같고,
**수정(U)과 삭제(D)가 새로 추가**되었습니다.

```java
@Service
public class IntroService {

    private final IntroRepository introRepository;

    public IntroService(IntroRepository introRepository) {
        this.introRepository = introRepository;   // 생성자 주입(DI)
    }

    /** [R] 전체 목록 (최신 글이 위로) */
    public List<Intro> findAll() {
        return introRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** [R] 한 건 조회. 없으면 예외 */
    public Intro findById(Long id) {
        return introRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자기소개서입니다. id=" + id));
    }

    /** [C] 등록 */
    public Intro create(String name, String title, String content) {
        Intro intro = new Intro();
        intro.setName(name);
        intro.setTitle(title);
        intro.setContent(content);
        intro.setCreatedAt(LocalDateTime.now());
        return introRepository.save(intro);   // 이 한 줄이 INSERT
    }

    /** [U] 수정 — save()가 없는 것에 주목! */
    @Transactional
    public void update(Long id, String name, String title, String content) {
        Intro intro = findById(id);   // 1. 조회하고
        intro.setName(name);          // 2. 값만 바꾸면
        intro.setTitle(title);
        intro.setContent(content);    // 3. 메서드가 끝날 때 UPDATE가 나갑니다.
    }

    /** [D] 삭제 */
    public void delete(Long id) {
        introRepository.deleteById(id);   // 이 한 줄이 DELETE
    }
}
```

### 변경 감지(Dirty Checking) — JPA에서 가장 신기한 순간

`update()`에는 `save()`가 없는데 어떻게 DB가 바뀔까요?

1. `@Transactional`이 붙은 메서드가 시작되면 **트랜잭션**이 열립니다.
   ([DB 교육 04의 Step06](../../03_Database/04_JDBC_실습.md)에서 직접 하던 `commit()`을 스프링이 대신하는 것)
2. 트랜잭션 안에서 조회한 엔티티는 JPA가 **원본 스냅샷을 찍어 두고 감시**합니다.
3. 메서드가 정상 종료되면 스냅샷과 현재 값을 비교해서, **달라진 필드가 있으면 UPDATE문을 자동 실행**하고 commit합니다.

그래서 "조회 → 값 변경"만 하면 수정이 끝납니다. 이것이 변경 감지입니다.
(`save(intro)`를 명시적으로 불러도 결과는 같습니다. 다만 JPA다운 방식은 위쪽입니다.)

---

## 7. 컨트롤러 — URL 일곱 개로 CRUD 완성

먼저 URL 설계입니다. 스프링부트 교육 때의 네 개에 **아래 세 개가 추가**됩니다.

| 메서드 | URL | 역할 | CRUD |
|---|---|---|---|
| GET | `/` | 목록 | R |
| GET | `/intro/new` | 작성 폼 | |
| POST | `/intro` | 저장 | C |
| GET | `/intro/{id}` | 상세 | R |
| **GET** | **`/intro/{id}/edit`** | **수정 폼 (기존 값 채워서)** | |
| **POST** | **`/intro/{id}/edit`** | **수정 처리** | **U** |
| **POST** | **`/intro/{id}/delete`** | **삭제 처리** | **D** |

> ❓ **왜 수정·삭제도 POST인가요?** HTML의 `<form>`은 GET과 POST만 보낼 수 있습니다.
> (REST API를 만들 때는 PUT/DELETE 메서드를 쓰지만, 그건 다음 단계의 주제입니다.)
> 그리고 **데이터를 바꾸는 요청을 GET(링크)으로 만들면 안 됩니다** — 브라우저나 검색엔진이
> 링크를 미리 읽기만 해도 글이 지워지는 사고가 납니다.

`controller/IntroController.java`에 추가되는 부분:

```java
/** 수정 폼 화면: GET /intro/{id}/edit — 기존 값을 채워서 보여줍니다. */
@GetMapping("/intro/{id}/edit")
public String editForm(@PathVariable Long id, Model model) {
    model.addAttribute("intro", introService.findById(id));
    return "edit"; // → templates/edit.html
}

/** [U] 수정 처리: POST /intro/{id}/edit — 수정 후 상세 화면으로 돌아갑니다. */
@PostMapping("/intro/{id}/edit")
public String edit(@PathVariable Long id,
                   @RequestParam String name,
                   @RequestParam String title,
                   @RequestParam String content) {
    introService.update(id, name, title, content);
    return "redirect:/intro/" + id;
}

/** [D] 삭제 처리: POST /intro/{id}/delete — 삭제 후 목록으로 돌아갑니다. */
@PostMapping("/intro/{id}/delete")
public String delete(@PathVariable Long id) {
    introService.delete(id);
    return "redirect:/";
}
```

목록/작성/상세 부분은 스프링부트 교육 03과 동일합니다. 전체 코드는
[샘플의 IntroController.java](./샘플/intro-jpa/src/main/java/com/example/introjpa/controller/IntroController.java)를 보세요.

---

## 8. 템플릿 — 새로 만드는 화면 두 가지

`list.html` / `form.html` / `detail.html`은 스프링부트 교육 때와 거의 같으니
([샘플 templates 폴더](./샘플/intro-jpa/src/main/resources/templates/) 참고), 새로운 부분만 봅니다.

### detail.html — 수정·삭제 버튼 달기

```html
<!-- 목록/수정은 링크(GET), 삭제는 폼 전송(POST)입니다. -->
<div class="d-flex gap-2 mt-4">
    <a th:href="@{/}" class="btn btn-outline-secondary">← 목록으로</a>
    <a th:href="@{/intro/{id}/edit(id=${intro.id})}" class="btn btn-outline-primary">수정</a>
    <form th:action="@{/intro/{id}/delete(id=${intro.id})}" method="post"
          onsubmit="return confirm('정말 삭제할까요? 되돌릴 수 없습니다.');">
        <button type="submit" class="btn btn-outline-danger">삭제</button>
    </form>
</div>
```

삭제 버튼은 링크가 아니라 **한 줄짜리 폼**입니다. `onsubmit`의 `confirm()`은
여러분이 JS 시간에 배운 그 함수 — 실수 클릭을 막는 최소한의 안전장치입니다.

### edit.html — 기존 값이 채워진 폼

작성 폼(form.html)과 거의 같고, 두 가지만 다릅니다.

```html
<form th:action="@{/intro/{id}/edit(id=${intro.id})}" method="post">  <!-- ① 전송 주소가 다름 -->

    <input type="text" class="form-control" id="name" name="name"
           th:value="${intro.name}" maxlength="50" required>          <!-- ② 기존 값을 미리 채움 -->

    <!-- textarea는 value 속성이 없어서 th:text로 내용을 채웁니다 -->
    <textarea class="form-control" id="content" name="content" rows="10"
              maxlength="4000" required th:text="${intro.content}"></textarea>
    ...
</form>
```

---

## 9. 실행하고 눈으로 확인하기

```powershell
.\gradlew.bat bootRun
```

`http://localhost:8080`에서 **등록 → 목록 → 상세 → 수정 → 삭제**를 차례로 눌러 보세요.
그때마다 콘솔에 JPA가 만든 SQL이 흘러갑니다. (아래는 실제 실행 출력)

```text
Hibernate:
    insert
    into
        intro
        (content, created_at, name, title, id)
    values
        (?, ?, ?, ?, default)
Hibernate:
    update
        intro
    set
        content=?,
        created_at=?,
        name=?,
        title=?
    where
        id=?
Hibernate:
    delete
    from
        intro
    where
        id=?
```

**여러분이 DB 교육에서 직접 쓰던 바로 그 SQL**입니다. JPA는 마법이 아니라
"SQL을 대신 써 주는 비서"라는 것, 이 출력이 그 증거입니다.

마지막으로 `http://localhost:8080/h2-console`에 접속해서
(JDBC URL에 `jdbc:h2:./data/introdb` 입력) `SELECT * FROM INTRO;`로
테이블에 실제로 데이터가 있는지 확인해 보세요.

---

## 정리

- 스프링부트 4는 3.5와 개념이 같습니다. 의존성 이름 몇 개(`starter-webmvc`, `spring-boot-h2console`)만 달라졌습니다.
- 엔티티 클래스 하나 = 테이블 하나. `ddl-auto=update`면 테이블도 JPA가 만들어 줍니다.
- CRUD 대응: `save()`=INSERT, `find...()`=SELECT, **변경 감지**=UPDATE, `deleteById()`=DELETE.
- 데이터를 바꾸는 요청(수정·삭제)은 반드시 POST로.

다음 문서에서는 이 앱을 **코드 수정 없이** MySQL 서버로 옮깁니다.

---

➡️ 다음: [02. MySQL로 전환하기](./02_MySQL로_전환하기.md) | 처음으로: [README](./README.md)
