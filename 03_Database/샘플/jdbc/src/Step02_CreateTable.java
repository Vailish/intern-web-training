import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * [2단계] 자바 코드에서 SQL을 실행해 테이블을 만듭니다.
 *
 * 핵심 등장인물:
 * - Statement : SQL 문장을 DB에 전달해 실행하는 심부름꾼
 *
 * 실행 방법:
 *   javac -encoding UTF-8 -d out src/Step02_CreateTable.java
 *   java -cp "out;lib/h2-2.3.232.jar" Step02_CreateTable
 */
public class Step02_CreateTable {

    static final String URL = "jdbc:h2:./data/introdb";
    static final String USER = "sa";
    static final String PASSWORD = "";

    public static void main(String[] args) {
        // 03 문서(SQL 기초)에서 콘솔에 직접 입력했던 그 DDL과 똑같습니다.
        String sql = """
                CREATE TABLE IF NOT EXISTS intro (
                    id         BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 자동증가 기본키
                    name       VARCHAR(50)  NOT NULL,              -- 작성자 이름
                    title      VARCHAR(200) NOT NULL,              -- 제목
                    content    VARCHAR(4000),                      -- 내용
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 작성시각(자동 기록)
                )
                """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("intro 테이블 준비 완료!");
            System.out.println("(IF NOT EXISTS 덕분에 여러 번 실행해도 안전합니다)");

        } catch (SQLException e) {
            System.out.println("실행 실패: " + e.getMessage());
        }
    }
}
