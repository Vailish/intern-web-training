// ============================================================================
// IntroList.jsx — 목록 화면 (intro-jpa의 templates/list.html에 해당)
//
// 이 컴포넌트가 하는 일:
//   1. 화면에 나타나자마자 백엔드에 GET /api/intros 요청을 보내고
//   2. 응답(JSON 배열)을 state에 담으면 → React가 표를 다시 그립니다.
//
// list.html과 비교해 보세요:
//   - th:each="intro : ${intros}"  →  intros.map(intro => <tr>...</tr>)
//   - 서버가 데이터를 "품은 HTML"을 주던 방식에서,
//     빈 화면을 먼저 그리고 데이터를 "나중에 받아와" 채우는 방식으로 바뀌었습니다.
// ============================================================================

import { useEffect, useState } from 'react'
import { fetchIntros, formatDateTime } from '../api/introApi.js'

// props 받기: 부모(App)가 넘겨준 { onSelect, onNew }를 구조 분해로 꺼냅니다.
function IntroList({ onSelect, onNew }) {
  // 이 화면이 기억해야 할 값 3가지. 데이터를 "나중에" 받아오는 화면의 기본 세트입니다.
  const [intros, setIntros] = useState([])       // 자기소개서 목록 (처음엔 빈 배열)
  const [loading, setLoading] = useState(true)   // 지금 불러오는 중인가?
  const [error, setError] = useState(null)       // 실패했다면 에러 메시지

  // ── useEffect: "화면이 그려진 다음에 이 일을 해 줘" ──────────────
  // 데이터 요청을 컴포넌트 함수 본문에서 바로 하면 안 되고(그리기 도중이라 부작용 금지),
  // useEffect 안에서 해야 합니다.
  // 두 번째 인자 [](빈 배열)의 의미: "처음 나타날 때 한 번만 실행해라".
  // (배열에 값을 넣으면 그 값이 바뀔 때마다 다시 실행됩니다 → IntroForm.jsx 참고)
  useEffect(() => {
    fetchIntros()
      .then((data) => setIntros(data))       // 성공: 받은 배열을 state에 저장 → 화면 갱신!
      .catch((err) => setError(err.message)) // 실패: 에러 메시지를 state에 저장
      .finally(() => setLoading(false))      // 성공이든 실패든 "로딩 중" 표시는 끝
  }, [])

  // ── 상태에 따라 다른 화면 ────────────────────────────────────
  // 서버 렌더링에는 없던 고민입니다: 데이터가 도착하기 "전"의 화면도 우리가 책임져야 합니다.
  if (loading) {
    return <p className="text-secondary">불러오는 중...</p>
  }
  if (error) {
    return (
      <div className="alert alert-danger">
        목록을 불러오지 못했습니다: {error}
        <br />
        <small>백엔드 서버(intro-api, 8080 포트)가 켜져 있는지 확인하세요.</small>
      </div>
    )
  }

  return (
    <>
      {/* <>...</>는 여러 태그를 하나로 묶는 포장지입니다.
          (JSX는 최상위 태그가 딱 하나여야 한다는 규칙 때문에 필요) */}
      <h1 className="mb-1">📋 인턴 자기소개서</h1>
      <p className="text-secondary">우리 기수 인턴들의 자기소개서 모음입니다.</p>

      {/* onClick={함수}: 클릭하면 부모가 준 onNew()를 호출 → App이 화면을 폼으로 전환.
          intro-jpa에서는 <a href="/intro/new">로 "페이지 이동"했지만,
          여기서는 페이지 이동 없이 state만 바뀝니다. */}
      <button type="button" className="btn btn-primary mb-3" onClick={onNew}>
        ✏️ 자기소개서 작성
      </button>

      <table className="table table-hover align-middle">
        <thead className="table-light">
          <tr>
            <th scope="col" style={{ width: '80px' }}>번호</th>
            {/* style은 문자열이 아니라 {{ }} 객체로 씁니다. 바깥 { }는 "JS 시작",
                안쪽 { }는 객체. width: '80px'처럼 값은 문자열로. */}
            <th scope="col">제목</th>
            <th scope="col" style={{ width: '120px' }}>이름</th>
            <th scope="col" style={{ width: '180px' }}>작성일</th>
          </tr>
        </thead>
        <tbody>
          {/* 배열.map(): 배열의 각 요소를 <tr>로 변환합니다. th:each의 React 버전!
              key={intro.id}: 반복으로 만든 태그에는 각 항목을 구별할 고유값을
              반드시 달아야 합니다. React가 "어느 행이 바뀌었는지" 알아채고
              그 행만 다시 그리기 위한 이름표입니다. (없으면 콘솔에 경고가 뜹니다) */}
          {intros.map((intro) => (
            <tr key={intro.id}>
              <td>{intro.id}</td>
              <td>
                {/* href 대신 onClick: 이동할 페이지가 없으니 부모에게 id만 알려줍니다 */}
                <a
                  href="#"
                  className="intro-link"
                  onClick={(e) => {
                    e.preventDefault() // a 태그의 기본 동작(페이지 맨 위로 점프)을 막습니다
                    onSelect(intro.id)
                  }}
                >
                  {intro.title}
                </a>
              </td>
              <td>{intro.name}</td>
              <td>{formatDateTime(intro.createdAt)}</td>
            </tr>
          ))}

          {/* 목록이 비어 있을 때의 안내 행.
              {조건 && <태그>}: 조건이 참일 때만 태그를 그리는 JSX 관용구입니다.
              (list.html의 th:if 역할) */}
          {intros.length === 0 && (
            <tr>
              <td colSpan="4" className="text-center text-secondary py-4">
                아직 등록된 자기소개서가 없습니다. 첫 번째 작성자가 되어 보세요!
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </>
  )
}

export default IntroList
