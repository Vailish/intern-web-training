import java.util.*;

/**
 * [02단계] List / Map / Set 실전 패턴
 *
 * 코테에서 가장 자주 쓰는 세 가지:
 *   (1) Map 으로 빈도 세기 (getOrDefault / merge)
 *   (2) Set 으로 중복 제거 & O(1) 존재 확인
 *   (3) Map 으로 Two Sum (O(N^2) → O(N))
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex03_ListMapSet.java
 *   java -cp out Ex03_ListMapSet
 */
public class Ex03_ListMapSet {

    public static void main(String[] args) {
        // ---- (1) 빈도 세기 ----
        String[] words = {"apple", "banana", "apple", "cherry", "apple", "banana"};
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            freq.put(w, freq.getOrDefault(w, 0) + 1);   // 없으면 0에서 시작해 +1
        }
        System.out.println("[빈도] " + freq);            // {banana=2, cherry=1, apple=3}
        System.out.println(" apple 등장 횟수 = " + freq.get("apple"));

        // 가장 많이 나온 단어 찾기
        String top = null;
        int best = -1;
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            if (e.getValue() > best) { best = e.getValue(); top = e.getKey(); }
        }
        System.out.println(" 최다 단어 = " + top + " (" + best + "회)");

        // ---- (2) 중복 제거 & 존재 확인 ----
        int[] nums = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3};
        Set<Integer> set = new HashSet<>();
        for (int x : nums) set.add(x);               // 중복 자동 제거
        System.out.println("\n[중복 제거] " + new TreeSet<>(set));  // 정렬해서 보기
        System.out.println(" 5가 있나? " + set.contains(5) + " (O(1) 조회)");
        System.out.println(" 7이 있나? " + set.contains(7));

        // ---- (3) Two Sum: 합이 target인 두 수의 인덱스 (O(N)) ----
        int target = 9;
        int[] arr = {2, 7, 11, 15};
        Map<Integer, Integer> seen = new HashMap<>();   // 값 → 인덱스
        int[] answer = null;
        for (int i = 0; i < arr.length; i++) {
            int need = target - arr[i];
            if (seen.containsKey(need)) {               // 짝을 이미 봤다면
                answer = new int[]{seen.get(need), i};
                break;
            }
            seen.put(arr[i], i);
        }
        System.out.println("\n[Two Sum] target=" + target
                + " → 인덱스 " + Arrays.toString(answer)
                + " (값 " + arr[answer[0]] + "+" + arr[answer[1]] + ")");
        System.out.println(" 이중 반복 O(N^2) 없이 해시로 O(N)에 해결.");
    }
}
