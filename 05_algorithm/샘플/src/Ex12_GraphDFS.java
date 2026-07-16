import java.util.*;

/**
 * [05단계] DFS (깊이 우선 탐색) — 인접 리스트
 *
 *   (1) DFS 순회 순서 출력
 *   (2) "연결 요소(그룹)의 개수" 세기 — DFS의 대표 활용
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex12_GraphDFS.java
 *   java -cp out Ex12_GraphDFS
 */
public class Ex12_GraphDFS {

    static List<List<Integer>> graph;
    static boolean[] visited;

    static void dfs(int cur) {
        visited[cur] = true;                 // 방문 표시 먼저
        System.out.print(cur + " ");
        for (int next : graph.get(cur)) {
            if (!visited[next]) dfs(next);   // 안 가본 곳으로 깊게
        }
    }

    static void addEdge(int u, int v) {
        graph.get(u).add(v);
        graph.get(v).add(u);                 // 무방향
    }

    public static void main(String[] args) {
        int n = 7;                            // 정점 1~7
        graph = new ArrayList<>();
        for (int i = 0; i <= n; i++) graph.add(new ArrayList<>());

        // 그룹 A: 1-2-3, 그룹 B: 4-5, 그룹 C: 6-7  (3개의 연결 요소)
        addEdge(1, 2); addEdge(2, 3);
        addEdge(4, 5);
        addEdge(6, 7);

        System.out.println("[DFS 순회] 1에서 시작:");
        System.out.print("  ");
        visited = new boolean[n + 1];
        dfs(1);
        System.out.println("  ← 1과 연결된 정점만 방문됨");

        // ---- 연결 요소(그룹) 개수 세기 ----
        visited = new boolean[n + 1];
        int components = 0;
        for (int i = 1; i <= n; i++) {
            if (!visited[i]) {                // 아직 어느 그룹에도 속하지 않은 정점
                components++;                 // 새 그룹 발견
                dfs(i);                       // 그 그룹 전체를 방문 처리
            }
        }
        System.out.println("\n\n[연결 요소 개수] = " + components + " 개 (정답: 3)");
    }
}
