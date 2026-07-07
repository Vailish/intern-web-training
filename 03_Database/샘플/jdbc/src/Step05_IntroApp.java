import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * [5단계] 완성판: 콘솔에서 동작하는 "자기소개서 등록/조회" 프로그램.
 *
 * Step01~04에서 배운 것을 하나로 합쳤습니다.
 * 메뉴: 1. 목록 / 2. 등록 / 3. 상세 보기 / 0. 종료
 *
 * 눈여겨볼 점:
 * - 목록/등록/상세가 각각 메서드로 분리되어 있습니다.
 *   (스프링 교육에서 배울 Controller-Service-Repository 분리의 축소판입니다)
 * - 모든 SQL이 ? 바인딩(PreparedStatement)을 씁니다.
 *
 * 실행 방법:
 *   javac -encoding UTF-8 -d out src/Step05_IntroApp.java
 *   java -cp "out;lib/h2-2.3.232.jar" Step05_IntroApp
 */
public class Step05_IntroApp {

    static final String URL = "jdbc:h2:./data/introdb";
    static final String USER = "sa";
    static final String PASSWORD = "";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 시작할 때 테이블이 없으면 만들어 둡니다 (Step02와 동일)
        createTableIfNotExists();

        while (true) {
            System.out.println();
            System.out.println("==== 자기소개서 관리 ====");
            System.out.println("1. 목록  2. 등록  3. 상세 보기  0. 종료");
            System.out.print("선택> ");
            String menu = sc.nextLine().trim();

            switch (menu) {
                case "1" -> printList();
                case "2" -> insert(sc);
                case "3" -> printDetail(sc);
                case "0" -> {
                    System.out.println("종료합니다.");
                    return;
                }
                default -> System.out.println("1, 2, 3, 0 중에서 선택해 주세요.");
            }
        }
    }

    /** 테이블 준비 (없을 때만 생성) */
    static void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS intro (
                    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name       VARCHAR(50)  NOT NULL,
                    title      VARCHAR(200) NOT NULL,
                    content    VARCHAR(4000),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            System.out.println("테이블 준비 실패: " + e.getMessage());
        }
    }

    /** 1. 목록: 최신 글이 위로 */
    static void printList() {
        String sql = "SELECT id, name, title, created_at FROM intro ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("-- 번호 | 제목 (이름, 작성시각) --");
            boolean empty = true;
            while (rs.next()) {
                System.out.printf("%d | %s (%s, %s)%n",
                        rs.getLong("id"), rs.getString("title"),
                        rs.getString("name"), rs.getTimestamp("created_at"));
                empty = false;
            }
            if (empty) System.out.println("(등록된 자기소개서가 없습니다)");

        } catch (SQLException e) {
            System.out.println("조회 실패: " + e.getMessage());
        }
    }

    /** 2. 등록: 입력받아 INSERT */
    static void insert(Scanner sc) {
        System.out.print("이름: ");
        String name = sc.nextLine().trim();
        System.out.print("제목: ");
        String title = sc.nextLine().trim();
        System.out.print("자기소개(한 줄): ");
        String content = sc.nextLine();

        if (name.isEmpty() || title.isEmpty()) {
            System.out.println("이름과 제목은 필수입니다. (NOT NULL 컬럼!)");
            return;
        }

        String sql = "INSERT INTO intro (name, title, content) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.executeUpdate();
            System.out.println("등록 완료!");

        } catch (SQLException e) {
            System.out.println("등록 실패: " + e.getMessage());
        }
    }

    /** 3. 상세 보기: 번호로 한 건 조회 */
    static void printDetail(Scanner sc) {
        System.out.print("번호: ");
        String input = sc.nextLine().trim();

        long id;
        try {
            id = Long.parseLong(input); // 숫자가 아니면 여기서 예외
        } catch (NumberFormatException e) {
            System.out.println("숫자를 입력해 주세요.");
            return;
        }

        String sql = "SELECT * FROM intro WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("== " + rs.getString("title") + " ==");
                    System.out.println("작성자: " + rs.getString("name")
                            + " / 작성시각: " + rs.getTimestamp("created_at"));
                    System.out.println(rs.getString("content"));
                } else {
                    System.out.println("해당 번호의 자기소개서가 없습니다: " + id);
                }
            }

        } catch (SQLException e) {
            System.out.println("조회 실패: " + e.getMessage());
        }
    }
}
