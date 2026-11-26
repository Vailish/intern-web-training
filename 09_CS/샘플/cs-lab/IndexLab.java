import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DB 인덱스: 인덱스가 없을 때와 있을 때, 그리고 "있는데도 안 쓰일 때"를 실행 계획(EXPLAIN)으로 확인합니다.
 * (문서: 06_DB_내부_트랜잭션과_인덱스)
 *
 * 실행 (이 폴더에서, 03_Database 모듈의 H2 드라이버 사용):
 *   java -cp "../../../03_Database/샘플/jdbc/lib/h2-2.3.232.jar" "-Dfile.encoding=UTF-8" IndexLab.java
 *
 * 관찰 포인트
 *  - 인덱스 없음: tableScan (30만 건을 처음부터 끝까지 확인)
 *  - 인덱스 있음: IDX_MEMBER_EMAIL 사용 (책 맨 뒤 '찾아보기'로 바로 이동)
 *  - LIKE '%값', 컬럼에 함수 씌우기 → 인덱스가 있어도 tableScan 으로 돌아간다
 *  - 인메모리 DB(jdbc:h2:mem:)라 실행이 끝나면 데이터가 사라지고 파일도 남지 않는다
 */
public class IndexLab {

    static final int ROWS = 300_000;

    public static void main(String[] args) {
        // 인메모리 DB: 프로그램 종료 시 사라짐
        try (Connection con = DriverManager.getConnection("jdbc:h2:mem:indexlab", "sa", "")) {
            setup(con);

            String target = "user250000@example.com";
            System.out.println("=== 1. 인덱스 없이 email 로 1건 찾기 ===");
            explain(con, "SELECT * FROM member WHERE email = '" + target + "'");
            time(con, target);

            System.out.println();
            System.out.println("=== 2. email 에 인덱스 만들기 ===");
            long t = System.nanoTime();
            try (Statement st = con.createStatement()) {
                st.execute("CREATE INDEX idx_member_email ON member(email)");
            }
            System.out.printf("CREATE INDEX 소요 %dms  (인덱스는 공짜가 아니다: 만들 때·INSERT/UPDATE 때마다 비용)%n",
                    (System.nanoTime() - t) / 1_000_000);
            explain(con, "SELECT * FROM member WHERE email = '" + target + "'");
            time(con, target);

            System.out.println();
            System.out.println("=== 3. 인덱스가 있어도 못 쓰는 경우 ===");
            System.out.println("[앞부분 일치 LIKE 'user25%'] → 정렬된 인덱스에서 범위로 찾을 수 있음");
            explain(con, "SELECT * FROM member WHERE email LIKE 'user25%'");
            System.out.println("[중간 일치 LIKE '%250000%'] → 어디서 시작할지 모르니 전부 확인");
            explain(con, "SELECT * FROM member WHERE email LIKE '%250000%'");
            System.out.println("[컬럼에 함수 LOWER(email) = ...] → 인덱스엔 원래 값이 정렬돼 있지 LOWER 결과가 아님");
            explain(con, "SELECT * FROM member WHERE LOWER(email) = '" + target + "'");
        } catch (SQLException e) {
            System.err.println("DB 오류: " + e.getMessage());
            System.err.println("H2 드라이버를 못 찾았다면 -cp 경로를 확인하세요 (README 참고).");
            System.exit(1);
        }
    }

    private static void setup(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute("CREATE TABLE member (id BIGINT PRIMARY KEY, email VARCHAR(100), name VARCHAR(50), age INT)");
        }
        long t = System.nanoTime();
        con.setAutoCommit(false);                       // 30만 건을 한 트랜잭션으로 넣어야 빠름
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO member VALUES (?, ?, ?, ?)")) {
            for (int i = 1; i <= ROWS; i++) {
                ps.setLong(1, i);
                ps.setString(2, "user" + i + "@example.com");
                ps.setString(3, "회원" + i);
                ps.setInt(4, 20 + i % 40);
                ps.addBatch();
                if (i % 10_000 == 0) ps.executeBatch();  // 1만 건씩 묶어서 전송 (배치)
            }
            ps.executeBatch();
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
        System.out.printf("회원 %,d건 준비 완료 (%dms)%n%n", ROWS, (System.nanoTime() - t) / 1_000_000);
    }

    /** EXPLAIN 결과에서 어떤 방식으로 찾는지(tableScan / 인덱스 이름) 보여 준다. */
    private static void explain(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("EXPLAIN " + sql)) {
            if (rs.next()) {
                String plan = rs.getString(1).replaceAll("\\s+", " ");
                String how = plan.contains("tableScan") ? "❌ 전체 스캔(tableScan)" : "✅ 인덱스 사용";
                System.out.println("  실행계획: " + how);
                System.out.println("    " + plan);
            }
        }
    }

    /**
     * 서로 다른 email 100개를 조회해 평균 시간을 잰다.
     * (같은 값으로 반복하면 DB가 이전 결과를 재사용해 실제보다 빠르게 나올 수 있어 매번 값을 바꾼다)
     */
    private static void time(Connection con, String email) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT name FROM member WHERE email = ?")) {
            for (int i = 0; i < 5; i++) {                                  // 워밍업
                ps.setString(1, "user" + (100_000 + i) + "@example.com");
                try (ResultSet rs = ps.executeQuery()) { rs.next(); }
            }
            int repeat = 100;
            int found = 0;
            long t = System.nanoTime();
            for (int i = 0; i < repeat; i++) {
                ps.setString(1, "user" + (200_000 + i * 997) + "@example.com");   // 매번 다른 회원
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) found++;
                }
            }
            double avgMs = (System.nanoTime() - t) / 1_000_000.0 / repeat;
            String name;
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { name = rs.next() ? rs.getString(1) : "(없음)"; }
            System.out.printf("  %s 조회 결과=%s / 서로 다른 회원 %d명 조회, 1회 평균 %.3fms%n", email, name, found, avgMs);
        }
    }
}
