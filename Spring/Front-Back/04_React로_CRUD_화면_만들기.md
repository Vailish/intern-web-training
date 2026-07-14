# 04. React로 CRUD 화면 만들기 — intro-react 완성

> **이 문서에서 배우는 것**
> - `fetch()`로 백엔드 REST API 호출하기 (async/await, 에러 처리)
> - **CORS 에러** — 분리 구조의 통과의례를 직접 겪고 해결하기
> - `useEffect`로 화면에 데이터 채우기, state로 화면 전환하기
> - 완성본: [샘플/intro-react](./샘플/intro-react/) — **모든 파일에 학습용 주석**이 달려 있습니다

이 문서는 완성본 코드를 "읽는 순서대로" 안내합니다. 문서를 옆에 두고
샘플 파일을 하나씩 열어 주석과 함께 읽는 것이 가장 좋은 학습법입니다.

---

## 1. 완성 모습과 실행

```
intro-react/src/
├── main.jsx                  ① 시작점
├── App.jsx                   ② 화면 전환 담당 (어떤 화면을 보여줄까?)
├── api/
│   └── introApi.js           ③ 백엔드 API 호출 함수 모음 (통신 전담)
└── components/
    ├── IntroList.jsx         ④ 목록 화면
    ├── IntroDetail.jsx       ⑤ 상세 화면
    └── IntroForm.jsx         ⑥ 작성/수정 화면 (하나로 겸용!)
```

터미널 2개로 실행합니다 (⚠️ 백엔드 먼저!):

```powershell
# 터미널 1: 백엔드            # 터미널 2: 프론트엔드
cd 샘플/intro-api             cd 샘플/intro-react
.\gradlew.bat bootRun         npm install   (최초 1회)
                              npm run dev
```

`http://localhost:5173` 접속 → intro-jpa와 똑같은 화면이 보이면 성공.
**F12 → Network 탭**을 열고 화면을 조작해 보세요. HTML이 아니라
`intros`라는 JSON 요청이 오가는 것이 이 모듈 전체의 핵심 장면입니다.

## 2. 통신 계층 — src/api/introApi.js

서버 호출 코드는 화면 코드에 섞지 않고 한 파일에 모았습니다.
(스프링에서 컨트롤러와 서비스를 나눈 것과 같은 "관심사 분리"입니다)

```js
const API_BASE_URL = 'http://localhost:8080'

async function request(url, options) {
  const response = await fetch(API_BASE_URL + url, options)

  if (!response.ok) {                    // 404, 500 등 실패 응답이면
    const errorBody = await response.json().catch(() => null)
    throw new Error(errorBody?.message ?? `요청 실패 (HTTP ${response.status})`)
  }
  if (response.status === 204) return null   // 삭제 성공은 본문이 없음
  return response.json()                     // JSON → JS 객체
}
```

읽어야 할 포인트 3가지:

1. **`fetch()`** — 브라우저 내장 HTTP 요청 함수. `fetch(url)`은 GET,
   POST/PUT/DELETE는 두 번째 인자로 method/headers/body를 지정합니다.
2. **`async`/`await`** — 서버 응답은 "언젠가" 도착합니다. `await`는 "도착할 때까지
   기다렸다가 다음 줄 실행"이라는 뜻이고, await를 쓰는 함수에는 async를 붙입니다.
3. **`response.ok` 확인은 필수** — fetch는 404/500이 와도 예외를 던지지 않습니다
   (네트워크가 끊겼을 때만 던짐). 성공 여부는 반드시 직접 확인해야 합니다.
   실패 시 02 문서에서 만든 `{"message": "..."}`를 꺼내 throw하면,
   화면 컴포넌트가 catch해서 사용자에게 보여줍니다.

등록 함수를 보면 "JS 객체 → JSON 전송"이 이렇게 됩니다:

```js
export function createIntro(data) {          // data = { name, title, content }
  return request('/api/intros', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },  // "본문은 JSON입니다" 선언
    body: JSON.stringify(data),                        // 객체 → JSON 문자열
  })
}
```

서버 쪽 `@RequestBody IntroRequest`가 이 JSON을 받아 객체로 되돌립니다.
**`JSON.stringify()` ↔ `@RequestBody`가 짝**이라는 것을 눈에 익혀 두세요.

## 3. CORS — 반드시 한 번은 만나는 에러

**직접 겪어 보기**: 백엔드의 `config/WebConfig.java`를 잠깐 지우고(또는 어노테이션을
주석 처리하고) 재시작한 뒤 5173에 접속해 보세요. 목록이 안 뜨고 콘솔(F12)에 이런
빨간 에러가 나옵니다:

```
Access to fetch at 'http://localhost:8080/api/intros' from origin
'http://localhost:5173' has been blocked by CORS policy: ...
```

**원인**: 브라우저의 동일 출처 정책(Same-Origin Policy). 내가 접속한 사이트(5173)의
JS가 **다른 출처**(8080 — 포트가 다르면 다른 출처!)로 요청하는 것을 브라우저가
기본적으로 차단합니다. 악성 사이트가 여러분이 로그인해 둔 다른 서비스에 몰래 요청을
보내는 공격을 막기 위한 보안 장치인데, 우리처럼 일부러 분리한 경우에도 걸립니다.

**해결**: 서버가 "그 출처는 내가 허락한다"고 응답에 표시해 주면 됩니다(CORS).
intro-api의 `WebConfig.java` 한 파일이 그 역할입니다:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")   // React 개발 서버 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

> ⚠️ `allowedOrigins("*")`로 전부 열면 편하지만, 위에서 설명한 보안 장치를 스스로
> 끄는 셈입니다. **꼭 필요한 출처만** 여는 습관을 들이세요. (DB 교육에서 배운
> "최소 권한 원칙"과 같은 사고방식입니다)

## 4. 목록 화면 — useEffect로 데이터 불러오기

[IntroList.jsx](./샘플/intro-react/src/components/IntroList.jsx)를 여세요.
서버 렌더링에는 없던 새 고민이 있습니다: **화면이 먼저 뜨고, 데이터는 나중에 온다.**

```jsx
function IntroList({ onSelect, onNew }) {
  const [intros, setIntros] = useState([])      // 데이터 (처음엔 빈 배열)
  const [loading, setLoading] = useState(true)  // 아직 불러오는 중?
  const [error, setError] = useState(null)      // 실패했다면 메시지

  useEffect(() => {                    // "화면이 그려진 다음에 실행해 줘"
    fetchIntros()
      .then((data) => setIntros(data))          // 도착! → state 갱신 → 화면 다시 그림
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])                               // [] = 처음 나타날 때 한 번만

  if (loading) return <p>불러오는 중...</p>
  if (error) return <div className="alert alert-danger">{error}</div>
  return ( /* 표 그리기 */ )
}
```

- **`useEffect(함수, [])`** — "이 컴포넌트가 화면에 나타나면 함수를 실행해 줘".
  API 호출처럼 "그리기 밖의 일"은 여기서 합니다. 두 번째 인자 `[]`는 "한 번만",
  `[id]`처럼 값을 넣으면 "그 값이 바뀔 때마다 다시" 실행됩니다(IntroDetail이 이 방식).
- **로딩/에러/정상 3가지 상태를 모두 그려 줘야** 좋은 화면입니다. 데이터가 오기 전
  0.5초 동안 사용자는 뭘 보고 있어야 할까요? 서버가 죽어 있다면요?

표를 그리는 부분은 타임리프의 반복문과 비교하면 바로 이해됩니다:

```html
<!-- intro-jpa (타임리프): -->
<tr th:each="intro : ${intros}"> <td th:text="${intro.id}">1</td> ... </tr>
```
```jsx
{/* intro-react (JSX): 배열.map으로 각 요소를 <tr>로 변환 */}
{intros.map((intro) => (
  <tr key={intro.id}>  {/* key: React가 각 행을 구별하는 이름표. 반복엔 필수! */}
    <td>{intro.id}</td> ...
  </tr>
))}
```

## 5. 화면 전환 — App.jsx의 state 하나로

intro-jpa에서 화면 전환은 URL 이동이었습니다(`/` → `/intro/3`). SPA에서는 페이지
이동이 없으므로, **"지금 어느 화면인가"를 state로 기억**합니다:

```jsx
function App() {
  // { name: 'list' } | { name: 'new' } | { name: 'detail', id: 3 } | { name: 'edit', id: 3 }
  const [page, setPage] = useState({ name: 'list' })

  if (page.name === 'list') {
    return <IntroList onSelect={(id) => setPage({ name: 'detail', id })}
                      onNew={() => setPage({ name: 'new' })} />
  }
  // ... detail, edit도 같은 방식
}
```

- 자식(IntroList)에는 **함수를 props로** 내려 줍니다. 자식은 행이 클릭되면
  `onSelect(3)`을 부를 뿐이고, 화면을 실제로 바꾸는 것은 부모 App입니다.
  **"자식 → 부모로의 신호는 props로 받은 함수 호출"** — React의 기본 통신 패턴입니다.
- intro-jpa에서 컨트롤러가 하던 "어느 화면을 보여줄까" 결정이 서버에서
  브라우저(App.jsx)로 이사 왔다는 점을 음미해 보세요.

> 📌 **실무에서는** URL과 화면을 연결해 주는 **react-router** 라이브러리를 씁니다
> (뒤로가기, 북마크, 새로고침이 자연스러워짐). 여기서는 "화면 전환도 결국 state"라는
> 원리를 보여주기 위해 일부러 안 썼습니다. 원리를 알면 라이브러리는 금방 배웁니다.

## 6. 폼 화면 — 입력값도 state로 (제어 컴포넌트)

[IntroForm.jsx](./샘플/intro-react/src/components/IntroForm.jsx)는 **작성과 수정을
하나의 컴포넌트로** 처리합니다 (`editId` prop이 있으면 수정 모드). intro-jpa에서
form.html과 edit.html이 거의 같은 내용이었는데, 컴포넌트라서 합칠 수 있었습니다.

React에서 입력창을 다루는 표준 방식 — **제어 컴포넌트(controlled component)**:

```jsx
const [name, setName] = useState('')

<input value={name}                                  // state 값을 표시하고
       onChange={(e) => setName(e.target.value)} />  // 타이핑마다 state 갱신
```

"입력창의 진짜 값은 state에 있다"가 요점입니다. 그래서 수정 모드에서 서버 데이터를
`setName(intro.name)`으로 넣으면 입력창이 채워지고, 제출할 때도 state만 모으면 됩니다.

제출 처리에서 서버 렌더링과의 결정적 차이를 확인하세요:

```jsx
async function handleSubmit(e) {
  e.preventDefault()             // ① 브라우저의 기본 폼 전송(=페이지 새로고침)을 차단!
  setSaving(true)                // ② 저장 중 버튼 비활성화 (연타 → 중복 등록 방지)
  try {
    const saved = isEdit ? await updateIntro(editId, data)   // PUT
                         : await createIntro(data)           // POST
    onSaved(saved.id)            // ③ 성공 → 부모에게 알림 → App이 상세 화면으로 전환
  } catch (err) {
    setError(err.message)        // ④ 실패 → 입력값을 유지한 채 에러 표시 (재시도 가능)
    setSaving(false)
  }
}
```

- intro-jpa의 `redirect:/`가 하던 일을 ③이 합니다 — **이동 결정권이 프론트로.**
- ④가 사용자 경험의 차이입니다: 서버 렌더링에서 저장이 실패하면 입력하던 내용이
  날아가기 쉽지만, SPA는 state에 값이 살아 있어 바로 재시도할 수 있습니다.

삭제(IntroDetail.jsx)도 같은 패턴입니다: `confirm()`으로 물은 뒤
`await deleteIntro(id)` → `onDeleted()` 로 목록에 복귀합니다.

## 7. 배포는 어떻게? — npm run build

개발 서버(5173)는 개발용입니다. 실제 배포할 때는:

```powershell
npm run build     # → dist/ 폴더에 정적 파일(html + js + css) 생성
```

`dist/`는 그냥 **정적 파일 묶음**이라 아무 웹 서버에나 올릴 수 있습니다.
대표적인 두 가지 방식:

1. **분리 배포(정석)** — 프론트는 정적 호스팅/웹서버(Nginx 등)에, 백엔드는 별도 서버에.
   여러분이 배운 GitHub Pages도 프론트 쪽 배포처가 될 수 있습니다.
2. **스프링에 합치기(간편)** — `dist/` 내용물을 백엔드의 `src/main/resources/static/`에
   복사하면 스프링부트가 8080에서 화면까지 서빙합니다. 서버가 하나로 돌아오므로
   CORS 설정도 필요 없어집니다. 소규모 사내 시스템에서 흔히 쓰는 절충안입니다.

---

## 정리 + 도전 과제

- 프론트는 **fetch로 JSON을 주고받고**, 받은 데이터를 **state에 넣어** 화면을 그린다.
- 출처(포트)가 다르면 **CORS** — 서버에서 허용 출처를 명시해야 한다.
- 화면 전환·이동·에러 안내가 전부 프론트 책임이 되었다. 그만큼 프론트가 "앱"이 된 것.

**스스로 해 보기** (쉬운 것부터):

1. 목록 화면에 **글 개수**(`총 N건`)를 표시해 보세요. (`intros.length`)
2. 삭제 성공 후 `alert('삭제되었습니다')`를 띄워 보세요.
3. 작성 폼에서 이름이 비어 있으면 등록 버튼을 비활성화해 보세요. (`disabled={!name}`)
4. (심화) 검색창을 만들어 제목에 검색어가 포함된 글만 목록에 보여 주세요.
   힌트: `intros.filter(i => i.title.includes(keyword))` — 서버 호출 없이 됩니다!
5. (심화) 백엔드에 `GET /api/intros?name=김인턴` 같은 필터 기능을 추가하고
   프론트와 연결해 보세요. — 백/프론트를 **양쪽 다** 고치는 첫 경험!
