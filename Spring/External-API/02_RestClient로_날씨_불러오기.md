# 02. RestClient로 날씨 불러오기 — 코드 한 줄씩

> **이 문서에서 배우는 것**
> - `RestClient` 를 Bean으로 준비하기 (baseUrl, 타임아웃)
> - 외부 JSON을 담을 **DTO** 만들기 (`record`, `@JsonIgnoreProperties`, `@JsonProperty`)
> - 클라이언트 → 서비스 → 컨트롤러 → 화면으로 데이터가 흐르는 전 과정
> - 완성본: [샘플/weather](./샘플/weather/)

이 앱은 외부 API를 **두 번** 부릅니다. 아래 흐름을 머릿속에 그려 두고 코드를 보세요.

```
[브라우저] "Seoul"
    │  GET /?city=Seoul
    ▼
WeatherController ──▶ WeatherService ──▶ GeocodingClient ──▶ 🌐 지오코딩 API   (① 좌표 찾기)
                            │                                    "Seoul" → 위도/경도
                            │
                            └──────────▶ ForecastClient ───▶ 🌐 날씨 API        (② 날씨 받기)
                                              위도/경도 → 기온/습도/풍속
                            │
                            ▼
                       WeatherView (화면용으로 조립) ──▶ index.html
```

DB 앱의 `리포지토리`가 있던 자리에 `클라이언트(RestClient)`가 들어온 것뿐입니다.

---

## 1. build.gradle — 무엇이 있고 무엇이 없는가

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'    // RestClient가 여기 들어있음
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'  // 화면
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    // JPA·H2·MySQL 이 하나도 없습니다 — DB를 안 쓰기 때문!
}
```

- **RestClient는 별도 의존성이 아닙니다.** `spring-boot-starter-webmvc` 안의 `spring-web`에 들어 있어서,
  웹 스타터만 있으면 바로 쓸 수 있습니다.
- 데이터를 외부에서 가져오므로 **DB 관련 의존성이 전부 빠졌습니다.** (intro-jpa와 비교해 보세요)

---

## 2. RestClient 준비 — `config/RestClientConfig.java`

RestClient를 미리 만들어(Bean) 두고, 필요한 곳에서 가져다 씁니다.
외부 서버가 둘(지오코딩·날씨)이라 각 서버 주소를 박아 둔 RestClient를 **2개** 만듭니다.

```java
@Configuration
public class RestClientConfig {

    // 타임아웃: 외부 서버가 느릴 때 우리 서버가 무한정 멈추지 않도록 (실무 필수!)
    private ClientHttpRequestFactory timeoutFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));  // 연결까지 최대 3초
        factory.setReadTimeout(Duration.ofSeconds(5));     // 응답까지 최대 5초
        return factory;
    }

    @Bean
    public RestClient geocodingRestClient() {
        return RestClient.builder()
                .baseUrl("https://geocoding-api.open-meteo.com")  // 이 서버 전용
                .requestFactory(timeoutFactory())
                .build();
    }

    @Bean
    public RestClient forecastRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.open-meteo.com")            // 저 서버 전용
                .requestFactory(timeoutFactory())
                .build();
    }
}
```

- `baseUrl` 을 박아 두면, 실제 호출할 땐 뒤 경로(`/v1/search`)만 적으면 됩니다.
- **타임아웃이 왜 중요한가?** 이게 없으면 외부 서버가 응답을 안 줄 때 우리 서버 스레드가
  영원히 붙잡혀 있다가, 그런 요청이 쌓이면 **우리 서버 전체가 멈춥니다.** (03 문서에서 더)

> ⚠️ **함정 ①(부트 4.x 실측)** — 인터넷 예제에는 `RestClient.Builder`를 **주입**받아 쓰라고 나옵니다
> (`public RestClient xxx(RestClient.Builder builder)`). 그런데 **스프링부트 4.1의 `webmvc` 스타터에는
> 그 자동 Builder Bean이 없어서** `No qualifying bean of type RestClient$Builder` 오류가 납니다.
> → 위처럼 **`RestClient.builder()` (정적 메서드)** 로 직접 만들면 어느 버전에서든 동작합니다.

> ⚠️ **함정 ②(부트 4.x 실측)** — 부트 3.4의 타임아웃 헬퍼(`ClientHttpRequestFactorySettings`,
> `ClientHttpRequestFactoryBuilder`)는 **부트 4.1의 webmvc 스타터 클래스패스에 없습니다.**
> 그래서 spring-web 기본 제공 `SimpleClientHttpRequestFactory`로 타임아웃을 걸었습니다. (버전에 안전)

---

## 3. DTO — 외부 JSON을 담을 그릇

외부 API가 준 JSON을 자바 객체로 받으려면, **JSON 모양을 닮은 클래스**가 필요합니다.
값만 담는 용도라 `record`(불변 데이터 객체)가 딱 맞습니다.

### 3-1. 지오코딩 응답 — 배열과 중첩

실제 응답:
```json
{ "results": [ { "name": "서울특별시", "latitude": 37.566, "longitude": 126.9784, "country": "대한민국" } ] }
```

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<GeoResult> results) { }

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoResult(
        String name, double latitude, double longitude,
        String country, String admin1, String timezone) { }
```

- **`@JsonIgnoreProperties(ignoreUnknown = true)` — 외부 API 다룰 때 거의 필수.**
  응답에는 우리가 안 쓰는 필드(`generationtime_ms`, `elevation`, `population`…)가 잔뜩 옵니다.
  이 어노테이션이 없으면 "모르는 필드"를 만났을 때 **오류가 납니다.**
  "필요한 것만 받고 나머지는 무시"하겠다는 선언입니다.
- record의 **부품 이름(latitude)을 JSON 필드 이름과 똑같이** 맞추면 Jackson이 알아서 채워 줍니다.

### 3-2. 날씨 응답 — 이름이 다를 땐 `@JsonProperty`

실제 응답:
```json
{ "timezone": "Asia/Seoul",
  "current": { "temperature_2m": 25.7, "relative_humidity_2m": 94, "weather_code": 63, "wind_speed_10m": 7.9 } }
```

JSON은 `temperature_2m`(밑줄), 자바는 `temperature`(낙타) 쓰는 게 관례라 **이름이 다릅니다.**
이럴 때 `@JsonProperty`로 "이 JSON 이름과 연결하라"고 알려 줍니다.

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record Current(
        String time,
        @JsonProperty("temperature_2m")      double temperature,
        @JsonProperty("relative_humidity_2m") int    humidity,
        @JsonProperty("weather_code")         int    weatherCode,
        @JsonProperty("wind_speed_10m")       double windSpeed) { }
```

> 📌 **JSON ↔ 자바 변환은 누가?** — **Jackson** 라이브러리입니다(웹 스타터에 기본 포함).
> 우리가 `body(Current.class)` 라고만 하면, Jackson이 JSON 글자를 읽어 record를 채워 줍니다.
> Front-Back 모듈에서 우리 객체를 JSON으로 **내보낼 때**도 이 Jackson이 일했습니다. 방향만 반대입니다.

---

## 4. 클라이언트 — 실제로 API를 부르는 코드

### 4-1. 좌표 찾기 — `client/GeocodingClient.java`

```java
@Component
public class GeocodingClient {

    private final RestClient geocodingRestClient;

    // 아까 만든 Bean 2개 중 "geocodingRestClient"를 콕 집어 주입 (@Qualifier)
    public GeocodingClient(@Qualifier("geocodingRestClient") RestClient geocodingRestClient) {
        this.geocodingRestClient = geocodingRestClient;
    }

    public Optional<GeoResult> findCity(String cityName) {
        GeocodingResponse response = geocodingRestClient.get()      // GET 요청
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", cityName)   // ?name=Seoul
                        .queryParam("count", 1)         // &count=1
                        .queryParam("language", "ko")   // &language=ko (결과를 한글로)
                        .build())
                .retrieve()                              // 요청을 실제로 쏨
                .body(GeocodingResponse.class);          // 응답 JSON → 객체로 변환

        if (response == null || response.results() == null || response.results().isEmpty()) {
            return Optional.empty();                     // 못 찾음
        }
        return Optional.of(response.results().get(0));   // 가장 잘 맞는 1건
    }
}
```

**RestClient의 문장 구조**를 눈에 익히세요 — 영어 문장처럼 읽힙니다:
```
restClient . get() . uri(...) . retrieve() . body(타입)
   (누가)    (뭘)    (어디에)    (보내고)     (받아서 이 타입으로)
```

- **`@Qualifier("geocodingRestClient")`** — 같은 타입(RestClient) Bean이 둘이라, 이름으로 콕 집어야 합니다.
  안 그러면 "둘 중 뭘 줘야 하지?" 하고 스프링이 오류를 냅니다.
- **`Optional<GeoResult>` 반환** — "결과가 없을 수도 있다"를 타입으로 정직하게 표현했습니다.
  없는 도시면 `Optional.empty()`. (서비스가 이걸 보고 에러 처리)

### 4-2. 날씨 받기 — `client/ForecastClient.java`

```java
@Component
public class ForecastClient {

    private final RestClient forecastRestClient;

    public ForecastClient(@Qualifier("forecastRestClient") RestClient forecastRestClient) {
        this.forecastRestClient = forecastRestClient;
    }

    public ForecastResponse getCurrent(double latitude, double longitude) {
        return forecastRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current",
                            "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m")  // 받고 싶은 항목들
                        .queryParam("timezone", "auto")   // 그 지역 시간대로 시각 표시
                        .build())
                .retrieve()
                .body(ForecastResponse.class);
    }
}
```

> 📌 **쿼리 파라미터를 문자열로 이어붙이지 마세요.** `"?name=" + cityName` 처럼 손으로 붙이면
> 도시명에 공백·특수문자가 있을 때 URL이 깨집니다. `queryParam(...)`을 쓰면 스프링이
> **URL 인코딩을 알아서** 해 줍니다. (예: 공백 → `%20`) — 안전하고 실수가 없습니다.

---

## 5. 서비스 — 두 호출을 엮는 지휘자 `service/WeatherService.java`

```java
@Service
public class WeatherService {

    private final GeocodingClient geocodingClient;
    private final ForecastClient  forecastClient;
    // 생성자 주입 (생략)

    public WeatherView getWeather(String city) {
        // ① 도시명 → 좌표. 없으면 예외를 던져 컨트롤러가 안내하게 함
        GeoResult geo = geocodingClient.findCity(city)
                .orElseThrow(() -> new CityNotFoundException(city));

        // ② 좌표 → 현재 날씨
        ForecastResponse forecast = forecastClient.getCurrent(geo.latitude(), geo.longitude());
        Current now = forecast.current();

        // 숫자 날씨코드(63)를 한글 설명+이모지("비", "🌧️")로 번역
        WeatherCode.Description desc = WeatherCode.of(now.weatherCode());

        // 화면에 뿌리기 좋게 하나로 조립해서 반환
        return new WeatherView(geo.name(), geo.country(),
                now.temperature(), now.humidity(), now.windSpeed(),
                desc.text(), desc.emoji(), now.time(), forecast.timezone());
    }
}
```

- **호출 이어붙이기(chaining):** ①의 결과(좌표)를 ②의 입력으로 넘깁니다. 실무에서 아주 흔한 패턴입니다
  (예: 로그인 API로 토큰 받기 → 그 토큰으로 정보 API 부르기).
- **"외부 응답 DTO"와 "화면용 객체"를 분리:** `Current`(외부에서 온 날것)를 화면까지 그대로 끌고
  가지 않고, 화면에 필요한 것만 담은 **`WeatherView`** 로 갈아탑니다. 외부 API가 스펙을 바꿔도
  화면 코드는 안 흔들리게 하는 **방어막**입니다.
- **날씨코드 번역(`WeatherCode`):** 외부 API는 날씨를 숫자(0=맑음, 63=비)로 줍니다. 사람이 볼 수 있게
  바꾸는 "코드 → 값" 변환은 외부 API를 쓸 때 자주 필요합니다(은행 거래코드, 공공 API 상태코드 등).

---

## 6. 컨트롤러 & 화면 — 여기는 지금까지와 똑같음

`controller/WeatherController.java` — 기존 `@Controller`와 완전히 동일한 방식입니다.

```java
@GetMapping("/")
public String index(@RequestParam(required = false) String city, Model model) {
    if (city == null || city.isBlank()) return "index";   // 검색 전이면 폼만

    model.addAttribute("city", city);
    try {
        model.addAttribute("weather", weatherService.getWeather(city));
    } catch (CityNotFoundException e) {
        model.addAttribute("error", e.getMessage());        // "그런 도시 없음"
    } catch (RestClientException e) {
        model.addAttribute("error", "날씨 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.");  // 외부 장애
    }
    return "index";
}
```

`templates/index.html` — Thymeleaf 문법도 그대로입니다.

```html
<div th:if="${weather}" class="card">
    <h2 th:text="${weather.cityName}">도시</h2>
    <span th:text="${weather.temperature}">0</span>℃
    <span th:text="${weather.description}">날씨</span>
</div>
<div th:if="${error}" class="alert alert-warning" th:text="${error}"></div>
```

> **핵심 메시지:** 데이터 출처가 DB에서 외부 API로 바뀌었을 뿐, **컨트롤러와 화면은 하나도 안 바뀝니다.**
> 3계층 구조 덕분에 "데이터를 어디서 가져오는가"가 서비스 아래에 감춰져 있기 때문입니다.

---

## 7. 실행하고 눈으로 확인

```powershell
cd 샘플/weather
.\gradlew.bat bootRun
```

`http://localhost:8080` → `Seoul` 입력. 아래처럼 나오면 성공입니다. (값은 실제 실행 결과)

| 검색 폼 | 결과 카드 |
|---|---|
| ![폼](./images/weather_01_form.png) | ![결과](./images/weather_02_result.png) |

> 📌 **외부로 뭘 보내는지 눈으로 보고 싶다면** — `application.properties`의 다음 줄 주석을 푸세요.
> RestClient가 보내는 요청 URL이 콘솔에 찍힙니다.
> `logging.level.org.springframework.web.client.RestClient=DEBUG`

---

## 정리

- RestClient 사용은 `get().uri(...).retrieve().body(타입)` 한 문장으로 끝납니다.
- 받은 JSON은 **DTO(record)** 로 받고, 안 쓰는 필드는 `@JsonIgnoreProperties`로 무시,
  이름이 다르면 `@JsonProperty`로 연결합니다.
- 외부 응답 DTO ≠ 화면용 객체. 서비스에서 **화면용으로 갈아타** 외부 변화로부터 화면을 지킵니다.
- 여기까지는 "잘 될 때"의 코드입니다. 다음 문서에서 **안 될 때**(에러·타임아웃)와 **키 보안**을 배웁니다.

**다음** → [03. 에러 처리와 실무 고려사항](./03_에러처리와_실무_고려사항.md)
