import java.util.*;

/**
 * [04단계] 그리디 — 매 순간 최선
 *
 *   (1) 거스름돈: 큰 동전부터 (배수 체계에서만 최적)
 *   (2) 회의실 배정: 끝나는 시간이 빠른 것부터
 *   (3) 그리디가 "틀리는" 예: 동전 {1,4,5}로 8원
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex11_Greedy.java
 *   java -cp out Ex11_Greedy
 */
public class Ex11_Greedy {

    /** 거스름돈 최소 동전 개수 (coins는 내림차순 가정) */
    static int change(int[] coins, int amount) {
        int count = 0;
        for (int coin : coins) {
            count += amount / coin;    // 이 동전을 최대한
            amount %= coin;
        }
        return count;
    }

    public static void main(String[] args) {
        // ---- (1) 거스름돈 (배수 동전 → 그리디 최적) ----
        int[] coins = {500, 100, 50, 10};
        int amount = 1260;
        System.out.println("[거스름돈] " + amount + "원 → " + change(coins, amount) + "개");
        System.out.println("  (500×2 + 100×2 + 50×1 + 10×1 = 6개)");

        // ---- (2) 회의실 배정: 끝나는 시간 빠른 순 ----
        int[][] meetings = {{1, 4}, {3, 5}, {0, 6}, {5, 7}, {3, 8}, {5, 9}, {6, 10}, {8, 11}, {8, 12}, {2, 13}, {12, 14}};
        Arrays.sort(meetings, (a, b) -> a[1] != b[1] ? a[1] - b[1] : a[0] - b[0]); // 끝나는 시간 오름차순
        int cnt = 0, endTime = 0;
        for (int[] m : meetings) {
            if (m[0] >= endTime) {     // 이전 회의가 끝난 뒤 시작 가능
                cnt++;
                endTime = m[1];        // 이 회의를 채택
            }
        }
        System.out.println("\n[회의실 배정] 최대 " + cnt + "개 회의 배정 가능");

        // ---- (3) 그리디가 틀리는 예 ----
        int[] badCoins = {5, 4, 1};
        int target = 8;
        int greedyResult = change(badCoins, target);   // 5+1+1+1 = 4개
        System.out.println("\n[그리디의 함정] 동전 {5,4,1}로 " + target + "원 거슬러주기");
        System.out.println("  그리디(큰 것부터): " + greedyResult + "개  ← 5+1+1+1");
        System.out.println("  실제 최적:         2개  ← 4+4");
        System.out.println("  → 배수 관계가 아닌 동전은 그리디가 틀림! (이런 건 DP로 풀어야 함)");
    }
}
