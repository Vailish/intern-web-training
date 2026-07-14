# 02. REST API 기본 개념 — 그리고 intro-jpa를 API 서버로 바꾸기

> **이 문서에서 배우는 것**
> - API, REST, JSON이 정확히 무엇인지
> - HTTP 메서드(GET/POST/PUT/DELETE)와 상태 코드(200/201/204/404...)
> - `@Controller` → `@RestController`: intro-jpa를 **화면 없는 API 서버**로 바꾸기
> - 완성본: [샘플/intro-api](./샘플/intro-api/)

---

## 1. API란? — "프로그램끼리의 창구"

**API(Application Programming Interface)** 는 프로그램이 다른 프로그램의 기능을
쓸 수 있게 열어 둔 창구입니다. 우리 경우:

- React(브라우저의 프로그램)가
- 스프링부트 서버(다른 프로그램)의 "자기소개서 목록을 조회하는 기능"을
- `GET http://localhost:8080/api/intros` 라는 **정해진 약속**으로 호출합니다.

이 약속을 HTTP와 JSON 위에서, 아래 나올 규칙(REST 스타일)으로 정한 것이
**REST API**입니다. 오늘날 웹 API의 사실상 표준입니다.

## 2. JSON — 데이터를 주고받는 공용어

**JSON(JavaScript Object Notation)** 은 데이터를 글자로 표현하는 형식입니다.
자바 객체도, 파이썬 객체도, JS 객체도 JSON으로 바꿔 주고받을 수 있어서
서로 다른 언어로 만든 프로그램끼리의 공용어 역할을 합니다.

```json
{
  "id": 1,
  "name": "김인턴",
  "title": "안녕하세요, 성장하는 개발자 김인턴입니다",
  "createdAt": "2026-07-14T09:48:00"
}
```

- `{ }` 객체, `[ ]` 배열, `"키": 값` 쌍 — JS 객체 문법과 거의 같습니다.
- 스프링에서는 컨트롤러가 자바 객체를 반환하면 **Jackson이라는 라이브러리가
  getter를 읽어 JSON으로 자동 변환**합니다. 반대 방향(JSON → 객체)도 자동입니다.

## 3. HTTP 복습 — 메서드와 상태 코드

### 메서드: "무엇을 하고 싶은가"

| 메서드 | 의미 | CRUD |
|---|---|---|
| GET | 조회 (데이터를 바꾸지 않음) | Read |
| POST | 생성 | Create |
| PUT | 수정 (전체 교체) | Update |
| DELETE | 삭제 | Delete |

intro-jpa에서는 GET/POST만 썼습니다 — **HTML `<form>`이 GET/POST밖에 못 보내기
때문**이었죠. 그래서 삭제도 `POST /intro/{id}/delete`처럼 URL에 동작을 적어 흉내
냈습니다. JavaScript의 `fetch()`는 PUT/DELETE를 제대로 보낼 수 있어서, 이제 HTTP를
설계된 의도대로 쓸 수 있습니다.

### 상태 코드: "결과가 어떻게 됐는가"

응답의 첫머리에 실려 오는 세 자리 숫자입니다. 우리 API가 쓰는 것만 추리면:

| 코드 | 의미 | 우리 API에서 |
|---|---|---|
| **200** OK | 성공 | 조회/수정 성공 |
| **201** Created | 만들었음 | 등록 성공 |
| **204** No Content | 성공, 돌려줄 내용 없음 | 삭제 성공 |
| **400** Bad Request | 요청이 이상함 | 본문 JSON이 깨졌을 때 등 |
| **404** Not Found | 그런 거 없음 | 존재하지 않는 id 조회 |
| **500** Internal Server Error | 서버가 고장 | 처리 중 예외 발생 |

> 📌 **왜 코드를 구분해 줘야 하나?** — 프론트가 상황마다 다른 안내를 해야 하기
> 때문입니다. 404면 "삭제된 글입니다", 500이면 "잠시 후 다시 시도해 주세요".
> 서버가 전부 200으로 응답해 버리면 프론트는 성공/실패조차 구분할 수 없습니다.

## 4. REST 스타일 URL 설계 — 자원 중심으로

REST의 핵심 규칙: **URL은 자원(명사)만 가리키고, 동작은 메서드로 구분한다.**

| 하고 싶은 일 | intro-jpa (동작이 URL에) | intro-api (REST 스타일) |
|---|---|---|
| 목록 조회 | `GET /` | `GET /api/intros` |
| 등록 | `POST /intro` | `POST /api/intros` |
| 한 건 조회 | `GET /intro/{id}` | `GET /api/intros/{id}` |
| 수정 | `POST /intro/{id}/edit` | `PUT /api/intros/{id}` |
| 삭제 | `POST /intro/{id}/delete` | `DELETE /api/intros/{id}` |
| 작성 폼 화면 | `GET /intro/new` | **없음!** 화면은 React의 몫 |

- URL은 `intros`(자기소개서들) 하나뿐인데 메서드에 따라 다른 일이 됩니다.
- `/edit`, `/delete` 같은 동사가 사라졌습니다.
- 관례상 자원 이름은 복수형(intros)을 씁니다.
- `/api`를 앞에 붙여 "여기는 데이터 창구"임을 표시합니다(관례).

## 5. 코드로: @Controller → @RestController

[샘플/intro-api](./샘플/intro-api/)를 열어 intro-jpa와 나란히 비교하며 보세요.
**바뀐 것은 컨트롤러 계층뿐**입니다. 핵심만 추리면:

### 5-1. 반환값이 "화면 이름"에서 "데이터"로

```java
// intro-jpa: 반환한 문자열은 templates/list.html을 가리켰습니다
@Controller
public class IntroController {
    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("intros", introService.findAll());
        return "list";
    }
}

// intro-api: 반환한 객체가 그대로 JSON이 되어 나갑니다
@RestController                  // = @Controller + 반환값을 JSON으로!
@RequestMapping("/api/intros")   // 공통 URL 앞부분
public class IntroApiController {
    @GetMapping
    public List<Intro> list() {
        return introService.findAll();   // Model도, 템플릿 이름도 필요 없음
    }
}
```

### 5-2. 입력도 JSON으로 — @RequestParam → @RequestBody

```java
// intro-jpa: 폼 필드를 하나씩 받았습니다
public String create(@RequestParam String name,
                     @RequestParam String title,
                     @RequestParam String content) { ... }

// intro-api: 요청 본문의 JSON을 객체 하나로 받습니다
public ResponseEntity<Intro> create(@RequestBody IntroRequest request) {
    Intro saved = introService.create(request.getName(), request.getTitle(), request.getContent());
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);  // 201 + 저장 결과
}
```

- `IntroRequest`는 name/title/content만 담는 작은 클래스(**DTO**)입니다.
  엔티티를 직접 받으면 요청에 `"id": 999`를 끼워 넣는 장난까지 받아 주게 되므로,
  "클라이언트가 보내도 되는 값"만 담은 별도 클래스를 만듭니다.
- `redirect:/`가 사라졌습니다 — 저장 후 어느 화면으로 갈지는 이제 **프론트가** 정합니다.

### 5-3. 실패도 정확하게 — 예외를 404로 번역

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", e.getMessage()));
}
```

서비스가 "존재하지 않는 id"라며 던진 예외를 그냥 두면 500(서버 고장)으로 나갑니다.
"서버가 고장난 것"과 "찾는 데이터가 없는 것"은 다른 상황이므로 404로 바꿔 응답합니다.

### 5-4. 그 외 달라진 점

| 항목 | 내용 |
|---|---|
| build.gradle | `spring-boot-starter-thymeleaf` **삭제** — 템플릿 엔진이 필요 없음 |
| templates/, static/ | **삭제** — 화면 담당이 없어졌으므로 |
| Service/Repository/Entity | **변경 없음** (패키지 이름만 introapi로) |
| WebConfig.java | **추가** — CORS 설정. 04 문서에서 다룹니다 |

## 6. 직접 확인해 보기

```powershell
cd 샘플/intro-api
.\gradlew.bat bootRun
```

**① 브라우저로 GET** — `http://localhost:8080/api/intros` 접속.
HTML 대신 JSON 텍스트가 그대로 보이면 성공입니다. "화면 없는 서버"를 눈으로 확인하세요.
(글이 없으면 `[]`만 보입니다)

**② PowerShell로 등록(POST)** — 브라우저 주소창은 GET만 보낼 수 있으므로 도구가 필요합니다:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/intros -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"name":"김인턴","title":"첫 API 호출","content":"PowerShell에서 보냈습니다"}'))
```

**③ 404 확인** — `http://localhost:8080/api/intros/999` 접속 →
`{"message":"존재하지 않는 자기소개서입니다. id=999"}`

> ⚠️ **함정: Windows에서 curl로 한글 보내기** — `curl -d '{"name":"김인턴"...}'`처럼
> 한글이 든 본문을 보내면 Windows 터미널의 문자 인코딩(CP949) 때문에 서버가
> 400 Bad Request를 반환하는 경우가 있습니다. 서버 잘못이 아닙니다!
> PowerShell에서는 위처럼 `Invoke-RestMethod` + UTF8 바이트 변환을 쓰는 것이 안전합니다.
> (04 문서부터는 브라우저의 fetch가 알아서 UTF-8로 보내 주므로 이 문제가 없습니다)

> 🏢 **eGovFrame 연결 포인트** — 실무 코드에서 메서드에 `@ResponseBody`가 붙어 있거나
> 반환 타입이 객체/Map인 컨트롤러를 보게 될 겁니다. 그게 바로 이 패턴입니다
> (`@RestController` = 클래스의 모든 메서드에 `@ResponseBody`를 붙인 것).
> JSP 화면 속에서 Ajax로 부분 데이터를 갱신할 때 이런 JSON 응답 메서드를 씁니다.

---

## 정리

- 서버는 이제 **JSON 데이터만** 응답합니다. 화면·이동은 전부 프론트 책임.
- URL은 자원(명사), 동작은 메서드(GET/POST/PUT/DELETE), 결과는 상태 코드로.
- 백엔드 준비 끝! 이제 이 API를 사용할 프론트엔드(React)를 만들러 갑니다.

**다음** → [03. React 시작하기](./03_React_시작하기.md)
