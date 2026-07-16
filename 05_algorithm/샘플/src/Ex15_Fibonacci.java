import java.util.*;

/**
 * [06단계] 피보나치로 보는 DP — 세 방식 시간 비교
 *
 *   (1) 순수 재귀      : O(2^N)  ← 같은 계산을 지수적으로 반복 (느림!)
 *   (2) 메모이제이션    : O(N)    ← 재귀 + 캐시 (Top-Down)
 *   (3) 타뷸레이션      : O(N)    ← 반복문 + 표 (Bottom-Up)
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex15_Fibonacci.java
 *   java -cp out Ex15_Fibonacci
 */
public class Ex15_Fibonacci {

    /** (1) 순수 재귀 — 중복 계산 폭발 */
    static long fibNaive(int n) {
        if (n <= 1) return n;
        return fibNaive(n - 1) + fibNaive(n - 2);
    }

    /** (2) 메모이제이션 — 한 번 구한 값 저장 */
    static long[] memo;
    static long fibMemo(int n) {
        if (n <= 1) return n;
        if (memo[n] != 0) return memo[n];
        return memo[n] = fibMemo(n - 1) + fibMemo(n - 2);
    }

    /** (3) 타뷸레이션 — 작은 값부터 표 채우기 */
    static long fibTab(int n) {
        if (n <= 1) return n;
        long[] dp = new long[n + 1];
        dp[0] = 0; dp[1] = 1;
        for (int i = 2; i <= n; i++) dp[i] = dp[i - 1] + dp[i - 2];
        return dp[n];
    }

    public static void main(String[] args) {
        // 순수 재귀는 40만 넘어도 매우 느려지므로 작게 잡음
        int naiveN = 40;
        long t1 = System.nanoTime();
        long r1 = fibNaive(naiveN);
        long ms1 = (System.nanoTime() - t1) / 1_000_000;
        System.out.println("[순수 재귀]  fib(" + naiveN + ") = " + r1 + "  (" + ms1 + " ms)");

        int bigN = 90;   // long 범위 안에서 가능한 큰 값
        memo = new long[bigN + 1];
        long t2 = System.nanoTime();
        long r2 = fibMemo(bigN);
        long us2 = (System.nanoTime() - t2) / 1_000;
        System.out.println("[메모이제이션] fib(" + bigN + ") = " + r2 + "  (" + us2 + " μs)");

        long t3 = System.nanoTime();
        long r3 = fibTab(bigN);
        long us3 = (System.nanoTime() - t3) / 1_000;
        System.out.println("[타뷸레이션]  fib(" + bigN + ") = " + r3 + "  (" + us3 + " μs)");

        System.out.println("\n→ 순수 재귀는 fib(40)에 이미 수백 ms. fib(90)은 사실상 불가능.");
        System.out.println("  DP(메모/타뷸)는 fib(90)도 순식간. 이것이 '중복 계산 제거'의 힘.");
    }
}
