# CSS 기초 정리
- [TIL정리내용 - 02_CSS기본](https://github.com/Vailish/TIL/blob/master/01_Language/02_HTML%26CSS/02_CSS%EA%B8%B0%EB%B3%B8.md)
- [TIL정리내용 - 02_CSS기본](https://github.com/Vailish/TIL/blob/master/01_Language/02_HTML%26CSS/03_flexbox.md)
- [MDN - HTML](https://developer.mozilla.org/ko/docs/Web/CSS)
- [flex방식 연습 - Flexbox Froggy](https://flexboxfroggy.com/#ko)
- [grid방식 연습 - gridgarden](https://cssgridgarden.com/#ko)
- [bootstrap](https://getbootstrap.com/)
---

## 1. CSS란?

CSS(Cascading Style Sheets)는 HTML로 작성된 문서의 **모양(디자인)** 을 꾸미는 언어입니다.

- HTML: 문서의 "구조"와 "내용"을 담당 (뼈대)
- CSS: 문서의 "디자인"을 담당 (색상, 크기, 배치, 폰트 등)
- JavaScript: 문서의 "동작"을 담당 (클릭하면 ~한다 등)

```html
<!-- HTML -->
<p class="title">안녕하세요</p>
```

```css
/* CSS */
.title {
  color: blue;
  font-size: 24px;
}
```

---

## 2. CSS를 적용하는 3가지 방법

| 방법 | 설명 | 예시 |
|---|---|---|
| 인라인(Inline) | HTML 태그에 직접 style 속성 작성 | `<p style="color:red;">텍스트</p>` |
| 내부(Internal) | `<head>` 안에 `<style>` 태그 사용 | `<style>p{color:red;}</style>` |
| 외부(External) | 별도의 .css 파일을 만들어 `link` 태그로 연결 | `<link rel="stylesheet" href="style.css">` |

> 실무에서는 **외부 CSS 파일**을 사용하는 것이 기본입니다. 유지보수와 재사용이 쉽기 때문입니다.

---

## 3. 선택자(Selector) — CSS를 어디에 적용할지 정하기

| 선택자 | 문법 | 설명 |
|---|---|---|
| 전체 선택자 | `*` | 모든 요소 선택 |
| 태그 선택자 | `p`, `div` | 해당 태그 전체 선택 |
| 클래스 선택자 | `.클래스명` | class 속성으로 선택 (가장 많이 사용) |
| id 선택자 | `#아이디명` | id 속성으로 선택 (페이지에서 고유해야 함) |
| 자식 선택자 | `부모 > 자식` | 직계 자식만 선택 |
| 후손 선택자 | `부모 자식` | 모든 하위 요소 선택 |
| 가상 클래스 | `:hover`, `:focus` | 특정 상태일 때 적용 |

```css
.box {}          /* class="box" 요소 */
#header {}       /* id="header" 요소 */
.nav > li {}     /* nav의 직계 자식 li */
a:hover {}       /* 마우스를 올렸을 때 */
```

---

## 4. 박스 모델(Box Model) — 모든 요소는 "상자"다

CSS에서 모든 요소는 다음 4개 영역으로 구성된 박스로 취급됩니다.

```
┌─────────────────────────────┐
│           margin            │  (요소 바깥 여백)
│  ┌───────────────────────┐  │
│  │        border         │  │  (테두리)
│  │  ┌─────────────────┐  │  │
│  │  │     padding      │  │  │  (테두리 안쪽 여백)
│  │  │  ┌────────────┐  │  │  │
│  │  │  │  content   │  │  │  │  (실제 내용)
│  │  │  └────────────┘  │  │  │
│  │  └─────────────────┘  │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

```css
.box {
  width: 300px;
  padding: 20px;
  border: 2px solid black;
  margin: 10px;
}
```

### box-sizing (실무 필수 설정)

기본값은 `content-box`이지만, padding과 border가 width에 포함되지 않아 계산이 복잡해집니다.
실무에서는 거의 항상 아래처럼 설정합니다.

```css
* {
  box-sizing: border-box;
}
```

---

## 5. display 속성 — 요소를 어떻게 배치할지 결정

| 값 | 특징 |
|---|---|
| `block` | 한 줄 전체를 차지함 (div, p, h1 등 기본값) |
| `inline` | 내용 크기만큼만 차지, 줄바꿈 없음 (span, a 등 기본값) |
| `inline-block` | inline처럼 옆으로 배치되지만 width/height 지정 가능 |
| `none` | 화면에서 완전히 사라짐 (공간도 없음) |
| `flex` | 자식 요소들을 가로/세로로 유연하게 배치 |
| `grid` | 자식 요소들을 격자(표) 형태로 배치 |

---

## 6. position 속성 — 요소의 위치를 직접 조정

| 값 | 설명 |
|---|---|
| `static` | 기본값. 원래 흐름대로 배치 |
| `relative` | 원래 위치를 기준으로 이동 (다른 요소에 영향 없음) |
| `absolute` | 가장 가까운 relative 부모를 기준으로 위치 지정 |
| `fixed` | 화면(viewport)을 기준으로 고정 (스크롤해도 안 움직임) |
| `sticky` | 스크롤 중 특정 위치에 도달하면 고정됨 |

```css
.parent {
  position: relative;
}
.child {
  position: absolute;
  top: 0;
  right: 0;
}
```

---

## 7. 레이아웃을 만드는 3가지 방식 (다음 실습 주제)

옛날에는 `float`로 레이아웃을 짰지만, 지금은 `flexbox`와 `grid`가 표준입니다.
같은 디자인을 세 가지 방식으로 모두 만들어보면 차이를 확실히 이해할 수 있습니다.

### 7-1. Float 방식 (과거 방식, 이제는 잘 안 씀)

원래 이미지 옆에 글을 흘리기 위한 속성인데, 과거에는 이를 응용해 레이아웃을 짰습니다.

```css
.box {
  float: left;
  width: 33.33%;
}
.container::after {
  content: "";
  display: block;
  clear: both; /* float 풀리는 현상 방지 */
}
```

- 단점: 부모 높이가 자동으로 안 잡히는 문제(`clear` 필요), 순서 제어가 직관적이지 않음

### 7-2. Flexbox 방식 (1차원 배치에 강함)

가로 또는 세로, "한 방향" 정렬에 최적화되어 있습니다.

```css
.container {
  display: flex;
  justify-content: space-between; /* 가로 정렬 */
  align-items: center;            /* 세로 정렬 */
  gap: 16px;                      /* 요소 간 간격 */
}
.box {
  flex: 1; /* 남는 공간 1:1로 나눠 가짐 */
}
```

| 속성 | 역할 |
|---|---|
| `display: flex` | flexbox 활성화 |
| `flex-direction` | row(기본, 가로) / column(세로) |
| `justify-content` | 주축 방향 정렬 (가로일 때 좌우) |
| `align-items` | 교차축 방향 정렬 (가로일 때 위아래) |
| `flex-wrap` | 줄바꿈 허용 여부 |
| `gap` | 자식 요소 간 간격 |

### 7-3. Grid 방식 (2차원 배치에 강함)

가로와 세로를 동시에, "표"처럼 배치할 때 가장 강력합니다.

```css
.container {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3칸 균등 분할 */
  gap: 16px;
}
.box {
  /* 필요 시 특정 칸 지정 가능 */
  grid-column: span 2;
}
```

| 속성 | 역할 |
|---|---|
| `display: grid` | grid 활성화 |
| `grid-template-columns` | 열(세로줄) 개수와 너비 정의 |
| `grid-template-rows` | 행(가로줄) 개수와 높이 정의 |
| `gap` | 칸 사이 간격 |
| `grid-column` / `grid-row` | 특정 요소가 몇 칸을 차지할지 |

### 7-4. 비교 정리

| 항목 | Float | Flexbox | Grid |
|---|---|---|---|
| 등장 시기 | 과거 표준 방식 | 1차원 레이아웃 표준 | 2차원 레이아웃 표준 |
| 적합한 상황 | 거의 사용 안 함 | 네비바, 카드 한 줄 나열 등 | 전체 페이지 구조, 표 형태 레이아웃 |
| 코드 복잡도 | clear, overflow 처리 필요해 복잡 | 비교적 간단 | 처음엔 어렵지만 구조적임 |
| 실무 권장도 | 비권장(레거시 코드 이해용) | 적극 권장 | 적극 권장 |

> 다음 실습에서 **같은 디자인**을 세 가지 방식으로 각각 만들어보면서 "언제 무엇을 써야 하는지" 감을 잡아봅시다.

---

## 8. 자주 헷갈리는 단위

| 단위 | 설명 |
|---|---|
| `px` | 고정 픽셀 (절대 단위) |
| `%` | 부모 요소 기준 비율 |
| `em` | 부모 요소의 font-size 기준 |
| `rem` | 최상위(html) 요소의 font-size 기준 (실무에서 가장 선호) |
| `vw` / `vh` | 브라우저 화면(viewport) 너비/높이 기준 |

---

## 9. 자주 하는 실수 체크리스트

- [ ] `box-sizing: border-box`를 빼먹고 width 계산에서 헷갈려함
- [ ] class 이름과 id 이름을 혼동해서 선택자를 `#`/`.` 잘못 사용
- [ ] float 사용 후 `clear` 처리를 안 해서 레이아웃이 깨짐
- [ ] flex 컨테이너에 직접 `text-align`을 써서 정렬이 안 됨 (→ `justify-content`/`align-items` 사용해야 함)
- [ ] CSS 우선순위(명시도, Specificity)를 몰라서 스타일이 적용 안 될 때 무작정 `!important` 사용

