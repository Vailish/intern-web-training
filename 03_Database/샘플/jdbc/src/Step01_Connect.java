import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * [1단계] 데이터베이스에 "접속"만 해봅니다.
 *
 * 핵심 등장인물:
 * - DriverManager : JDBC URL을 보고 알맞은 드라이버를 찾아 접속을 만들어 주는 관리자
 * - Connection    : DB와 연결된 통로. 모든 작업은 이 통로를 통해 이루어집니다.
 *
 * 실행 방법 (샘플/jdbc 폴더에서):
 *   javac -encoding UTF-8 -d out src/Step01_Connect.java
 *   java -cp "out;lib/h2-2.3.232.jar" Step01_Connect
 */
public class Step01_Connect {

    // 접속 정보 3요소: URL / 사용자 / 비밀번호 (02 문서에서 배운 그것!)
    static final String URL = "jdbc:h2:./data/introdb";
    static final String USER = "sa";
    static final String PASSWORD = "";

    public static void main(String[] args) {
        // try(...) 괄호 안에서 만든 자원은 블록이 끝나면 자동으로 닫힙니다.
        // (= try-with-resources. 접속을 닫지 않으면 자원이 새어 나갑니다)
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("접속 성공!");
            System.out.println("DB 제품명: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("DB 버전  : " + conn.getMetaData().getDatabaseProductVersion());

        } catch (SQLException e) {
            // 접속 실패: URL 오타, DB 서버 꺼짐, 비밀번호 오류 등
            System.out.println("접속 실패: " + e.getMessage());
        }
    }
}
