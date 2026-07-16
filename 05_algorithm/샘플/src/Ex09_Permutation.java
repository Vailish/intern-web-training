import java.util.*;

/**
 * [04단계] 순열과 조합 — 재귀로 경우의 수 생성
 *
 *   조합(Combination): 순서 무관, (1,2)=(2,1)
 *   순열(Permutation): 순서 중요, (1,2)≠(2,1)
 *
 * 공통 뼈대 = 선택 → 재귀 → 선택 취소(백트래킹)
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex09_Permutation.java
 *   java -cp out Ex09_Permutation
 */
public class Ex09_Permutation {

    static int[] arr = {1, 2, 3, 4};
    static int r = 2;                 // 뽑을 개수
    static List<List<Integer>> results = new ArrayList<>();

    /** 조합: start부터 뒤로만 진행해 중복(순서 다른 같은 조합)을 막음 */
    static void combination(int start, List<Integer> picked) {
        if (picked.size() == r) {
            results.add(new ArrayList<>(picked));
            return;
        }
        for (int i = start; i < arr.length; i++) {
            picked.add(arr[i]);              // 선택
            combination(i + 1, picked);      // i+1 → 앞으로만
            picked.remove(picked.size() - 1);// 선택 취소
        }
    }

    /** 순열: visited로 이미 쓴 것만 제외, 매번 0부터 후보 → 순서가 생김 */
    static void permutation(boolean[] visited, List<Integer> picked) {
        if (picked.size() == r) {
            results.add(new ArrayList<>(picked));
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            if (visited[i]) continue;
            visited[i] = true;               // 선택
            picked.add(arr[i]);
            permutation(visited, picked);
            picked.remove(picked.size() - 1);// 선택 취소
            visited[i] = false;
        }
    }

    public static void main(String[] args) {
        System.out.println("원소 " + Arrays.toString(arr) + " 에서 " + r + "개 뽑기\n");

        results.clear();
        combination(0, new ArrayList<>());
        System.out.println("[조합] " + results.size() + "가지 (순서 무관)");
        for (List<Integer> c : results) System.out.println("  " + c);

        results.clear();
        permutation(new boolean[arr.length], new ArrayList<>());
        System.out.println("\n[순열] " + results.size() + "가지 (순서 중요)");
        for (List<Integer> p : results) System.out.println("  " + p);
    }
}
