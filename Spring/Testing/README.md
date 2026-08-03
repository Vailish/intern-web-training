# 테스트 코드 작성하기 — JUnit5 · Mockito · 슬라이스 · 통합 테스트

지금까지 만든 앱들은 **브라우저로 직접 눌러 보며** 동작을 확인했습니다.
하지만 기능이 늘어날수록 매번 손으로 확인하는 건 느리고 실수가 생깁니다.
이번 모듈에서는 **코드로 코드를 검증하는 자동화 테스트**를 배웁니다.

> 💡 **왜 지금 배우나?** 채용 시장 조사에서 **테스트 코드는 신입이 보강하면 좋은 1순위**로 꼽혔습니다.
> "테스트를 짤 줄 안다"는 것은 실무 투입 준비가 됐다는 강력한 신호입니다.
> (참고: [99_참고자료/신입개발자_취업준비_로드맵](../../99_참고자료/))

---

## 이 모듈에서 배우는 것

- **테스트를 왜 작성하는가**, 그리고 **테스트 피라미드**(무엇을 얼마나 짤지)
- 스프링에서 쓰는 **4가지 테스트 유형**과 각각의 애노테이션
- 실제로 통과하는 테스트 코드 (완성본: [샘플/intro-test](./샘플/intro-test/))

이 샘플은 [Database 모듈의 intro-jpa](../Database/샘플/intro-jpa/)와 **똑같은 자기소개서 CRUD 앱**입니다.
익숙한 앱에 **테스트만 새로 붙여** 배우는 것이 목적입니다.

---

## 1. 테스트를 왜 작성하는가

- **회귀 방지**: 새 기능을 넣다가 기존 기능이 깨지는 것(회귀)을 자동으로 잡아냅니다.
- **빠른 피드백**: 서버를 띄우고 브라우저로 클릭하는 대신, 몇 초 만에 수십 개를 검증합니다.
- **안전한 리팩토링**: 테스트가 있으면 "고쳐도 안 깨진다"는 확신으로 코드를 개선할 수 있습니다.
- **문서 역할**: 잘 쓴 테스트는 "이 코드가 무엇을 해야 하는가"를 보여 주는 살아있는 명세입니다.

---

## 2. 테스트 피라미드 — 무엇을 얼마나 짤까

```
        /\        통합 테스트 (느림, 적게)   ← 앱 전체를 띄워 "연결"을 확인
       /  \
      /----\      슬라이스 테스트 (중간)     ← 계층 하나만 잘라서 확인
     /      \
    /--------\    단위 테스트 (빠름, 많이)   ← 클래스 하나만 순수하게 확인
```

**아래로 갈수록 빠르고 많이**, 위로 갈수록 느리고 적게 짜는 것이 원칙입니다.
단위 테스트로 세부 로직을 촘촘히 검증하고, 통합 테스트로 "전체가 이어지는지"만 확인합니다.

---

## 3. 4가지 테스트 유형 (이 샘플의 구성)

| 유형 | 애노테이션 | 스프링 로딩 | DB | 무엇을 검증 | 파일 |
|---|---|---|---|---|---|
| **단위** | `@ExtendWith(MockitoExtension.class)` | ❌ 안 뜸 | ❌ | 서비스 로직만 (리포지토리는 가짜) | [IntroServiceTest](./샘플/intro-test/src/test/java/com/example/introtest/service/IntroServiceTest.java) |
| **리포지토리 슬라이스** | `@DataJpaTest` | JPA만 | 인메모리 H2 | 실제 SQL·조회·정렬 | [IntroRepositoryTest](./샘플/intro-test/src/test/java/com/example/introtest/repository/IntroRepositoryTest.java) |
| **컨트롤러 슬라이스** | `@WebMvcTest` + `MockMvc` | 웹 계층만 | ❌ (서비스는 가짜) | URL 매핑·뷰 이름·리다이렉트·모델 | [IntroControllerTest](./샘플/intro-test/src/test/java/com/example/introtest/controller/IntroControllerTest.java) |
| **통합** | `@SpringBootTest` + `MockMvc` | 앱 전체 | 인메모리 H2 | 컨트롤러→서비스→DB 전 과정 | [IntroIntegrationTest](./샘플/intro-test/src/test/java/com/example/introtest/IntroIntegrationTest.java) |

각 파일의 **한글 주석이 강의 노트**입니다. 파일을 열어 순서대로 읽어 보세요.

### 핵심 도구
- **JUnit5**: 테스트를 정의(`@Test`)하고 실행하는 뼈대.
- **AssertJ**: `assertThat(값).isEqualTo(...)` 처럼 읽기 쉬운 검증 문법.
- **Mockito**: 진짜 대신 **가짜(Mock)** 객체를 만들어, 검사 대상만 떼어내 테스트.
- **MockMvc**: 실제 톰캣 없이 **가짜 HTTP 요청**을 컨트롤러에 보내는 도구.

---

## 4. 실행하기

```bash
cd Spring/Testing/샘플/intro-test
./gradlew test
```

- 콘솔에 `BUILD SUCCESSFUL` 이 뜨면 전부 통과입니다. (이 샘플: **테스트 10개**)
- 상세 결과 리포트: `build/reports/tests/test/index.html` 을 브라우저로 열면 통과/실패가 보기 좋게 나옵니다.
- 실패를 일부러 경험해 보려면, 서비스 코드의 값을 바꾼 뒤 다시 `./gradlew test` 를 돌려 보세요.

---

## 5. ⚠️ 스프링부트 4에서 바뀐 것 (함정)

> ⚠️ **함정 1 — 테스트 슬라이스 애노테이션의 패키지가 이동했습니다.**
> 스프링부트 4는 모듈이 잘게 쪼개지면서 슬라이스 애노테이션 위치가 바뀌었습니다.
> 인터넷 예제(부트 3.x 기준)의 import 를 그대로 쓰면 **"cannot find symbol"** 이 납니다.
> | 애노테이션 | 옛 패키지(부트 3.x) | **새 패키지(부트 4.x)** |
> |---|---|---|
> | `@WebMvcTest` `@AutoConfigureMockMvc` | `...test.autoconfigure.web.servlet` | `org.springframework.boot.webmvc.test.autoconfigure` |
> | `@DataJpaTest` | `...test.autoconfigure.orm.jpa` | `org.springframework.boot.data.jpa.test.autoconfigure` |
> 의존성도 기술별로 나뉩니다: `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`.

> ⚠️ **함정 2 — `@MockBean` 은 `@MockitoBean` 으로 바뀌었습니다.**
> 스프링 컨텍스트의 빈을 가짜로 교체할 때 쓰던 `@MockBean`(부트 3.4에서 지원 종료)은
> 이제 **`@MockitoBean`**(`org.springframework.test.context.bean.override.mockito.MockitoBean`)을 씁니다.

> ⚠️ **함정 3 — 테스트는 인메모리 H2로.**
> `bootRun` 서버가 켜진 채로 `./gradlew test` 를 돌리면, 파일 H2(`./data`)를 서로 잡으려다 충돌합니다.
> 그래서 `src/test/resources/application.properties` 에서 **인메모리 H2**로 덮어씁니다.
> (테스트는 매번 깨끗한 DB에서 시작해야 결과가 일정합니다.)

---

## 🏢 실무(eGovFrame) 연결 포인트

회사 실무(SI/공공)에서도 테스트는 갈수록 중요해지고 있습니다. 스택은 달라도 **개념은 그대로** 이어집니다.
- eGovFrame(Spring MVC)에서도 **JUnit5 + Mockito + Spring Test(MockMvc)** 를 동일하게 씁니다.
- MyBatis 기반이면 `@MyBatisTest`(마이바티스 슬라이스)로 매퍼 SQL을 검증합니다. (`@DataJpaTest`의 MyBatis 판)
- 즉 여기서 익힌 **"단위 → 슬라이스 → 통합"의 사고방식**은 프레임워크가 바뀌어도 재사용됩니다.

---

## 다음 단계

테스트로 코드에 안전망을 씌웠다면, 이제 이 앱을 **취업용 포트폴리오로 발전**시킬 차례입니다.
→ [캡스톤/포트폴리오 가이드](../Capstone/) 로 이어집니다.
