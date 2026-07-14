# intro-react — 자기소개서 프론트엔드 (React + Vite)

[intro-api](../intro-api/) 백엔드의 REST API를 호출해 자기소개서 CRUD 화면을 그리는
React 앱입니다. 자세한 학습 문서는 [Front-Back 모듈 README](../../README.md)를 보세요.

## 실행 방법

**백엔드(intro-api)가 8080 포트에 먼저 떠 있어야 합니다!**

```powershell
# 1. (최초 1회) 라이브러리 설치 — node_modules 폴더가 없다면 반드시 실행
npm install

# 2. 개발 서버 실행
npm run dev
```

브라우저에서 `http://localhost:5173` 접속. 종료는 콘솔에서 `Ctrl + C`.

## 파일 읽는 순서 (주석이 강의 노트입니다)

| 순서 | 파일 | 내용 |
|---|---|---|
| 1 | [index.html](./index.html) | SPA의 뼈대 — 비어 있는 div 하나가 전부인 이유 |
| 2 | [src/main.jsx](./src/main.jsx) | React 앱의 시작점, StrictMode |
| 3 | [src/App.jsx](./src/App.jsx) | state로 화면 전환하기 (컨트롤러의 역할 이동) |
| 4 | [src/api/introApi.js](./src/api/introApi.js) | fetch로 REST API 호출, 에러 처리 |
| 5 | [src/components/IntroList.jsx](./src/components/IntroList.jsx) | useEffect로 데이터 불러오기, map 반복 렌더링 |
| 6 | [src/components/IntroDetail.jsx](./src/components/IntroDetail.jsx) | props로 id 받기, DELETE 호출 |
| 7 | [src/components/IntroForm.jsx](./src/components/IntroForm.jsx) | 제어 컴포넌트(입력값 state 관리), POST/PUT |
