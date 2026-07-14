// ============================================================================
// IntroForm.jsx — 작성/수정 화면 (intro-jpa의 form.html + edit.html에 해당)
//
// 컴포넌트 하나로 두 화면을 겸합니다:
//   <IntroForm />           → 작성 모드 (빈 폼)
//   <IntroForm editId={3} /> → 수정 모드 (3번 글을 불러와 폼에 채움)
// intro-jpa에서는 form.html과 edit.html 두 파일이 거의 똑같았는데,
// React에서는 "재사용 가능한 부품(컴포넌트)"이라 하나로 합칠 수 있습니다.
// 이것이 컴포넌트 방식의 큰 장점입니다!
//
// 핵심 개념 — 제어 컴포넌트(controlled component):
//   HTML에서 input의 값은 브라우저가 알아서 관리했지만,
//   React에서는 입력값도 state로 관리합니다.
//     value={name}  : state 값을 입력창에 표시하고
//     onChange={...}: 키를 누를 때마다 state를 갱신합니다.
//   "입력창의 진짜 값은 state에 있다" — 그래서 JS가 언제든 값을 읽고
//   바꿀 수 있습니다(수정 모드에서 기존 글을 채워 넣는 것도 이 원리).
// ============================================================================

import { useEffect, useState } from 'react'
import { createIntro, fetchIntro, updateIntro } from '../api/introApi.js'

function IntroForm({ editId, onSaved, onCancel }) {
  // editId가 있으면 수정 모드, 없으면(undefined) 작성 모드입니다.
  const isEdit = editId != null

  // 입력창 3개의 값을 각각 state로 관리합니다.
  const [name, setName] = useState('')
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [saving, setSaving] = useState(false) // 저장 요청이 진행 중인가?
  const [error, setError] = useState(null)

  // 수정 모드라면: 화면에 나타날 때 기존 글을 불러와 입력창에 채웁니다.
  // (작성 모드에서는 editId가 없으므로 아무 일도 하지 않습니다)
  useEffect(() => {
    if (isEdit) {
      fetchIntro(editId)
        .then((intro) => {
          // 받아온 값을 state에 넣으면 → value={name}인 입력창에 자동 표시됩니다
          setName(intro.name)
          setTitle(intro.title)
          setContent(intro.content)
        })
        .catch((err) => setError(err.message))
    }
  }, [isEdit, editId])

  /**
   * 폼 제출(등록/수정 버튼 클릭) 시 실행됩니다.
   *
   * intro-jpa와의 결정적 차이:
   *   - 옛날: <form method="post"> → 브라우저가 알아서 전송 + 페이지 새로고침
   *   - 지금: JS가 fetch로 전송하고, 페이지는 새로고침되지 않습니다!
   */
  async function handleSubmit(e) {
    // 브라우저의 기본 폼 전송(페이지 새로고침)을 막습니다.
    // SPA에서 새로고침이 일어나면 state가 전부 날아가므로 반드시 필요합니다.
    e.preventDefault()

    setSaving(true) // 버튼을 비활성화해서 저장 중 연타(중복 등록)를 막습니다
    setError(null)
    try {
      const data = { name, title, content } // state 값들을 모아 하나의 객체로
      // 수정 모드면 PUT, 작성 모드면 POST를 보냅니다.
      const saved = isEdit ? await updateIntro(editId, data) : await createIntro(data)
      // 저장 후 어디로 갈지는 부모(App)가 결정합니다(→ 상세 화면).
      // 서버가 응답에 담아준 id 덕분에 방금 만든 글의 상세로 갈 수 있습니다.
      onSaved(saved.id)
    } catch (err) {
      setError(err.message)
      setSaving(false) // 실패했으면 버튼을 다시 눌러 재시도할 수 있게 되돌립니다
    }
  }

  return (
    <div style={{ maxWidth: '720px' }}>
      <h1 className="mb-4">{isEdit ? '✏️ 자기소개서 수정' : '✏️ 자기소개서 작성'}</h1>

      {/* 저장에 실패하면 폼 위에 에러를 보여줍니다 (입력값은 그대로 남아 있음!) */}
      {error && <div className="alert alert-danger">{error}</div>}

      {/* action/method가 없습니다 — 전송은 handleSubmit(JS)이 담당하기 때문 */}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="name" className="form-label">이름</label>
          {/* htmlFor: JSX에서는 for 대신 htmlFor (for도 JS 예약어라서)
              onChange의 e.target.value = 입력창에 현재 적혀 있는 값 */}
          <input type="text" className="form-control" id="name"
                 placeholder="예: 김철수" maxLength="50" required
                 value={name} onChange={(e) => setName(e.target.value)} />
        </div>

        <div className="mb-3">
          <label htmlFor="title" className="form-label">제목</label>
          <input type="text" className="form-control" id="title"
                 placeholder="예: 성장하는 개발자 김철수입니다" maxLength="200" required
                 value={title} onChange={(e) => setTitle(e.target.value)} />
        </div>

        <div className="mb-3">
          <label htmlFor="content" className="form-label">자기소개</label>
          <textarea className="form-control" id="content" rows="10"
                    placeholder="자신을 자유롭게 소개해 주세요." maxLength="4000" required
                    value={content} onChange={(e) => setContent(e.target.value)} />
        </div>

        {/* disabled={saving}: 저장 요청이 진행되는 동안 버튼이 눌리지 않습니다 */}
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? '저장 중...' : isEdit ? '수정' : '등록'}
        </button>{' '}
        <button type="button" className="btn btn-outline-secondary" onClick={onCancel}>
          취소
        </button>
      </form>
    </div>
  )
}

export default IntroForm
