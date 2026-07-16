import java.util.*;

/**
 * [04단계] 백트래킹 — N-Queens
 *
 * N×N 판에 퀸 N개를 서로 공격하지 않게 놓는 경우의 수.
 * 퀸은 같은 열/대각선을 공격 (행은 한 행에 하나만 놓으므로 자동 배제).
 *
 * 백트래킹의 핵심: 놓기 전에 isSafe로 검사 → 안전할 때만 다음 행으로.
 * 안전하지 않은 가지는 즉시 포기(가지치기)하여 탐색량을 줄임.
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex10_Backtracking_NQueens.java
 *   java -cp out Ex10_Backtracking_NQueens
 */
public class Ex10_Backtracking_NQueens {

    static int n;
    static int[] col;    // col[r] = r번째 행에서 퀸이 놓인 열

    /** row행 c열에 놓아도 이전 행들의 퀸과 충돌하지 않는가? */
    static boolean isSafe(int row, int c) {
        for (int r = 0; r < row; r++) {
            if (col[r] == c) return false;                         // 같은 열
            if (Math.abs(col[r] - c) == Math.abs(r - row)) return false; // 대각선
        }
        return true;
    }

    static int solve(int row) {
        if (row == n) return 1;              // N개를 모두 놓음 → 성공 1가지
        int count = 0;
        for (int c = 0; c < n; c++) {
            if (isSafe(row, c)) {            // 안전할 때만 진행 (가지치기)
                col[row] = c;
                count += solve(row + 1);
            }
        }
        return count;
    }

    public static void main(String[] args) {
        System.out.println("N-Queens 해의 개수:");
        for (n = 1; n <= 10; n++) {
            col = new int[n];
            System.out.printf("  %2d x %2d 판 → %d 가지%n", n, n, solve(0));
        }
        System.out.println("\n(N=8일 때 92가지가 유명한 정답. 값이 맞는지 확인해 보세요.)");
    }
}
