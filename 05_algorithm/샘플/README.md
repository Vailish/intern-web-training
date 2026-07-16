# 알고리즘 샘플 코드

[05_algorithm](../README.md) 각 문서에 나오는 예제의 완성 실행 코드입니다.
모두 **JDK 17 하나로** 외부 라이브러리 없이 바로 실행됩니다. 파일 맨 위 주석에 무엇을 보는 예제인지 적혀 있습니다.

## 문서별 대응표

| 문서 | 파일 | 내용 |
|---|---|---|
| [01 시간복잡도/입출력](../01_기초_시간복잡도와_입출력.md) | `Ex01_BigO.java` | O(N) vs O(N²) 실제 시간 비교 |
| | `Ex02_FastIO.java` | 빠른 입출력 템플릿, String 덧셈의 위험 |
| [02 자료구조](../02_자료구조_활용.md) | `Ex03_ListMapSet.java` | 빈도 세기, 중복 제거, Two Sum |
| | `Ex04_StackQueueDeque.java` | 괄호 검사(스택), FIFO(큐), 덱 |
| | `Ex05_PriorityQueue.java` | 최소/최대 힙, K번째 큰 값 |
| [03 정렬/탐색](../03_정렬과_탐색.md) | `Ex06_SortComparator.java` | 다중 기준 정렬, 뺄셈 비교 함정 |
| | `Ex07_BinarySearch.java` | 이진탐색 + lower/upper bound |
| | `Ex08_ParametricSearch.java` | 랜선 자르기 — 답을 이진탐색 |
| [04 완전탐색/그리디](../04_완전탐색과_그리디.md) | `Ex09_Permutation.java` | 순열·조합 재귀 생성 |
| | `Ex10_Backtracking_NQueens.java` | 백트래킹 + 가지치기 |
| | `Ex11_Greedy.java` | 거스름돈, 회의실, 그리디가 틀리는 예 |
| [05 DFS/BFS](../05_그래프탐색_DFS_BFS.md) | `Ex12_GraphDFS.java` | DFS, 연결 요소 개수 |
| | `Ex13_GraphBFS.java` | BFS, 최단 거리 배열 |
| | `Ex14_MazeBFS.java` | 격자 미로 최단 거리 (정석) |
| [06 DP](../06_동적계획법_DP.md) | `Ex15_Fibonacci.java` | 재귀 vs 메모 vs 타뷸 시간 비교 |
| | `Ex16_Knapsack.java` | 0/1 배낭 (2차원 DP) |
| | `Ex17_LIS.java` | 최장 증가 부분 수열 |
| [07 문자열](../07_문자열_처리.md) | `Ex18_StringBasics.java` | char↔int, 메서드, `==` 함정, 뒤집기 |
| | `Ex19_CharFrequency_Anagram.java` | int[26] 빈도, 아나그램 |
| | `Ex20_Palindrome.java` | 회문(투 포인터), 기호 무시 |
| | `Ex21_StringParsing.java` | split 함정, StringTokenizer, 정규식 |
| | `Ex22_SlidingWindow.java` | 중복 없는 최장 부분 문자열 |

## 실행 방법 (PowerShell, 이 폴더에서)

```powershell
javac -encoding UTF-8 -d out src/Ex01_BigO.java
java -cp out Ex01_BigO
```

- 다른 예제는 파일명/클래스명만 바꿔 같은 방식으로 실행합니다.
- macOS/리눅스에서 클래스가 여러 개면 클래스패스 구분자가 `;`가 아니라 `:`입니다 (단일 클래스는 무관).
- `-encoding UTF-8`은 소스의 한글 주석/출력이 깨지지 않도록 하는 옵션입니다. **꼭 붙이세요.**

### 한글이 깨져 보인다면 (Windows 콘솔)

윈도우 터미널 기본 코드페이지 때문에 실행 결과의 한글이 깨질 수 있습니다. 이럴 땐 실행 전에:

```powershell
chcp 65001            # 콘솔을 UTF-8로 전환
java -cp out Ex01_BigO
```

또는 IntelliJ 등 IDE에서 실행하면 대개 정상 출력됩니다. (코드 자체 문제는 아닙니다.)

## 전체 한 번에 컴파일

```powershell
javac -encoding UTF-8 -d out src/*.java
```
