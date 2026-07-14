// ============================================================================
// introApi.js — 백엔드 REST API를 호출하는 함수 모음
//
// 서버와 통신하는 코드를 이렇게 한 파일에 모아 두면:
//   1. 화면 컴포넌트(.jsx)에는 "화면 그리는 코드"만 남아 깔끔해집니다.
//   2. API 주소가 바뀌어도 이 파일만 고치면 됩니다.
// 스프링에서 컨트롤러(화면 담당)와 서비스/리포지토리(데이터 담당)를
// 나눈 것과 같은 "관심사 분리"입니다.
//
// 여기서 쓰는 fetch()는 브라우저에 내장된 HTTP 요청 함수입니다.
// (별도 설치 없이 사용 가능. 실무에서는 axios라는 라이브러리도 많이 씁니다)
// ============================================================================

// 백엔드 서버 주소. 스프링부트(intro-api)가 8080 포트에서 돌고 있어야 합니다.
// React 개발 서버(5173)와 "다른 출처"라서 백엔드에 CORS 설정이 필요합니다.
// → intro-api의 config/WebConfig.java 참고!
const API_BASE_URL = 'http://localhost:8080'

/**
 * 모든 API 호출이 공통으로 거치는 함수입니다.
 *
 * async/await 문법:
 *   - 서버 응답은 "언젠가" 도착합니다(0.1초 뒤일 수도, 3초 뒤일 수도).
 *   - await는 "응답이 올 때까지 기다렸다가 다음 줄을 실행해라"는 뜻입니다.
 *   - await를 쓰는 함수에는 async를 붙여야 합니다.
 *
 * 에러 처리:
 *   - fetch()는 404, 500 같은 실패 응답에도 예외를 던지지 않습니다!
 *     (네트워크가 아예 끊겼을 때만 예외 발생)
 *   - 그래서 response.ok (상태 코드가 200번대인지)를 반드시 직접 확인하고,
 *     실패면 서버가 보낸 에러 메시지를 꺼내 throw 합니다.
 *   - 이렇게 던진 에러는 화면 컴포넌트의 try...catch가 받아서 사용자에게 보여줍니다.
 */
async function request(url, options) {
  const response = await fetch(API_BASE_URL + url, options)

  if (!response.ok) {
    // 백엔드의 @ExceptionHandler가 보낸 { "message": "..." }를 꺼내 봅니다.
    // 에러 응답의 본문이 JSON이 아닐 수도 있으니 catch로 방어합니다.
    const errorBody = await response.json().catch(() => null)
    throw new Error(errorBody?.message ?? `요청 실패 (HTTP ${response.status})`)
  }

  // 204 No Content(삭제 성공)는 본문이 없으므로 json()을 부르면 안 됩니다.
  if (response.status === 204) {
    return null
  }

  return response.json() // JSON 문자열 → JavaScript 객체로 변환
}

/** [R] 목록 조회: GET /api/intros → 자기소개서 배열 */
export function fetchIntros() {
  return request('/api/intros')
}

/** [R] 한 건 조회: GET /api/intros/{id} → 자기소개서 객체 */
export function fetchIntro(id) {
  return request(`/api/intros/${id}`)
}

/**
 * [C] 등록: POST /api/intros
 * @param {object} data - { name, title, content }
 *
 * intro-jpa에서는 <form method="post">가 알아서 보내주던 것을
 * 이제 JavaScript가 직접 조립해서 보냅니다:
 *   - method: HTTP 메서드 지정
 *   - headers: "본문이 JSON"이라고 서버에 알려줌 (없으면 서버가 못 읽습니다!)
 *   - body: 객체를 JSON 문자열로 변환 ({name:"홍길동"} → '{"name":"홍길동"}')
 * 서버에서는 @RequestBody가 이 JSON을 IntroRequest 객체로 되돌립니다.
 */
export function createIntro(data) {
  return request('/api/intros', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
}

/** [U] 수정: PUT /api/intros/{id} */
export function updateIntro(id, data) {
  return request(`/api/intros/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
}

/** [D] 삭제: DELETE /api/intros/{id} (성공 시 서버가 204를 주므로 반환값은 null) */
export function deleteIntro(id) {
  return request(`/api/intros/${id}`, { method: 'DELETE' })
}

/**
 * 날짜 표시용 헬퍼: 서버가 주는 "2026-07-14T10:30:00.123" (ISO 8601 형식)을
 * 화면에 보여줄 "2026-07-14 10:30" 로 바꿉니다.
 * (intro-jpa에서 타임리프의 #temporals.format()이 하던 일을 이제 JS가 합니다)
 */
export function formatDateTime(isoString) {
  if (!isoString) return ''
  return isoString.substring(0, 16).replace('T', ' ')
}
