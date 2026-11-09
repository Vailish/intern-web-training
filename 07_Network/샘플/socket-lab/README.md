# socket-lab — TCP·UDP를 직접 다뤄 보는 실습 코드

스프링 없이 **자바 표준 소켓 API만으로** 만든 서버·클라이언트입니다. Gradle 빌드 없이 Java 11+ 의 "소스 파일 바로 실행" 기능으로 실행합니다.

| 파일 | 포트 | 설명 | 사용하는 문서 |
|---|---|---|---|
| `TcpEchoServer.java` | 9000 | 받은 줄을 `[echo]` 를 붙여 돌려줌. 접속한 클라이언트의 IP:포트 출력 | 01, 04 |
| `TcpClient.java` | (대상 지정) | 입력한 줄을 CRLF로 보내고 받은 것을 모두 출력. **연결 실패 시 실패 단계(DNS/TCP)와 의미를 설명** | 04, 05, 06 |
| `UdpServer.java` | 9001 | UDP 에코 서버 | 04 |
| `UdpClient.java` | (대상 지정) | 보내고 2초간 답장을 기다림. 서버가 없어도 send()는 성공함을 보여 줌 | 04 |

## 실행 (PowerShell)

```powershell
java "-Dfile.encoding=UTF-8" TcpEchoServer.java              # 서버 (Ctrl+C 로 종료)
java "-Dfile.encoding=UTF-8" TcpClient.java                  # localhost 9000 에 접속
java "-Dfile.encoding=UTF-8" TcpClient.java localhost 8080   # 스프링 앱에 HTTP 직접 보내기
java "-Dfile.encoding=UTF-8" UdpServer.java
java "-Dfile.encoding=UTF-8" UdpClient.java
```

- 클라이언트 종료: **Ctrl+Z 후 Enter** (macOS·리눅스는 Ctrl+D)
- `"-Dfile.encoding=UTF-8"` 은 **따옴표째** 붙입니다.
  - 빼면 한글 Windows + Java 17에서 소스를 MS949로 읽어 **한글 메시지가 깨집니다.**
  - 따옴표가 없으면 PowerShell 5.1이 인자를 `.` 에서 잘라 `Could not find or load main class .encoding=UTF-8` 이 납니다.
- IntelliJ에서 파일을 열고 `main` 옆 ▶ 버튼으로 실행해도 됩니다(콘솔 입력은 Run 창에서).

## 주의
- PowerShell 파이프(`"hello" | java ...`)로 입력을 넣으면 앞에 보이지 않는 문자(BOM)가 붙을 수 있습니다. **직접 타이핑**하세요.
- 학습용 코드라 보안·성능 처리는 최소화했습니다. 실무 서버 코드의 참고용이 아닙니다.
