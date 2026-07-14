// ============================================================================
// App.jsx — 최상위 컴포넌트: "지금 어떤 화면을 보여줄지" 결정합니다.
//
// intro-jpa에서는 화면 전환 = URL 이동이었습니다.
//   / → list.html,  /intro/new → form.html,  /intro/3 → detail.html
// 서버의 컨트롤러가 URL을 보고 어떤 HTML을 줄지 결정했지요.
//
// React(SPA)에서는 페이지 이동이 없습니다. 대신 "지금 무슨 화면인지"를
// state(상태)로 기억해 두고, 그 값에 따라 다른 컴포넌트를 그립니다.
// 컨트롤러의 화면 결정 역할이 서버에서 브라우저로 넘어온 것입니다!
//
// (실무에서는 react-router라는 라이브러리로 URL과 화면을 연결하지만,
//  여기서는 "state로 화면을 바꾼다"는 React의 원리를 그대로 보여주기 위해
//  라이브러리 없이 구현했습니다.)
// ============================================================================

import { useState } from 'react'
import IntroList from './components/IntroList.jsx'
import IntroDetail from './components/IntroDetail.jsx'
import IntroForm from './components/IntroForm.jsx'

function App() {
  // ── state(상태) ──────────────────────────────────────────────
  // useState: React에게 "이 값을 기억해 줘. 바뀌면 화면을 다시 그려 줘"라고
  // 부탁하는 함수입니다. [현재값, 바꾸는함수] 한 쌍을 돌려줍니다.
  //
  // page 값의 모양: { name: 'list' }                → 목록 화면
  //                { name: 'new' }                 → 작성 화면
  //                { name: 'detail', id: 3 }       → 3번 글 상세 화면
  //                { name: 'edit', id: 3 }         → 3번 글 수정 화면
  //
  // ⚠️ 일반 변수(let page = ...)로 하면 안 되나요? → 안 됩니다!
  //    값을 바꿔도 React가 모르기 때문에 화면이 다시 그려지지 않습니다.
  //    "화면에 영향을 주는 값은 반드시 state로" — React의 제1원칙입니다.
  const [page, setPage] = useState({ name: 'list' })

  // ── 화면 선택 ────────────────────────────────────────────────
  // page.name에 따라 보여줄 컴포넌트를 고릅니다.
  // <IntroList onSelect={...} /> 처럼 속성으로 넘기는 값을 props라고 합니다.
  // 여기서는 "화면을 전환하는 함수"를 자식에게 props로 내려주고,
  // 자식은 버튼이 눌리면 그 함수를 호출합니다(자식 → 부모로 신호 보내기).
  let screen
  if (page.name === 'list') {
    screen = (
      <IntroList
        onSelect={(id) => setPage({ name: 'detail', id })} // 행 클릭 → 상세로
        onNew={() => setPage({ name: 'new' })}             // 작성 버튼 → 폼으로
      />
    )
  } else if (page.name === 'new') {
    screen = (
      <IntroForm
        onSaved={(id) => setPage({ name: 'detail', id })}  // 저장 완료 → 상세로
        onCancel={() => setPage({ name: 'list' })}         // 취소 → 목록으로
      />
    )
  } else if (page.name === 'detail') {
    screen = (
      <IntroDetail
        id={page.id}
        onBack={() => setPage({ name: 'list' })}
        onEdit={() => setPage({ name: 'edit', id: page.id })}
        onDeleted={() => setPage({ name: 'list' })}        // 삭제 완료 → 목록으로
      />
    )
  } else if (page.name === 'edit') {
    screen = (
      <IntroForm
        editId={page.id}                                    // id가 있으면 "수정 모드"
        onSaved={(id) => setPage({ name: 'detail', id })}
        onCancel={() => setPage({ name: 'detail', id: page.id })}
      />
    )
  }

  // ── JSX ──────────────────────────────────────────────────────
  // return 안의 HTML처럼 생긴 것이 JSX입니다. "JS 안에서 화면 구조를 쓰는 문법"으로,
  // {변수} 형태로 JS 값을 끼워 넣을 수 있습니다. 타임리프의 th:text와 비슷한
  // 역할이지만, 서버가 아니라 브라우저에서 실행된다는 점이 다릅니다.
  return (
    <div className="container py-5">
      {/* JSX에서는 class 대신 className을 씁니다 (class는 JS 예약어라서) */}
      {screen}
    </div>
  )
}

// 다른 파일(main.jsx)에서 import 할 수 있게 내보냅니다.
export default App
