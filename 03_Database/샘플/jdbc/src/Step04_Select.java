import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * [4단계] 저장된 자기소개서 목록을 조회(SELECT)합니다.
 *
 * 핵심 등장인물:
 * - ResultSet : SELECT 결과 표를 한 행씩 읽게 해주는 커서.
 *   rs.next()가 "다음 행으로 이동, 행이 있으면 true"를 뜻하므로
 *   while (rs.next()) { ... } 패턴으로 전체를 순회합니다.
 *
 * 실행 방법:
 *   javac -encoding UTF-8 -d out src/Step04_Select.java
 *   java -cp "out;lib/h2-2.3.232.jar" Step04_Select
 */
public class Step04_Select {

    static final String URL = "jdbc:h2:./data/introdb";
    static final String USER = "sa";
    static final String PASSWORD = "";

    public static void main(String[] args) {
        String sql = "SELECT id, name, title, created_at FROM intro ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // SELECT는 executeQuery()

            System.out.println("== 자기소개서 목록 ==");
            int count = 0;
            while (rs.next()) {
                // 컬럼 이름으로 값을 꺼냅니다 (타입에 맞는 getXxx 사용)
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String title = rs.getString("title");
                var createdAt = rs.getTimestamp("created_at");

                System.out.printf("[%d] %s - %s (%s)%n", id, title, name, createdAt);
                count++;
            }
            if (count == 0) {
                System.out.println("(아직 등록된 자기소개서가 없습니다. Step03을 먼저 실행해 보세요)");
            }

        } catch (SQLException e) {
            System.out.println("실행 실패: " + e.getMessage());
        }
    }
}
