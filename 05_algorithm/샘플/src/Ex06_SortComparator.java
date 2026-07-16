import java.util.*;

/**
 * [03단계] Comparator로 원하는 기준 정렬 + 뺄셈 비교의 함정
 *
 *   (1) 다중 기준 정렬: 점수 내림차순, 같으면 이름 오름차순
 *   (2) int[] 는 내림차순 정렬이 없음 → 대안
 *   (3) a - b 비교의 오버플로 위험
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex06_SortComparator.java
 *   java -cp out Ex06_SortComparator
 */
public class Ex06_SortComparator {

    record Student(String name, int score) {}

    public static void main(String[] args) {
        // ---- (1) 다중 기준 정렬 ----
        List<Student> students = new ArrayList<>(List.of(
                new Student("김철수", 90),
                new Student("이영희", 95),
                new Student("박민준", 90),
                new Student("최지우", 95)
        ));
        students.sort(
                Comparator.comparingInt(Student::score).reversed()  // 점수 내림차순
                          .thenComparing(Student::name)             // 같으면 이름 오름차순
        );
        System.out.println("[다중 기준 정렬] 점수 내림차순, 동점은 이름 오름차순");
        for (Student s : students) System.out.println("  " + s.score() + " " + s.name());

        // ---- (2) int[] 내림차순: 직접은 안 됨 → Integer[] 로 ----
        Integer[] arr = {5, 2, 8, 1, 9};
        Arrays.sort(arr, Collections.reverseOrder());
        System.out.println("\n[Integer[] 내림차순] " + Arrays.toString(arr));

        // ---- (3) 뺄셈 비교의 오버플로 함정 ----
        int big = 2_000_000_000, smallNeg = -2_000_000_000;
        // big - smallNeg = 40억 → int 범위(21억) 초과 → 음수로 뒤집힘 → 비교 결과가 틀림!
        System.out.println("\n[뺄셈 비교 함정]");
        System.out.println("  big - smallNeg = " + (big - smallNeg) + "  (음수로 오버플로!)");
        System.out.println("  Integer.compare(big, smallNeg) = " + Integer.compare(big, smallNeg)
                + "  (올바름: 1)");
        System.out.println("→ 정렬 비교자에서는 (a-b) 대신 Integer.compare(a,b) 를 쓰세요.");
    }
}
