import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * [3단계] 자기소개서 한 건을 저장(INSERT)합니다.
 *
 * 핵심 등장인물:
 * - PreparedStatement : 값이 들어갈 자리를 ? 로 비워 둔 SQL.
 *   ? 에 값을 끼워 넣는 일은 드라이버가 안전하게 처리합니다.
 *
 * 왜 문자열을 + 로 이어붙이지 않고 ? 를 쓰나요?
 *   "SQL Injection" 공격을 막기 위해서입니다. (04 문서에서 자세히!)
 *   사용자가 입력한 값에 SQL 조각이 섞여 있어도, ? 바인딩은
 *   그것을 "그냥 글자"로 취급하므로 안전합니다.
 *
 * 실행 방법:
 *   javac -encoding UTF-8 -d out src/Step03_Insert.java
 *   java -cp "out;lib/h2-2.3.232.jar" Step03_Insert
 */
public class Step03_Insert {

    static final String URL = "jdbc:h2:./data/introdb";
    static final String USER = "sa";
    static final String PASSWORD = "";

    public static void main(String[] args) {
        String sql = "INSERT INTO intro (name, title, content) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "김철수");                       // 첫 번째 ?
            ps.setString(2, "성장하는 개발자 김철수입니다");   // 두 번째 ?
            ps.setString(3, "안녕하세요. JDBC로 저장한 첫 자기소개서입니다."); // 세 번째 ?

            int rows = ps.executeUpdate(); // INSERT/UPDATE/DELETE는 executeUpdate()
            System.out.println("저장 완료! (" + rows + "건 반영)");
            System.out.println("created_at은 DEFAULT 덕분에 DB가 알아서 기록했습니다.");

        } catch (SQLException e) {
            System.out.println("실행 실패: " + e.getMessage());
        }
    }
}
