import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

/**
 * 갱신 분실(Lost Update): 트랜잭션을 써도 "읽고 → 자바에서 계산하고 → 쓰는" 코드는 돈이 사라질 수 있습니다.
 * (문서: 06_DB_내부_트랜잭션과_인덱스)
 *
 * 실행 (이 폴더에서, 03_Database 모듈의 H2 드라이버 사용):
 *   java -cp "../../../03_Database/샘플/jdbc/lib/h2-2.3.232.jar" "-Dfile.encoding=UTF-8" LostUpdateLab.java
 *
 * 상황: 잔액 10,000원 계좌에서 두 사람이 거의 동시에 3,000원과 5,000원을 출금 → 정답은 2,000원
 *  1) ❌ SELECT 로 읽고 자바에서 빼서 UPDATE balance = ? → 한쪽 출금이 사라짐
 *  2) ✅ UPDATE balance = balance - ? (DB가 한 문장 안에서 읽고 쓰기를 처리)
 *  3) ✅ SELECT ... FOR UPDATE (읽을 때 행을 잠가서 다른 트랜잭션이 기다리게 함)
 */
public class LostUpdateLab {

    // DB_CLOSE_DELAY=-1: 연결이 모두 닫혀도 인메모리 DB 유지 / LOCK_TIMEOUT: 잠금 대기 최대 10초
    static final String URL = "jdbc:h2:mem:bank;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000";

    public static void main(String[] args) throws Exception {
        try {
            run();
        } catch (SQLException e) {
            System.err.println("DB 오류: " + e.getMessage());
            System.err.println("H2 드라이버를 못 찾았다면 -cp 경로를 확인하세요 (README 참고).");
            System.exit(1);
        }
    }

    static void run() throws Exception {
        try (Connection admin = DriverManager.getConnection(URL, "sa", "")) {
            try (Statement st = admin.createStatement()) {
                st.execute("CREATE TABLE account (id INT PRIMARY KEY, balance INT NOT NULL)");
            }
            System.out.println("격리 수준(이 DB 기본값) = " + isolationName(admin.getTransactionIsolation()));
            System.out.println();

            reset(admin);
            System.out.println("=== 1. ❌ 읽고 → 자바에서 계산 → 덮어쓰기 ===");
            readModifyWrite();
            System.out.println("최종 잔액 = " + balance(admin) + "원  (정답 2,000원)  ← 3,000원 출금이 사라짐");

            reset(admin);
            System.out.println();
            System.out.println("=== 2. ✅ UPDATE 한 문장으로 계산까지 DB에서 ===");
            atomicUpdate();
            System.out.println("최종 잔액 = " + balance(admin) + "원");

            reset(admin);
            System.out.println();
            System.out.println("=== 3. ✅ SELECT ... FOR UPDATE 로 행 잠그기 ===");
            selectForUpdate();
            System.out.println("최종 잔액 = " + balance(admin) + "원");
        }
    }

    /** 두 트랜잭션의 실행 순서를 손으로 엇갈리게 만들어 갱신 분실을 확실히 재현한다. */
    static void readModifyWrite() throws SQLException {
        try (Connection a = open(); Connection b = open()) {
            int balA = readBalance(a, false);
            System.out.println("[A] 잔액 조회: " + balA);
            int balB = readBalance(b, false);
            System.out.println("[B] 잔액 조회: " + balB + "   (A가 아직 커밋 전이라 B도 10,000을 봄)");

            writeBalance(a, balA - 3000);
            a.commit();
            System.out.println("[A] " + balA + " - 3000 = " + (balA - 3000) + " 저장, 커밋");

            writeBalance(b, balB - 5000);
            b.commit();
            System.out.println("[B] " + balB + " - 5000 = " + (balB - 5000) + " 저장, 커밋   ← A의 결과를 덮어씀");
        }
    }

    static void atomicUpdate() throws SQLException {
        try (Connection a = open(); Connection b = open()) {
            String sql = "UPDATE account SET balance = balance - ? WHERE id = 1 AND balance >= ?";
            try (PreparedStatement pa = a.prepareStatement(sql); PreparedStatement pb = b.prepareStatement(sql)) {
                pa.setInt(1, 3000); pa.setInt(2, 3000);
                System.out.println("[A] UPDATE ... balance - 3000 → 변경된 행 " + pa.executeUpdate());
                a.commit();
                pb.setInt(1, 5000); pb.setInt(2, 5000);
                System.out.println("[B] UPDATE ... balance - 5000 → 변경된 행 " + pb.executeUpdate());
                b.commit();
            }
            System.out.println("→ 'balance >= ?' 조건 덕분에 잔액 부족이면 변경된 행이 0 → 자바에서 '잔액 부족' 처리");
        }
    }

    /** FOR UPDATE: A가 행을 잠근 동안 B의 SELECT ... FOR UPDATE 는 기다린다 (스레드 2개로 실제 동시 실행). */
    static void selectForUpdate() throws Exception {
        CountDownLatch aLocked = new CountDownLatch(1);
        long t0 = System.nanoTime();

        Thread ta = new Thread(() -> withdraw("A", 3000, aLocked, null, t0));
        Thread tb = new Thread(() -> withdraw("B", 5000, null, aLocked, t0));
        ta.start();
        tb.start();
        ta.join();
        tb.join();
    }

    private static void withdraw(String who, int amount, CountDownLatch signalAfterLock,
                                 CountDownLatch waitBeforeStart, long t0) {
        try (Connection c = open()) {
            if (waitBeforeStart != null) waitBeforeStart.await();     // B는 A가 잠근 뒤에 시작
            System.out.printf("[%s] %4dms SELECT ... FOR UPDATE 시도%n", who, ms(t0));
            int bal = readBalance(c, true);
            System.out.printf("[%s] %4dms 잠금 획득, 잔액 %d%n", who, ms(t0), bal);
            if (signalAfterLock != null) signalAfterLock.countDown();
            Thread.sleep(1000);                                        // 계산·검증에 1초 걸린다고 가정
            if (bal < amount) {
                System.out.printf("[%s] 잔액 부족으로 출금 취소%n", who);
                c.rollback();
                return;
            }
            writeBalance(c, bal - amount);
            c.commit();
            System.out.printf("[%s] %4dms %d - %d = %d 저장, 커밋 (잠금 해제)%n", who, ms(t0), bal, amount, bal - amount);
        } catch (SQLException e) {
            System.out.println("[" + who + "] DB 오류: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---- 도우미 메서드 ----
    private static Connection open() throws SQLException {
        Connection c = DriverManager.getConnection(URL, "sa", "");
        c.setAutoCommit(false);                    // 트랜잭션 시작
        return c;
    }

    private static int readBalance(Connection c, boolean forUpdate) throws SQLException {
        String sql = "SELECT balance FROM account WHERE id = 1" + (forUpdate ? " FOR UPDATE" : "");
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (!rs.next()) throw new SQLException("계좌가 없습니다");
            return rs.getInt(1);
        }
    }

    private static void writeBalance(Connection c, int newBalance) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE account SET balance = ? WHERE id = 1")) {
            ps.setInt(1, newBalance);
            ps.executeUpdate();
        }
    }

    private static void reset(Connection admin) throws SQLException {
        try (Statement st = admin.createStatement()) {
            st.execute("MERGE INTO account KEY(id) VALUES (1, 10000)");
        }
    }

    private static int balance(Connection admin) throws SQLException {
        try (Statement st = admin.createStatement();
             ResultSet rs = st.executeQuery("SELECT balance FROM account WHERE id = 1")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static long ms(long t0) {
        return (System.nanoTime() - t0) / 1_000_000;
    }

    private static String isolationName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "기타(" + level + ")";
        };
    }
}
