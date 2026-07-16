import java.util.*;

/**
 * [06단계] 0/1 배낭 문제 — 2차원 DP
 *
 * 무게 한도 W인 가방에 물건들(무게, 가치)을 넣어 가치 합을 최대로.
 * 각 물건은 0개 또는 1개만 (그래서 "0/1" 배낭).
 *
 * 상태:  dp[i][w] = 앞의 i개 물건까지 고려, 무게 한도 w일 때 최대 가치
 * 점화식: dp[i][w] = max( 안 넣음 dp[i-1][w],
 *                        넣음   dp[i-1][w-무게] + 가치 )   (넣을 수 있을 때)
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex16_Knapsack.java
 *   java -cp out Ex16_Knapsack
 */
public class Ex16_Knapsack {

    public static void main(String[] args) {
        int[] weight = {6, 4, 3, 5};    // 각 물건의 무게
        int[] value  = {13, 8, 6, 12};  // 각 물건의 가치
        int n = weight.length;
        int W = 10;                     // 가방 무게 한도

        // dp[i][w] : 1..i번째 물건까지, 한도 w일 때 최대 가치 (i는 1부터)
        int[][] dp = new int[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            int wt = weight[i - 1], val = value[i - 1];   // i번째 물건 (배열은 0-based)
            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i - 1][w];                  // 1) 안 넣는 경우
                if (w >= wt) {                            // 2) 넣을 수 있으면 비교
                    dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - wt] + val);
                }
            }
        }

        System.out.println("물건 (무게, 가치):");
        for (int i = 0; i < n; i++) System.out.println("  " + (i + 1) + ": (" + weight[i] + ", " + value[i] + ")");
        System.out.println("가방 한도 W = " + W);
        System.out.println("\n→ 담을 수 있는 최대 가치 = " + dp[n][W]);
        System.out.println("  (무게4+가치8, 무게6+가치13 → 무게10, 가치21이 최적)");
    }
}
