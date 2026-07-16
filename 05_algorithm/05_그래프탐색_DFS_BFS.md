# 05. 그래프 탐색: DFS·BFS — 코테 최대 빈출

> **이 문서에서 배우는 것**
> - **그래프**를 자바로 표현하는 법 (인접 리스트) 과 **격자(2차원 배열)** 로 보는 관점
> - **DFS(깊이 우선)**: 재귀로 끝까지 파고들기 — "연결 요소", "경로 존재"
> - **BFS(너비 우선)**: 큐로 가까운 곳부터 — **"최단 거리/최소 횟수"의 정답**
> - **방문 체크**의 중요성과, 그것을 빼먹으면 생기는 무한 루프

DFS/BFS는 코딩테스트에서 **가장 자주 나오는 유형**입니다. 이거 하나만 확실히 잡아도 합격률이 크게 오릅니다.
[02번 자료구조](./02_자료구조_활용.md)의 스택/큐가 여기서 실제로 쓰입니다.

---

## 1. 그래프란? 어떻게 표현하나

**그래프 = 정점(node) + 간선(edge)**. "무엇이 무엇과 연결되어 있다"를 나타냅니다.
친구 관계, 도로망, 미로, 네트워크 — 전부 그래프입니다.

### 표현 ① 인접 리스트 (권장)

각 정점마다 "연결된 정점들의 목록"을 저장. 메모리 효율적이고 코테의 표준.

```java
int n = 5;   // 정점 1~5
List<List<Integer>> graph = new ArrayList<>();
for (int i = 0; i <= n; i++) graph.add(new ArrayList<>());

// 1-2, 1-3, 2-4 간선 추가 (양방향 = 무방향 그래프)
void addEdge(int u, int v) {
    graph.get(u).add(v);
    graph.get(v).add(u);   // 방향 그래프면 이 줄 생략
}
```

### 표현 ② 격자 (2차원 배열)

미로·지도 문제는 그래프를 따로 안 만들고 **2차원 배열 자체를 그래프로** 봅니다.
각 칸이 정점, 상하좌우 인접 칸이 간선입니다. **코테 그래프 문제의 절반이 이 형태입니다.**

```java
int[][] grid = {...};
// 상하좌우 이동 (이 4방향 배열은 관용구처럼 외워두세요)
int[] dx = {-1, 1, 0, 0};
int[] dy = {0, 0, -1, 1};
```

---

## 2. DFS — 깊이 우선: 한 방향으로 끝까지

한 갈래를 **끝까지 파고든 뒤**, 막히면 되돌아와 다음 갈래로. 재귀로 짜는 게 자연스럽습니다.

```java
static boolean[] visited;

static void dfs(int cur, List<List<Integer>> graph) {
    visited[cur] = true;                    // ★ 방문 표시 먼저!
    System.out.print(cur + " ");
    for (int next : graph.get(cur)) {
        if (!visited[next]) {               // 안 가본 곳만
            dfs(next, graph);
        }
    }
}
```

**쓰임새**: "연결된 그룹(요소)이 몇 개인가", "A에서 B로 갈 수 있나", 백트래킹 기반 탐색.

> ⚠️ **재귀 깊이 주의**: 정점이 수십만 개면 재귀 DFS가 **StackOverflowError**를 냅니다.
> 그럴 땐 명시적 스택(`ArrayDeque`)으로 바꾸거나 BFS를 쓰세요.

---

## 3. BFS — 너비 우선: 가까운 곳부터 물결처럼

시작점에서 **한 칸 거리 → 두 칸 거리 → ...** 순으로 퍼져나갑니다. **큐**로 구현합니다.

```java
static void bfs(int start, List<List<Integer>> graph) {
    Queue<Integer> queue = new ArrayDeque<>();
    boolean[] visited = new boolean[graph.size()];

    visited[start] = true;      // ★ 큐에 "넣을 때" 방문 표시 (중요!)
    queue.offer(start);

    while (!queue.isEmpty()) {
        int cur = queue.poll();
        System.out.print(cur + " ");
        for (int next : graph.get(cur)) {
            if (!visited[next]) {
                visited[next] = true;   // 넣는 순간 표시 → 중복 삽입 방지
                queue.offer(next);
            }
        }
    }
}
```

> 📌 **BFS의 방문 표시는 큐에 넣을 때 합니다** (꺼낼 때가 아니라!).
> 꺼낼 때 표시하면 같은 정점이 큐에 여러 번 들어가 중복 처리 → 느려지거나 틀립니다.

### 🎯 BFS = 최단 거리

가중치가 없는(=모든 간선 비용이 1인) 그래프에서, **BFS로 처음 도달한 순간이 곧 최단 거리**입니다.
"최소 이동 횟수", "미로의 최단 경로", "몇 단계 만에" 라는 말이 보이면 **거의 무조건 BFS**입니다.

거리는 별도 배열에 기록합니다.

```java
int[] dist = new int[n];
Arrays.fill(dist, -1);          // -1 = 아직 방문 안 함 (방문 배열 겸용)
dist[start] = 0;
queue.offer(start);
while (!queue.isEmpty()) {
    int cur = queue.poll();
    for (int next : graph.get(cur)) {
        if (dist[next] == -1) {           // 안 가본 곳
            dist[next] = dist[cur] + 1;   // 한 칸 더
            queue.offer(next);
        }
    }
}
```

---

## 4. DFS vs BFS — 언제 무엇을?

| 기준 | DFS | BFS |
|---|---|---|
| 구현 | 재귀(간결) / 스택 | 큐 |
| 탐색 순서 | 한 갈래 끝까지 | 가까운 것부터 |
| **최단 거리** | ❌ 보장 안 됨 | ✅ **보장됨** |
| 잘 맞는 문제 | 연결 요소, 경로 존재, 조합 탐색 | 최소 이동/단계, 미로 최단경로 |
| 위험 | 깊으면 StackOverflow | 넓으면 메모리(큐)가 큼 |

> 💡 **결정 규칙**: 문제가 **"최단/최소 횟수"** 를 물으면 → **BFS**.
> **"연결 여부/그룹 개수/모든 경로"** 를 물으면 → DFS(또는 BFS 아무거나).

### 대표 예: 미로 최단 거리 (격자 BFS)

> (0,0)에서 출발해 (N-1,M-1)까지 가는 최소 칸 수. 1은 길, 0은 벽.

이것이 **격자 BFS의 정석 템플릿**입니다. 반드시 손에 익히세요.
전체 실행 예제는 [샘플/src/Ex14_MazeBFS.java](./샘플/src/Ex14_MazeBFS.java)에 있고, 상하좌우 이동 + 거리 누적의 완성형입니다.

---

## 5. 가장 흔한 실수 3가지

1. **방문 체크 누락** → 같은 곳을 무한히 오가며 **무한 루프** 또는 시간초과. `visited`는 선택이 아니라 필수.
2. **BFS에서 꺼낼 때 방문 표시** → 큐에 중복 삽입. 반드시 **넣을 때** 표시.
3. **격자 범위 벗어남** → 이동 후 `0 <= nx < N && 0 <= ny < M` 검사를 빼먹으면 `ArrayIndexOutOfBounds`.

```java
int nx = x + dx[d], ny = y + dy[d];
if (nx < 0 || nx >= N || ny < 0 || ny >= M) continue;   // 범위 밖이면 무시
if (visited[nx][ny] || grid[nx][ny] == 0) continue;      // 방문했거나 벽이면 무시
```

---

## 정리

- 그래프는 **인접 리스트**(일반) 또는 **2차원 배열**(격자)로 표현한다.
- **DFS**는 재귀로 깊게 — 연결 요소·경로 존재.
- **BFS**는 큐로 넓게 — **가중치 없는 최단 거리의 정답.**
- 방문 체크는 필수, BFS는 **넣을 때** 표시, 격자는 **범위 검사** 필수.

## 샘플 코드

| 파일 | 내용 |
|---|---|
| [Ex12_GraphDFS.java](./샘플/src/Ex12_GraphDFS.java) | 인접 리스트 DFS, 연결 요소 개수 세기 |
| [Ex13_GraphBFS.java](./샘플/src/Ex13_GraphBFS.java) | 인접 리스트 BFS, 최단 거리 배열 |
| [Ex14_MazeBFS.java](./샘플/src/Ex14_MazeBFS.java) | 격자 미로 최단 거리 (정석 템플릿) |

## 연습 문제

> **LeetCode**는 바로 클릭, **프로그래머스**는 [코딩테스트 연습](https://school.programmers.co.kr/learn/challenges)에서 **문제명 검색**.

| 문제 | 플랫폼 | 유형 |
|---|---|---|
| [Number of Islands](https://leetcode.com/problems/number-of-islands/) | LeetCode | 연결 요소(섬 개수) |
| [Flood Fill](https://leetcode.com/problems/flood-fill/) | LeetCode | DFS/BFS 기본 |
| [Rotting Oranges](https://leetcode.com/problems/rotting-oranges/) | LeetCode | 다중 시작점 BFS |
| [Shortest Path in Binary Matrix](https://leetcode.com/problems/shortest-path-in-binary-matrix/) | LeetCode | 격자 최단거리 BFS |
| [Course Schedule](https://leetcode.com/problems/course-schedule/) | LeetCode | 그래프 탐색(사이클) |
| 타겟 넘버 | 프로그래머스 | DFS |
| 네트워크 | 프로그래머스 | 연결 요소 |
| 게임 맵 최단거리 | 프로그래머스 | 격자 BFS |
| 단어 변환 | 프로그래머스 | BFS |

➡️ 다음: [06. 동적계획법(DP)](./06_동적계획법_DP.md)
