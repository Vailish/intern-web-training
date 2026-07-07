# JDBC 단계별 샘플 코드

[04. JDBC 실습](../../04_JDBC_실습.md) 문서의 완성 코드입니다. 단계 순서대로 보세요.

| 파일 | 내용 |
|---|---|
| `src/Step01_Connect.java` | DB 접속만 해보기 (Connection) |
| `src/Step02_CreateTable.java` | 자바에서 SQL 실행 (Statement, DDL) |
| `src/Step03_Insert.java` | 저장 (PreparedStatement, `?` 바인딩) |
| `src/Step04_Select.java` | 조회 (ResultSet 순회) |
| `src/Step05_IntroApp.java` | 완성판: 자기소개서 콘솔 앱 |
| `src/Step06_Transaction.java` | 트랜잭션 (commit / rollback) |

## 실행 방법 (PowerShell, 이 폴더에서)

```powershell
javac -encoding UTF-8 -d out src/Step01_Connect.java
java -cp "out;lib/h2-2.3.232.jar" Step01_Connect
```

- 다른 단계도 파일명만 바꿔 같은 방식으로 실행합니다. (macOS/리눅스는 `;` 대신 `:`)
- 실행하면 `data/` 폴더에 H2 DB 파일이 생깁니다. 지우고 처음부터 다시 해도 됩니다.
- `lib/mysql-connector-j-8.4.0.jar`는 [05 심화(도커로 MySQL)](../../05_심화_도커로_MySQL.md)에서 사용합니다.
