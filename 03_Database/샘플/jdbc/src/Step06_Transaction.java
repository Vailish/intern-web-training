import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * [6단계] 트랜잭션: "전부 성공 아니면 전부 취소"
 *
 * 시나리오: 자기소개서 2건을 "한 묶음"으로 저장하는데,
 * 두 번째 건에 일부러 오류(제목 NULL)를 만들어 둡니다.
 *
 * - 자동커밋(기본값)이라면: 1건은 저장되고 1건만 실패 → 어중간한 상태
 * - 트랜잭션으로 묶으면: 둘 다 취소(rollback) → 깔끔한 원상복구
 *
 * 계좌이체를 떠올리면 됩니다. "출금은 됐는데 입금이 실패"가 허용되면 안 되죠.
 *
 * 실행 방법:
 *   javac -encoding UTF-8 -d out src/Step06_Transaction.java
 *   java -cp "out;lib/h2-2.3.232.jar" Step06_Transaction
 */
public class Step06_Transaction {

    static final String URL = "jdbc:h2:./data/introdb";
    static final String USER = "sa";
    static final String PASSWORD = "";

    public static void main(String[] args) {
        String sql = "INSERT INTO intro (name, title, content) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            conn.setAutoCommit(false); // 지금부터 내가 commit 하기 전까지는 "임시 저장" 상태

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                // 1건째: 정상 데이터
                ps.setString(1, "박민준");
                ps.setString(2, "트랜잭션 실습 1건째");
                ps.setString(3, "이 글은 커밋 전의 임시 데이터입니다.");
                ps.executeUpdate();
                System.out.println("1건째 INSERT 성공 (아직 임시 상태)");

                // 2건째: 일부러 오류 발생 — title은 NOT NULL인데 null을 넣음
                ps.setString(1, "박민준");
                ps.setString(2, null);
                ps.setString(3, "이 글은 저장되면 안 됩니다.");
                ps.executeUpdate(); // 여기서 SQLException 발생!

                conn.commit(); // 여기까지 와야 진짜 저장
                System.out.println("2건 모두 커밋 완료");

            } catch (SQLException e) {
                conn.rollback(); // 묶음 전체 취소 — 1건째도 함께 사라집니다
                System.out.println("오류 발생 → 롤백했습니다: " + e.getMessage());
                System.out.println("Step04_Select를 실행해 보세요. 1건째도 저장되지 않았습니다!");
            }

        } catch (SQLException e) {
            System.out.println("접속 실패: " + e.getMessage());
        }
    }
}
