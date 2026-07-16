import java.util.*;

/**
 * [06단계] LIS — 최장 증가 부분 수열 (Longest Increasing Subsequence)
 *
 * 수열에서 순서를 유지하며 골랐을 때 "계속 커지는" 가장 긴 부분 수열의 길이.
 *
 * 상태:  dp[i] = i번째 원소로 "끝나는" 증가 수열의 최대 길이
 * 점화식: dp[i] = max(dp[j] + 1)   (j<i 이고 arr[j] < arr[i] 인 모든 j)
 * 답:    dp 배열의 최댓값
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex17_LIS.java
 *   java -cp out Ex17_LIS
 */
public class Ex17_LIS {

    public static void main(String[] args) {
        int[] arr = {10, 20, 10, 30, 20, 50};
        int n = arr.length;

        int[] dp = new int[n];
        Arrays.fill(dp, 1);          // 자기 자신만으로도 길이 1

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (arr[j] < arr[i]) {                    // 앞 원소가 더 작으면 이어붙일 수 있음
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
        }

        int answer = 0;
        for (int d : dp) answer = Math.max(answer, d);   // dp의 최댓값이 정답

        System.out.println("수열: " + Arrays.toString(arr));
        System.out.println("dp  : " + Arrays.toString(dp) + "  (각 위치에서 끝나는 LIS 길이)");
        System.out.println("\n→ 최장 증가 부분 수열의 길이 = " + answer);
        System.out.println("  (예: 10 → 20 → 30 → 50, 길이 4)");
    }
}
