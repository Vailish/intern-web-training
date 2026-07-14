# 03. React 시작하기 — 프로젝트 생성과 기본 원리

> **이 문서에서 배우는 것**
> - Node.js와 npm — 자바의 JDK/Gradle에 해당하는 프론트 도구
> - Vite로 React 프로젝트 만들기
> - React의 4가지 핵심: **컴포넌트, JSX, props, state**
> - 손으로 직접: 버튼 카운터 만들어 보기

여기서부터는 자바가 아니라 **JavaScript**의 세계입니다. HTML/CSS/JS 교육에서 배운
JS 문법(함수, 객체, 배열, 화살표 함수)을 그대로 씁니다.

---

## 1. React란?

**React**는 화면(UI)을 만드는 JavaScript 라이브러리입니다. 페이스북(현 메타)이 만들었고,
현재 프론트엔드 분야에서 가장 널리 쓰입니다.

바닐라 JS로 화면을 갱신하려면 이런 코드를 썼습니다:

```js
// 예전 방식: DOM을 하나하나 직접 조작 ("어떻게"를 일일이 지시)
const td = document.createElement('td');
td.textContent = intro.title;
tr.appendChild(td);
table.appendChild(tr);
```

데이터가 바뀔 때마다 "어느 태그를 찾아서, 뭘 지우고, 뭘 넣을지"를 전부 사람이 관리해야
해서, 화면이 조금만 복잡해져도 버그 덩어리가 됩니다. React의 접근은 다릅니다:

```jsx
// React 방식: "데이터가 이런 모양일 때 화면은 이렇게 생겼다"만 선언
<td>{intro.title}</td>
```

**개발자는 "데이터 → 화면" 규칙만 적어 두고, 데이터가 바뀌면 React가 알아서
바뀐 부분만 다시 그립니다.** 이것이 React의 근본 원리입니다. 아래에서 배울
컴포넌트/JSX/props/state는 전부 이 원리를 구현하는 장치들입니다.

## 2. 준비물: Node.js와 npm

| 자바 세계 | 프론트 세계 | 하는 일 |
|---|---|---|
| JDK | **Node.js** | 코드 실행 환경 (브라우저 밖에서 JS를 실행) |
| Gradle | **npm** | 라이브러리 관리 + 빌드/실행 명령 |
| build.gradle | **package.json** | 프로젝트 정보와 의존성 목록 |
| ~/.gradle 캐시 | **node_modules 폴더** | 내려받은 라이브러리 저장소 |

[nodejs.org](https://nodejs.org)에서 **LTS 버전**을 받아 설치하세요 (기본 옵션으로 다음다음).
npm은 Node.js에 딸려 옵니다. 설치 확인:

```powershell
node --version    # v22.x.x 처럼 나오면 OK
npm --version     # 11.x.x
```

> 📌 "React를 브라우저에서 쓰는데 왜 Node.js가 필요하지?" — 실행은 브라우저가 하지만,
> **개발 도구**(개발 서버, JSX 변환, 빌드)가 Node.js 위에서 돕니다.
> 그레이들이 자바 앱 자체는 아니지만 빌드에 필요한 것과 같습니다.

## 3. 프로젝트 만들기 — Vite

**Vite**(비트, 프랑스어로 "빠르다")는 React 프로젝트의 뼈대 생성 + 개발 서버 + 빌드를
담당하는 도구입니다. start.spring.io + 내장 톰캣 + Gradle을 합친 역할이라고 보면 됩니다.

```powershell
# my-app 이라는 이름의 React 프로젝트 생성
npx create-vite@latest my-app --template react

cd my-app
npm install      # package.json에 적힌 라이브러리 내려받기 (최초 1회)
npm run dev      # 개발 서버 실행!
```

`npx`는 "npm에서 도구를 받아 바로 실행"하는 명령입니다.
`http://localhost:5173`에 접속해 Vite 초기 화면이 보이면 성공입니다.

> ⚠️ **함정: `npm create vite@latest my-app -- --template react`** — 인터넷 자료에
> 많이 나오는 이 명령이 npm 11에서는 `--template` 옵션을 제대로 전달하지 못해서
> **엉뚱한 템플릿(TypeScript 등)이 생성될 수 있습니다** (이 자료를 만들 때 실제로 겪음).
> 위처럼 `npx create-vite@latest`를 쓰는 것이 확실합니다.
> 만약 src 폴더에 `.jsx`가 아니라 `.ts` 파일이 생겼다면 폴더를 지우고 다시 만드세요.

### 생성된 폴더 구조

```
my-app/
├── index.html          ← 유일한 HTML! (SPA의 뼈대, 빈 div 하나)
├── package.json        ← build.gradle 격 (의존성·명령 정의)
├── vite.config.js      ← Vite 설정
├── node_modules/       ← 내려받은 라이브러리 (git에 올리지 않음, npm install로 복원)
└── src/
    ├── main.jsx        ← 시작점 (index.html의 div에 App을 그려 넣음)
    ├── App.jsx         ← 최상위 컴포넌트 (여기부터 고치면 됨)
    └── index.css       ← 전역 CSS
```

`npm run dev` 상태에서 `App.jsx`를 고쳐 저장해 보세요 — **새로고침 없이 즉시**
화면에 반영됩니다(HMR). 스프링 DevTools보다 훨씬 빠릅니다.

## 4. React 핵심 개념 4가지

### ① 컴포넌트(Component) — 화면 부품 = 함수

React에서 화면은 **컴포넌트**라는 부품의 조립입니다. 컴포넌트의 정체는
"화면 조각을 반환하는 JS 함수"일 뿐입니다:

```jsx
// 컴포넌트 = 첫 글자가 대문자인 함수. <Hello /> 라고 태그처럼 쓸 수 있게 됩니다.
function Hello() {
  return <h1>안녕하세요!</h1>;
}

function App() {
  return (
    <div>
      <Hello />   {/* 내가 만든 컴포넌트를 HTML 태그처럼 사용! */}
      <Hello />   {/* 부품이니까 재사용도 됩니다 */}
    </div>
  );
}
```

우리 샘플에서는 목록/상세/폼 화면이 각각 `IntroList`, `IntroDetail`, `IntroForm`
컴포넌트입니다. intro-jpa의 templates 폴더에 html이 3~4개였던 것과 대응됩니다.

### ② JSX — JS 안에 화면 구조를 쓰는 문법

`return <h1>...</h1>` 처럼 JS 코드 안에 HTML처럼 생긴 것을 쓰는 문법이 **JSX**입니다.
브라우저는 JSX를 모르기 때문에 Vite가 진짜 JS로 변환해 줍니다. HTML과 다른 규칙 4가지:

| 규칙 | HTML | JSX | 이유 |
|---|---|---|---|
| 중괄호 = JS 값 삽입 | - | `<td>{intro.title}</td>` | 타임리프 `th:text` 역할 |
| class 금지 | `class="btn"` | `className="btn"` | class는 JS 예약어 |
| for 금지 | `for="name"` | `htmlFor="name"` | for도 JS 예약어 |
| 최상위 태그는 하나 | - | 여러 개면 `<>...</>`로 감싸기 | 함수 반환값은 하나여야 하므로 |

### ③ props — 부모가 자식에게 주는 값

컴포넌트도 함수이므로 **인자**를 받을 수 있습니다. 태그의 속성으로 넘기면
함수의 첫 번째 인자(객체)로 들어옵니다. 이것이 **props**입니다.

```jsx
function Greeting({ name }) {          // props에서 name만 꺼내 받기 (구조 분해)
  return <p>{name}님, 환영합니다!</p>;
}

// 사용하는 쪽(부모):
<Greeting name="김인턴" />              // → "김인턴님, 환영합니다!"
<Greeting name="박신입" />              // → "박신입님, 환영합니다!"
```

값뿐 아니라 **함수도** 넘길 수 있습니다. 우리 샘플에서 App이 자식에게
"이 버튼 누르면 이 함수를 불러 줘"라고 화면 전환 함수를 내려 주는 데 씁니다.

### ④ state — 컴포넌트가 기억하는, 화면을 움직이는 값

**바뀌면 화면도 바뀌어야 하는 값**은 반드시 state로 선언해야 합니다.

```jsx
import { useState } from 'react';

function Counter() {
  // [현재값, 바꾸는 함수] 를 돌려줍니다. 0은 최초값.
  const [count, setCount] = useState(0);

  return (
    <button onClick={() => setCount(count + 1)}>
      {count}번 눌렀습니다
    </button>
  );
}
```

동작 원리가 중요합니다:

1. 버튼 클릭 → `setCount(1)` 호출
2. React가 "state가 바뀌었네!" 하고 **Counter 함수를 다시 실행**
3. 새 반환값(`1번 눌렀습니다`)과 이전 화면을 비교해 **바뀐 부분만** 실제 화면에 반영

> ⚠️ `let count = 0; count++` 처럼 일반 변수로 하면 **화면이 절대 안 바뀝니다.**
> React는 setXxx 함수가 불릴 때만 다시 그리기 때문입니다. "화면에 보이는 값은
> state로, 바꿀 때는 반드시 set 함수로" — React 제1원칙입니다.

## 5. 직접 해 보기 — 5분 실습

방금 만든 my-app의 `src/App.jsx` 내용을 통째로 지우고 이렇게 바꿔 보세요:

```jsx
import { useState } from 'react';

function App() {
  const [count, setCount] = useState(0);
  const [name, setName] = useState('');

  return (
    <div style={{ padding: '40px' }}>
      <h1>React 연습장</h1>

      {/* state ① 카운터 */}
      <button onClick={() => setCount(count + 1)}>👍 {count}</button>

      {/* state ② 입력값 — 타이핑하는 즉시 아래 문장이 바뀝니다 */}
      <p>
        <input value={name} onChange={(e) => setName(e.target.value)}
               placeholder="이름을 입력해 보세요" />
      </p>
      <p>{name ? `${name}님, 안녕하세요!` : '아직 이름이 없습니다.'}</p>
    </div>
  );
}

export default App;
```

저장하는 순간 브라우저가 바로 바뀝니다. 버튼과 입력창을 조작하며
**"state가 바뀌면 화면이 다시 그려진다"** 를 몸으로 확인하세요.

> 📌 **개발자 도구(F12) 팁** — Console에 경고가 뜨면 React가 뭘 지적하는지 읽어 보는
> 습관을 들이세요. 그리고 개발 모드(StrictMode)에서는 React가 검사 목적으로 컴포넌트를
> **일부러 두 번씩** 실행합니다. API 호출이 두 번 보여도 버그가 아닙니다
> (빌드된 실제 배포판에서는 한 번만 실행됩니다).

---

## 정리

- React 화면 = **컴포넌트**(함수)의 조립. 화면 구조는 **JSX**로 쓴다.
- 부모 → 자식으로 값을 주는 통로가 **props**, 화면을 움직이는 기억 장치가 **state**.
- "데이터가 바뀌면 React가 알아서 다시 그린다" — 모든 것이 이 원리 위에 있다.

이제 부품 조립법을 알았으니, 진짜 데이터(백엔드 API)를 연결해 자기소개서 앱을 만듭니다.

**다음** → [04. React로 CRUD 화면 만들기](./04_React로_CRUD_화면_만들기.md)
