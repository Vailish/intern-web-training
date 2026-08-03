# 스프링부트 → 레거시(eGovFrame / JSP) 넘어가기 — 브릿지 가이드

여러분은 **스프링부트**로 배웠지만, 회사(SI/공공)에서 처음 열게 될 프로젝트는
**eGovFrame(전자정부 표준프레임워크) 기반의 "옛날 방식" Spring MVC + JSP** 인 경우가 많습니다.
이 문서는 **부트만 아는 상태에서 레거시 프로젝트를 처음 만났을 때의 충격을 줄이는** 짧은 지도입니다.

> 💡 **핵심 안심 메시지**: eGovFrame은 **새로운 프레임워크가 아니라 "Spring + 검증된 라이브러리 + 관례 + 코드 생성기"** 입니다.
> 즉 여러분이 배운 **Spring 지식의 대부분이 그대로 통합니다.** 새로 적응할 건 딱 몇 가지뿐입니다.

---

## 1. eGovFrame이란 — 한 문장

> **정부·공공 SI에서 표준으로 쓰라고 만든, Spring 기반의 "세트 메뉴" + 코드 생성기.**

- 바닥은 **Spring(주로 Spring MVC)**. 여기에 MyBatis, 로깅, 보안 등 **검증된 라이브러리를 묶어** 버전을 고정해 둔 것.
- **개발 환경(이클립스 기반 IDE)** 과 **코드 생성기(CRUD 자동 생성)** 를 함께 제공.
- 최신 라인은 **Spring Boot 3 / Spring 6 기반(v5.x)** 도 있지만, **현장 운영 시스템 상당수는 구버전(비-부트 + JSP)** 입니다.

→ 그래서 여러분은 **"구버전(레거시) 유지보수 + 신규(부트) 구축"** 둘 다 만날 수 있습니다.

---

## 2. 새로 적응할 것은 딱 3가지

부트로 배운 것 중 **대부분은 그대로**입니다(DI, `@Controller/@Service`, MVC 흐름, JPA/MyBatis 개념 등).
레거시에서 **달라지는 핵심은 아래 3가지**뿐입니다.

### 차이 ① 화면: Thymeleaf → **JSP / JSTL**
부트 교육은 Thymeleaf를 썼지만, 레거시 화면단은 **JSP + JSTL**입니다. 개념(서버가 HTML을 그려 내려줌)은 같고 문법만 다릅니다.

| 하는 일 | Thymeleaf (배운 것) | JSP + JSTL (레거시) |
|---|---|---|
| 값 출력 | `<span th:text="${intro.name}">` | `<c:out value="${intro.name}"/>` 또는 `${intro.name}` |
| 반복 | `<tr th:each="i : ${intros}">` | `<c:forEach var="i" items="${intros}">` |
| 조건 | `<div th:if="${intro != null}">` | `<c:if test="${intro != null}">` |
| 링크 URL | `<a th:href="@{/intro/{id}(id=${i.id})}">` | `<a href="<c:url value='/intro/${i.id}'/>">` |
| 폼 전송 값 | `th:action` + `name` | 일반 `<form action=".." method="post">` + `name` |
| 상단 선언 | (불필요) | `<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>` 필요 |

> 모델에 값을 담아(`model.addAttribute`) 화면에서 `${...}` 로 꺼내 쓰는 **원리는 완전히 동일**합니다.

### 차이 ② 설정: 부트 오토컨피그 → **XML 설정 파일들**
부트는 `application.properties` + 어노테이션으로 자동 설정됐지만, 레거시는 **XML로 직접** 설정합니다.

| 부트(배운 것) | 레거시 eGovFrame | 역할 |
|---|---|---|
| (자동) | `web.xml` | 서블릿/필터/리스너 등록 (앱의 진입 설정) |
| `@SpringBootApplication` 자동 스캔 | `root-context.xml` | 서비스·리포지토리·DB 등 공용 빈 |
| MVC 자동 설정 | `servlet-context.xml` | 컨트롤러·뷰리졸버 등 웹 관련 빈 |
| `application.properties` | `*.properties` + XML `<bean>` | DB 접속 등 |

> 처음 레거시 프로젝트를 열면 "XML이 왜 이렇게 많지?" 싶지만, **부트가 자동으로 해 주던 일을 눈에 보이게 적어 둔 것**뿐입니다. 오히려 "부트가 뒤에서 뭘 했는지"를 이해하는 계기가 됩니다.

### 차이 ③ 실행·배포: 내장 톰캣 JAR → **외장 톰캣 WAR**
| 부트(배운 것) | 레거시 |
|---|---|
| `java -jar app.jar` (톰캣이 앱 안에 내장) | `app.war` 를 **외장 톰캣/WAS에 올려** 실행 |
| 포트 8080 바로 실행 | 톰캣의 `webapps/` 에 배포 후 기동 |

> 원리는 [참고자료: 웹서버·WAS 동작원리 PDF](../../99_참고자료/) 에서 다룬 "WAS가 앱을 구동한다"와 같습니다.

**데이터 접근(MyBatis)** 은 이미 [Database 모듈의 intro-mybatis](../Database/샘플/intro-mybatis/)에서 배웠습니다 — 레거시에서도 거의 그대로입니다(XML 매퍼 + `#{}` 파라미터).

---

## 3. eGovFrame 직접 해 보기 (선택)

풀 실습 모듈을 따로 두지 않은 이유: **설치 후 "코드 생성기"로 CRUD 샘플이 바로 나오기** 때문입니다.
궁금하면 아래 순서로 30분이면 체험할 수 있습니다.

1. [전자정부 표준프레임워크 포털](https://www.egovframe.go.kr) 에서 **개발환경(eGovFrame IDE)** 다운로드·설치.
2. `File → New → eGovFrame Web Project` 로 프로젝트 생성.
3. **CRUD 코드 생성**(테이블/모델 지정 → Controller/Service/DAO/JSP 자동 생성).
4. 생성된 코드를 열어, 위 **차이 ①②③** 관점에서 부트와 비교해 보기.
5. (선택) 무상 **정기교육**(egovframe.go.kr)으로 수료증 확보 — 이력서에 활용 가능(자격증은 아님).

> 자세한 취업 관점 정리는 [참고자료: 채용공고 비교분석(공공/SI 트랙)](../../99_참고자료/) 참고.

---

## 4. 요약 — 무엇을 안 해도 되고, 무엇만 새로 익히나

| 이미 아는 것(그대로 통함) | 새로 익히면 되는 것 |
|---|---|
| DI, `@Controller/@Service/@Repository`, MVC 요청 흐름 | **JSP/JSTL 문법** |
| Model에 담아 화면에 전달하는 원리 | **XML 설정 구조**(web.xml / context.xml) |
| MyBatis(XML 매퍼, `#{}`), SQL | **WAR + 외장 톰캣 배포** |
| Git, 기본 자바 | (코드 생성기 사용법 — 금방 익힘) |

**한 문장 결론:** eGovFrame이 두려워서 부트를 안 배운 게 아니라, **부트로 원리를 깊게 배웠기 때문에 레거시는 "문법·설정 방식만 다른 같은 Spring"** 으로 보이게 됩니다. 위 3가지 차이만 알고 가면 충분합니다.

---

## 🏢 실무 팁
- 레거시 프로젝트를 처음 받으면 **`web.xml` → `servlet-context.xml` → 컨트롤러 → JSP** 순서로 "요청이 흐르는 길"을 한 번 따라가 보세요. 구조가 한눈에 들어옵니다.
- 화면이 안 나오면 열에 아홉은 **JSTL taglib 선언 누락**이나 **뷰리졸버 경로(prefix/suffix)** 문제입니다.
- 신규 기능은 최신 라인(부트 기반)으로 가는 추세이니, **부트 역량은 계속 살려 두세요.**
