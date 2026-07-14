// ============================================================================
// main.jsx — React 앱의 진입점(entry point)
//
// index.html의 <script src="/src/main.jsx">가 이 파일을 실행하면서
// React 앱이 시작됩니다. 스프링부트의 main() 메서드에 해당하는 파일이라고
// 생각하면 됩니다. 보통 이 파일은 한 번 만들어 두면 건드릴 일이 없습니다.
// ============================================================================

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css' // CSS도 import로 불러옵니다 (Vite가 알아서 <style>로 넣어줌)
import App from './App.jsx'

// index.html의 <div id="root">를 찾아서, 그 안에 <App /> 컴포넌트를 그립니다.
// 이 한 줄이 "빈 div가 화면으로 바뀌는" 마법의 시작점입니다.
createRoot(document.getElementById('root')).render(
  // StrictMode: 개발 중 실수를 찾아주는 React의 검사 모드입니다.
  //
  // ⚠️ 알아둘 것: StrictMode는 "개발 모드에서만" 컴포넌트를 일부러 두 번씩
  // 실행해 봅니다. 그래서 개발자 도구(F12) Network 탭을 보면 목록 조회 API가
  // 두 번 호출된 것처럼 보입니다. 버그가 아니고, "두 번 실행돼도 문제없는
  // 코드인지" React가 검사하는 것입니다. 실제 배포(build)하면 한 번만 실행됩니다.
  <StrictMode>
    <App />
  </StrictMode>,
)
