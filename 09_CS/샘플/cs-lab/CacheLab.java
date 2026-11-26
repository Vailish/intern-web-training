/**
 * 메모리 계층과 캐시: 같은 양의 일을 해도 "메모리를 읽는 순서"에 따라 속도가 달라집니다.
 * (문서: 01_컴퓨터의_큰그림_CPU_메모리)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" CacheLab.java
 *
 * 관찰 포인트
 *  - 2차원 배열의 모든 칸을 더하는 똑같은 계산인데, 행 방향(옆으로)이 열 방향(아래로)보다 훨씬 빠르다
 *  - 이유: CPU는 메모리를 한 칸씩이 아니라 "덩어리(캐시 라인, 보통 64바이트)"로 가져와 캐시에 둔다
 *          옆 칸은 이미 캐시에 있지만, 아래 칸은 멀리 떨어져 있어 매번 메인 메모리까지 가야 한다
 *  - 측정값은 PC마다, 실행마다 다르다. "몇 배 차이"라는 경향만 보세요.
 */
public class CacheLab {

    static final int N = 4096;   // 4096 x 4096 int = 64MB

    public static void main(String[] args) {
        System.out.printf("%d x %d int 배열 (%dMB) 의 합 구하기%n%n", N, N, (long) N * N * 4 / 1024 / 1024);
        int[][] grid = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                grid[i][j] = 1;

        // JIT(실행 중 기계어 번역)가 끝나도록 몇 번 먼저 돌려 둔다 (워밍업)
        for (int w = 0; w < 2; w++) { rowMajor(grid); colMajor(grid); }

        for (int round = 1; round <= 3; round++) {
            long t1 = System.nanoTime();
            long s1 = rowMajor(grid);
            long rowMs = (System.nanoTime() - t1) / 1_000_000;

            long t2 = System.nanoTime();
            long s2 = colMajor(grid);
            long colMs = (System.nanoTime() - t2) / 1_000_000;

            System.out.printf("%d회차  행 방향 %4dms   열 방향 %4dms   (약 %.1f배)  합계 확인 %b%n",
                    round, rowMs, colMs, colMs / (double) Math.max(rowMs, 1), s1 == s2);
        }
        System.out.println();
        System.out.println("→ 계산량은 똑같다. 차이는 오직 '데이터가 CPU 가까이(캐시) 있느냐'.");
        System.out.println("→ 같은 원리가 웹에도 그대로: DB(디스크·네트워크)보다 메모리 캐시(Redis 등)가 빠른 이유");
    }

    /** grid[0][0], grid[0][1], grid[0][2] ... 메모리에 붙어 있는 순서대로 읽기 */
    static long rowMajor(int[][] g) {
        long sum = 0;
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                sum += g[i][j];
        return sum;
    }

    /** grid[0][0], grid[1][0], grid[2][0] ... 매번 다른 행(멀리 떨어진 메모리)으로 점프 */
    static long colMajor(int[][] g) {
        long sum = 0;
        for (int j = 0; j < N; j++)
            for (int i = 0; i < N; i++)
                sum += g[i][j];
        return sum;
    }
}
