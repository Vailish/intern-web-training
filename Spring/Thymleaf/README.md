# 타임리프(Thymeleaf) 기본 문법 정리

스프링부트 실습([03. 자기소개서 만들기](../SpringBoot/03_실습_자기소개서_만들기.md))에서 화면을 만들 때 쓰는
**타임리프 템플릿 엔진**의 설정 방법과 기본 문법을 한곳에 정리한 레퍼런스입니다.
실습 중 "이 문법이 뭐였지?" 싶을 때 찾아보세요.

모든 예제는 자기소개서 프로젝트(`intro`, 엔티티 `Intro`)를 기준으로 합니다.

---

## 1. 타임리프란?

**서버의 데이터를 HTML에 끼워 넣어 완성된 화면을 만들어 주는 템플릿 엔진**입니다.

여러분이 배운 HTML은 내용이 고정된 **정적** 문서였습니다.
타임리프는 컨트롤러가 `Model`에 담아 보낸 데이터를 HTML 속 지정된 자리에 채워서,
요청할 때마다 내용이 달라지는 **동적** 화면을 만듭니다.

```
브라우저 요청 → 컨트롤러가 Model에 데이터 담아 "list" 반환
             → 타임리프가 templates/list.html에 데이터를 채움
             → 완성된 HTML을 브라우저로 응답
```

**타임리프의 가장 큰 특징 — 내추럴 템플릿(Natural Template)**

타임리프 문법은 전부 HTML **속성**(`th:~~`) 형태라서, 서버 없이 브라우저로 파일을 직접 열어도
HTML이 깨지지 않고 그대로 보입니다.

```html
<!-- 서버를 거치면 "성장하는 개발자 김철수입니다"로 바뀌고,
     파일을 그냥 열면 "제목 예시"가 보입니다 -->
<h1 th:text="${intro.title}">제목 예시</h1>
```

태그 안의 `제목 예시`는 **미리보기용 예시값**이고, 실제 값은 `th:text`가 덮어씁니다.
디자이너·퍼블리셔와 협업할 때 서버 없이도 화면을 확인할 수 있어 실무에서 사랑받는 특징입니다.

---

## 2. 스프링부트에서 설정 방법

### 2-1. 의존성 추가 (이것이 사실상 전부입니다)

`build.gradle`의 `dependencies`에 스타터 한 줄을 추가합니다.
(start.spring.io에서 **Thymeleaf**를 체크했다면 이미 들어 있습니다.)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    // ...
}
```

스프링부트의 **자동 설정(Auto Configuration)** 덕분에, 의존성만 있으면
아래 규칙이 별도 설정 없이 바로 동작합니다.

| 항목 | 기본값 | 의미 |
|---|---|---|
| 템플릿 위치 | `src/main/resources/templates/` | 컨트롤러가 반환한 이름을 여기서 찾음 |
| 확장자 | `.html` | `return "list"` → `templates/list.html` |
| 정적 리소스 위치 | `src/main/resources/static/` | CSS/JS/이미지는 여기에 |
| 인코딩 | UTF-8 | 한글 걱정 없음 |

> **컨트롤러 ↔ 템플릿 연결 규칙**
> `@Controller`의 메서드가 문자열 `"list"`를 반환하면
> 스프링부트가 `templates/` + `list` + `.html`을 조합해 파일을 찾습니다.
> 앞뒤를 자동으로 붙여 주므로 `return "list.html"`이나 `return "/templates/list"`로 쓰면 오히려 에러가 납니다.

### 2-2. HTML에서 타임리프 선언

템플릿 파일의 `<html>` 태그에 네임스페이스를 선언합니다.
"이 문서에서 `th:`로 시작하는 속성은 타임리프 문법"이라는 뜻입니다.

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
```

(사실 선언이 없어도 동작은 하지만, 선언해야 IDE가 자동완성·문법검사를 해 줍니다. 항상 붙이는 습관을 들이세요.)

### 2-3. 개발 편의 설정 (application.properties)

```properties
# 템플릿 캐시 끄기 — HTML을 고치면 서버 재시작 없이 새로고침만으로 반영
# (운영 환경에서는 성능을 위해 true가 기본값입니다. 개발 중에만 false!)
spring.thymeleaf.cache=false
```

**DevTools를 쓰고 있다면**(우리 실습 프로젝트는 포함) 개발 중 캐시를 자동으로 꺼 주므로
이 설정을 생략해도 됩니다. 다만 HTML 수정 후에는 IDE의 빌드(IntelliJ: `Ctrl + F9`)가
한 번 일어나야 반영된다는 점만 기억하세요.

참고로 템플릿 위치나 확장자도 바꿀 수 있지만(`spring.thymeleaf.prefix`, `spring.thymeleaf.suffix`),
**기본값 그대로 쓰는 것이 표준**입니다. 바꿀 일은 거의 없습니다.

---

## 3. 기본 문법 — 표현식 3형제

타임리프 문법의 90%는 아래 세 가지 표현식으로 이루어집니다. 이것부터 확실히 잡으세요.

| 표현식 | 이름 | 용도 | 예시 |
|---|---|---|---|
| `${...}` | 변수 표현식 | **Model에 담긴 데이터**를 꺼냄 | `${intro.title}` |
| `@{...}` | 링크(URL) 표현식 | **URL 경로**를 만듦 | `@{/intro/new}` |
| `*{...}` | 선택 변수 표현식 | `th:object`로 선택한 객체의 필드 | `*{title}` |

### 3-1. `${...}` — Model의 데이터 꺼내기

컨트롤러에서 `model.addAttribute("intro", ...)`로 담은 이름 그대로 접근합니다.

```java
// 컨트롤러
model.addAttribute("intro", introService.findById(id));
```

```html
<!-- 템플릿: 객체의 필드는 점(.)으로 접근 -->
<h1 th:text="${intro.title}">제목 예시</h1>
<span th:text="${intro.name}">홍길동</span>
```

`${intro.title}`은 내부적으로 `intro.getTitle()`을 호출합니다.
**엔티티에 Getter가 없으면 값을 못 꺼냅니다** — 실습 중 빈 화면이 나오면 Getter부터 확인하세요.

### 3-2. `@{...}` — URL 만들기

`href`, `src`, `action` 등 URL이 들어가는 모든 자리에 씁니다.

```html
<!-- 고정 경로 -->
<a th:href="@{/intro/new}">✏️ 자기소개서 작성</a>

<!-- static 폴더의 정적 리소스 -->
<link rel="stylesheet" th:href="@{/css/style.css}">

<!-- 경로 변수: {id} 자리에 값을 끼움 → /intro/3 -->
<a th:href="@{/intro/{id}(id=${intro.id})}">상세 보기</a>

<!-- 쿼리 파라미터: 경로에 {} 가 없으면 ?로 붙음 → /intro?page=2 -->
<a th:href="@{/intro(page=2)}">2페이지</a>
```

> **왜 그냥 `href="/intro/new"`라고 안 쓰나요?**
> 지금은 결과가 같습니다. 하지만 `@{...}`를 쓰면 나중에 애플리케이션이
> `http://회사서버/myapp` 처럼 하위 경로에 배포되어도 타임리프가 접두어를 자동으로 붙여 줍니다.
> URL은 항상 `@{...}`로 쓰는 습관을 들이세요.

### 3-3. `*{...}` — th:object와 짝꿍 (폼에서 주로 사용)

`th:object`로 객체를 한 번 선택해 두면, 안쪽에서는 `*{필드명}`으로 짧게 접근합니다.

```html
<!-- ${intro.~~}를 반복해서 쓰는 대신 -->
<div th:object="${intro}">
    <h1 th:text="*{title}">제목 예시</h1>
    <span th:text="*{name}">홍길동</span>
</div>
```

우리 실습(03 문서)에서는 폼을 `@RequestParam`으로 받기 때문에 등장하지 않지만,
`@ModelAttribute` 방식([04 문서](../SpringBoot/04_헷갈리기_쉬운_개념.md) 참고)으로 개선하면 폼에서 자주 만나는 문법입니다.

---

## 4. 자주 쓰는 th: 속성

중요도 순서입니다. 위쪽 4개(`th:text`, `th:each`, `th:if`, `th:href`)만 알아도 실습을 완주할 수 있습니다.

### 4-1. `th:text` — 텍스트 출력

태그 안의 내용을 값으로 **교체**합니다.

```html
<td th:text="${intro.name}">홍길동</td>

<!-- 문자열 연결: | | 사이에 쓰면 + 없이 자연스럽게 섞을 수 있습니다 -->
<p th:text="|작성자: ${intro.name}|">작성자: 홍길동</p>
```

> **`th:utext`와의 차이 (보안!)**
> `th:text`는 `<b>` 같은 태그를 문자 그대로(`&lt;b&gt;`) 출력합니다(HTML 이스케이프).
> `th:utext`는 태그를 해석해서 렌더링하는데, 사용자 입력을 `th:utext`로 출력하면
> 악성 스크립트가 실행되는 **XSS 공격**에 뚫립니다. **기본은 항상 `th:text`**입니다.

### 4-2. `th:each` — 반복

컨트롤러가 보낸 목록을 한 줄씩 반복합니다. `for-each` 문과 같은 구조입니다.

```html
<!-- intros 목록에서 하나씩 꺼내 intro라는 이름으로 사용 -->
<tr th:each="intro : ${intros}">
    <td th:text="${intro.id}">1</td>
    <td th:text="${intro.title}">제목 예시</td>
</tr>
```

반복 상태가 필요하면 두 번째 변수를 선언합니다.

```html
<!-- status.index(0부터), status.count(1부터), status.first, status.last -->
<tr th:each="intro, status : ${intros}">
    <td th:text="${status.count}">1</td>   <!-- DB id 대신 화면용 순번 -->
    <td th:text="${intro.title}">제목 예시</td>
</tr>
```

### 4-3. `th:if` / `th:unless` — 조건부 렌더링

조건이 참일 때만 해당 태그를 그립니다. (거짓이면 태그 자체가 HTML에서 사라집니다)

```html
<!-- 목록이 비어 있을 때만 안내 행을 보여줌 (list.html 실제 코드) -->
<tr th:if="${#lists.isEmpty(intros)}">
    <td colspan="4">아직 등록된 자기소개서가 없습니다.</td>
</tr>

<!-- th:unless는 반대: 조건이 거짓일 때만 렌더링 -->
<p th:unless="${#lists.isEmpty(intros)}">총 소개서가 등록되어 있습니다.</p>
```

비교 연산자를 쓸 수 있습니다: `==`, `!=`, `>`, `<`, `>=`, `<=`, `and`, `or`, `!`

```html
<span th:if="${intro.id} > 10">우수 기수</span>
```

여러 갈래로 나뉠 때는 `th:switch` / `th:case`:

```html
<div th:switch="${intro.name}">
    <p th:case="'김철수'">철수의 소개서입니다.</p>
    <p th:case="*">그 외 인턴의 소개서입니다.</p>  <!-- * = default -->
</div>
```

### 4-4. `th:href` / `th:action` / `th:src` — 속성에 URL 넣기

이미 3-2에서 본 `@{...}`와 함께 씁니다.

```html
<!-- 폼 전송: 등록 버튼 → POST /intro (form.html 실제 코드) -->
<form th:action="@{/intro}" method="post">
    ...
</form>

<img th:src="@{/images/logo.png}" alt="로고">
```

### 4-5. `th:value` / `th:attr` / `th:classappend` — 기타 속성 조작

```html
<!-- input의 value 채우기 (수정 폼에서 기존 값 보여줄 때) -->
<input type="text" name="title" th:value="${intro.title}">

<!-- 조건에 따라 CSS 클래스 추가 -->
<tr th:classappend="${status.first} ? 'table-primary'">...</tr>

<!-- 아무 속성이나 지정: th:attr (자주 쓰진 않음) -->
<img th:attr="src=@{/images/logo.png}, alt=${intro.name}">
```

거의 모든 HTML 속성에 `th:` 버전이 있습니다: `th:id`, `th:name`, `th:placeholder`, `th:disabled`, `th:checked`, `th:selected` ...

### 4-6. 인라인 출력 `[[...]]` — 태그 없이 텍스트 중간에 출력

```html
<!-- th:text는 태그 내용 전체를 교체하지만, [[...]]는 원하는 자리에만 끼워 넣습니다 -->
<p>안녕하세요, [[${intro.name}]]님의 자기소개서입니다.</p>
```

`<span th:text=...>`를 남발하지 않아도 되어 편리하지만, 내추럴 템플릿의 장점(파일 직접 열기)은 조금 훼손됩니다.

### 4-7. 주석

```html
<!-- 일반 HTML 주석: 렌더링 후에도 소스 보기에 남습니다 -->
<!--/* 타임리프 주석: 렌더링 시 완전히 제거됩니다. 서버 정보가 담긴 메모는 이걸로! */-->
```

---

## 5. 유틸리티 객체 — `#`으로 시작하는 도우미

날짜 포맷, 문자열 처리 등을 도와주는 내장 도구입니다. 실습에서 실제로 쓰는 두 가지:

```html
<!-- #temporals: LocalDateTime 포맷 (list.html, detail.html 실제 코드) -->
<td th:text="${#temporals.format(intro.createdAt, 'yyyy-MM-dd HH:mm')}">2026-07-06 10:00</td>

<!-- #lists: 목록 관련 도우미 -->
<tr th:if="${#lists.isEmpty(intros)}">...</tr>
<span th:text="${#lists.size(intros)}">3</span>
```

그 밖에 알아두면 좋은 것들:

| 객체 | 용도 | 예시 |
|---|---|---|
| `#strings` | 문자열 처리 | `${#strings.abbreviate(intro.content, 30)}` — 30자 넘으면 `...` |
| `#numbers` | 숫자 포맷 | `${#numbers.formatInteger(1234567, 3, 'COMMA')}` → 1,234,567 |
| `#temporals` | 날짜/시간(java.time) | 위 예시 참고 |

전체 목록은 [공식 문서 부록](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#appendix-b-expression-utility-objects)에 있습니다.

---

## 6. 조각(Fragment) — 공통 레이아웃 재사용

머리글·바닥글처럼 **여러 페이지에서 반복되는 부분**을 한 파일로 만들어 재사용합니다.

```html
<!-- templates/fragments/header.html : 공통 조각 정의 -->
<html xmlns:th="http://www.thymeleaf.org">
<header th:fragment="siteHeader">
    <h1>인턴 자기소개서 서비스</h1>
    <nav><a th:href="@{/}">목록</a> | <a th:href="@{/intro/new}">작성</a></nav>
</header>
</html>
```

```html
<!-- 각 페이지에서 가져다 쓰기 -->
<body>
    <!-- th:replace: 이 div 태그 자체가 조각으로 교체됨 (주로 사용) -->
    <div th:replace="~{fragments/header :: siteHeader}"></div>

    <!-- th:insert: 이 div 태그는 유지하고 안쪽에 조각을 삽입 -->
    <div th:insert="~{fragments/header :: siteHeader}"></div>
</body>
```

`~{파일경로 :: 조각이름}` 형태이며, 경로는 `templates/` 기준입니다.
실습 프로젝트는 페이지가 3개뿐이라 쓰지 않았지만, 페이지가 늘어나면 필수가 되는 문법입니다.

---

## 7. 자주 나는 에러와 주의점

**① 화면이 하얗게 나오거나 500 에러 — 십중팔구 `${...}` 안의 오타**

```
org.springframework.expression.spel.SpelEvaluationException:
Property or field 'titel' cannot be found on object of type 'com.example.intro.domain.Intro'
```

콘솔의 에러 메시지에 **어느 필드를 못 찾았는지** 그대로 나옵니다. 필드명 오타, Getter 누락 순으로 확인하세요.

**② 값이 null인 객체에 점(.)을 찍으면 에러**

```html
<!-- intro가 null이면 500 에러 -->
<span th:text="${intro.name}"></span>

<!-- 안전 탐색 연산자 ?. : null이면 그냥 빈 값 출력 -->
<span th:text="${intro?.name}"></span>
```

**③ HTML을 고쳤는데 화면이 그대로 — 캐시/빌드 문제**

2-3에서 설명한 캐시 설정을 확인하고, IntelliJ라면 `Ctrl + F9`(빌드) 후 새로고침하세요.
그래도 안 되면 서버 재시작이 가장 확실합니다.

**④ `return "list"`인데 템플릿을 못 찾음**

```
TemplateInputException: Error resolving template [list]
```

파일이 `templates/` 폴더 바로 아래 있는지, 파일명이 정확히 `list.html`인지 확인하세요.
`@Controller` 대신 `@RestController`를 붙였을 때도 템플릿을 찾지 않고 문자열 "list"를 그대로 응답하니 주의([04 문서](../SpringBoot/04_헷갈리기_쉬운_개념.md) 참고).

---

## 8. 실무 연결 — 회사의 eGovFrame(JSP)과 비교

회사 실무 스택인 eGovFrame은 화면을 타임리프가 아닌 **JSP(+JSTL)** 로 만듭니다.
문법 생김새는 다르지만 **"서버 데이터를 HTML에 끼워 넣는다"는 개념은 완전히 동일**해서,
타임리프를 이해했다면 아래 대응표만으로 JSP 코드를 읽을 수 있습니다.

| 하는 일 | 타임리프 | JSP(JSTL) |
|---|---|---|
| 값 출력 | `<td th:text="${intro.name}">` | `<td>${intro.name}</td>` |
| 반복 | `<tr th:each="intro : ${intros}">` | `<c:forEach var="intro" items="${intros}">` |
| 조건 | `<tr th:if="${...}">` | `<c:if test="${...}">` |
| URL | `th:href="@{/intro/new}"` | `href="<c:url value='/intro/new'/>"` |
| 공통 레이아웃 | Fragment (`th:replace`) | Tiles / `<jsp:include>` |

큰 차이 하나: JSP는 HTML 사이에 **별도 태그**(`<c:forEach>`)를 끼워 넣는 방식이라
서버 없이 파일을 열면 화면이 깨집니다(내추럴 템플릿이 아님).
최신 스프링부트가 JSP 대신 타임리프를 권장하는 이유 중 하나입니다.

---

## 9. 요약 치트시트

```
설정      build.gradle에 spring-boot-starter-thymeleaf 한 줄 (자동 설정)
선언      <html xmlns:th="http://www.thymeleaf.org">
위치      templates/*.html  ←  컨트롤러의 return "이름"과 연결

${...}    Model 데이터 꺼내기          ${intro.title}
@{...}    URL 만들기                  @{/intro/{id}(id=${intro.id})}
*{...}    th:object 안에서 필드 접근    *{title}

th:text   텍스트 출력(이스케이프 O)     th:text="${intro.name}"
th:each   반복                        th:each="intro : ${intros}"
th:if     조건부 렌더링                th:if="${#lists.isEmpty(intros)}"
th:href   링크                        th:href="@{/intro/new}"
th:action 폼 전송 주소                 th:action="@{/intro}"
[[...]]   텍스트 중간 인라인 출력       [[${intro.name}]]
#도우미    유틸리티                    #temporals.format(...), #lists.isEmpty(...)
```

## 참고 자료

- [Thymeleaf 공식 튜토리얼 (3.1)](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html) — 가장 정확한 1차 자료 (영어)
- [Thymeleaf + Spring 연동 문서](https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html) — 폼 바인딩 등 심화
- 실습 프로젝트의 실제 사용 예: [list.html](../SpringBoot/샘플/intro/src/main/resources/templates/list.html) · [form.html](../SpringBoot/샘플/intro/src/main/resources/templates/form.html) · [detail.html](../SpringBoot/샘플/intro/src/main/resources/templates/detail.html)
