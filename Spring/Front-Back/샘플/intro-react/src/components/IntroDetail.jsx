// ============================================================================
// IntroDetail.jsx — 상세 화면 (intro-jpa의 templates/detail.html에 해당)
//
// props로 받은 id의 자기소개서 한 건을 GET /api/intros/{id}로 조회해 보여주고,
// 삭제 버튼을 누르면 DELETE /api/intros/{id}를 호출합니다.
//
// detail.html과 비교:
//   - th:text="${intro.title}"        →  {intro.title}
//   - 삭제용 <form method="post">      →  fetch로 DELETE 메서드를 직접 전송
//     (HTML 폼은 GET/POST만 가능했지만 JS는 DELETE를 제대로 보낼 수 있습니다!)
// ============================================================================

import { useEffect, useState } from 'react'
import { deleteIntro, fetchIntro, formatDateTime } from '../api/introApi.js'

function IntroDetail({ id, onBack, onEdit, onDeleted }) {
  const [intro, setIntro] = useState(null)   // 조회한 자기소개서 (처음엔 없음)
  const [error, setError] = useState(null)

  // id가 바뀔 때마다(=다른 글을 열 때마다) 다시 조회합니다.
  // 두 번째 인자 [id]: "id 값이 바뀌면 이 effect를 다시 실행해라"는 뜻입니다.
  useEffect(() => {
    fetchIntro(id)
      .then((data) => setIntro(data))
      .catch((err) => setError(err.message)) // 없는 id면 백엔드가 404 + 메시지를 줍니다
  }, [id])

  /** 삭제 버튼 클릭 시 실행됩니다. */
  async function handleDelete() {
    // confirm: 브라우저 내장 확인창. detail.html의 onsubmit confirm과 같은 역할.
    if (!window.confirm('정말 삭제할까요? 되돌릴 수 없습니다.')) {
      return
    }
    try {
      await deleteIntro(id)  // DELETE 요청이 끝날 때까지 기다렸다가
      onDeleted()            // 부모(App)에게 "삭제 끝!" → App이 목록 화면으로 전환
    } catch (err) {
      alert('삭제에 실패했습니다: ' + err.message)
    }
  }

  if (error) {
    return (
      <div style={{ maxWidth: '720px' }}>
        <div className="alert alert-danger">{error}</div>
        <button type="button" className="btn btn-outline-secondary" onClick={onBack}>
          ← 목록으로
        </button>
      </div>
    )
  }

  // 데이터가 아직 도착하지 않은 순간에도 이 컴포넌트는 한 번 그려집니다.
  // 이때 intro.title에 접근하면 에러가 나므로, 도착 전에는 안내문만 보여줍니다.
  if (intro === null) {
    return <p className="text-secondary">불러오는 중...</p>
  }

  return (
    <div style={{ maxWidth: '720px' }}>
      <h1 className="mb-1">{intro.title}</h1>
      <p className="text-secondary mb-4">
        {intro.name} · {formatDateTime(intro.createdAt)}
      </p>

      <div className="card">
        {/* content-box 클래스: 줄바꿈을 그대로 보여주기 위한 스타일 (index.css) */}
        <div className="card-body content-box">{intro.content}</div>
      </div>

      <div className="d-flex gap-2 mt-4">
        {/* 세 버튼 모두 "페이지 이동" 없이 함수 호출만 합니다 */}
        <button type="button" className="btn btn-outline-secondary" onClick={onBack}>
          ← 목록으로
        </button>
        <button type="button" className="btn btn-outline-primary" onClick={onEdit}>
          수정
        </button>
        <button type="button" className="btn btn-outline-danger" onClick={handleDelete}>
          삭제
        </button>
      </div>
    </div>
  )
}

export default IntroDetail
