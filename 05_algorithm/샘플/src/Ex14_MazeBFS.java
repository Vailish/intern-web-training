import java.util.*;

/**
 * [05단계] 격자 미로 최단 거리 — BFS 정석 템플릿 (미로 탐색 유형)
 *
 * 1은 지나갈 수 있는 길, 0은 벽.
 * (0,0)에서 (N-1,M-1)까지 가는 "최소 칸 수"(시작·끝 포함)를 구함.
 *
 * ★ 이 상하좌우 이동 + 거리 누적 패턴은 격자 문제의 표준. 통째로 외워두세요.
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex14_MazeBFS.java
 *   java -cp out Ex14_MazeBFS
 */
public class Ex14_MazeBFS {

    static final int[] dx = {-1, 1, 0, 0};   // 상, 하
    static final int[] dy = {0, 0, -1, 1};   // 좌, 우

    static int bfs(int[][] grid) {
        int n = grid.length, m = grid[0].length;
        int[][] dist = new int[n][m];        // 0 = 미방문
        Queue<int[]> queue = new ArrayDeque<>();

        queue.offer(new int[]{0, 0});
        dist[0][0] = 1;                      // 시작 칸을 1로 (칸 수를 세므로)

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0], y = cur[1];
            if (x == n - 1 && y == m - 1) return dist[x][y];   // 도착

            for (int d = 0; d < 4; d++) {
                int nx = x + dx[d], ny = y + dy[d];
                if (nx < 0 || nx >= n || ny < 0 || ny >= m) continue;  // 범위 밖
                if (grid[nx][ny] == 0 || dist[nx][ny] != 0) continue;  // 벽이거나 방문함
                dist[nx][ny] = dist[x][y] + 1;   // 한 칸 더
                queue.offer(new int[]{nx, ny});
            }
        }
        return -1;   // 도달 불가
    }

    public static void main(String[] args) {
        int[][] grid = {
                {1, 0, 1, 1, 1, 1},
                {1, 0, 1, 0, 1, 0},
                {1, 0, 1, 0, 1, 1},
                {1, 1, 1, 0, 1, 1},
        };
        System.out.println("미로 (1=길, 0=벽):");
        for (int[] row : grid) System.out.println("  " + Arrays.toString(row));

        int answer = bfs(grid);
        System.out.println("\n(0,0) → (" + (grid.length - 1) + "," + (grid[0].length - 1)
                + ") 최단 칸 수 = " + answer + " 칸");
    }
}
