import java.util.*;

/**
 * [05단계] BFS (너비 우선 탐색) — 인접 리스트 + 최단 거리
 *
 *   (1) BFS 순회 순서
 *   (2) 시작점에서 각 정점까지의 "최단 거리(간선 수)" 계산
 *       → 가중치 없는 그래프에서 BFS = 최단 거리
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex13_GraphBFS.java
 *   java -cp out Ex13_GraphBFS
 */
public class Ex13_GraphBFS {

    static List<List<Integer>> graph;

    static int[] bfs(int start, int n) {
        int[] dist = new int[n + 1];
        Arrays.fill(dist, -1);        // -1 = 미방문 (방문 배열 겸용)
        dist[start] = 0;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(start);

        System.out.print("[BFS 순회] ");
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            System.out.print(cur + " ");
            for (int next : graph.get(cur)) {
                if (dist[next] == -1) {           // 아직 안 간 곳
                    dist[next] = dist[cur] + 1;   // 한 칸 더 멀다
                    queue.offer(next);            // 넣을 때 방문 처리(=dist 기록)
                }
            }
        }
        System.out.println();
        return dist;
    }

    static void addEdge(int u, int v) {
        graph.get(u).add(v);
        graph.get(v).add(u);
    }

    public static void main(String[] args) {
        int n = 6;
        graph = new ArrayList<>();
        for (int i = 0; i <= n; i++) graph.add(new ArrayList<>());
        addEdge(1, 2); addEdge(1, 3);
        addEdge(2, 4); addEdge(3, 4);
        addEdge(4, 5); addEdge(5, 6);

        int[] dist = bfs(1, n);
        System.out.println("\n[1번에서 각 정점까지 최단 거리]");
        for (int i = 1; i <= n; i++) {
            System.out.println("  1 → " + i + " : " + dist[i]);
        }
    }
}
