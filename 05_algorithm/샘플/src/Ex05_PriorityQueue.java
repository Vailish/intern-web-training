import java.util.*;

/**
 * [02단계] PriorityQueue (힙) — 항상 최솟값/최댓값을 먼저
 *
 *   (1) 최소 힙 / 최대 힙 기본
 *   (2) K번째로 큰 값을 O(N log K)로 찾기
 *   (3) 객체를 우선순위로 정렬해 꺼내기 (Comparator)
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex05_PriorityQueue.java
 *   java -cp out Ex05_PriorityQueue
 */
public class Ex05_PriorityQueue {

    public static void main(String[] args) {
        int[] nums = {5, 1, 8, 3, 9, 2, 7};

        // ---- (1) 최소 힙 vs 최대 힙 ----
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        for (int x : nums) { minHeap.offer(x); maxHeap.offer(x); }

        System.out.print("[최소 힙에서 꺼낸 순서] ");
        while (!minHeap.isEmpty()) System.out.print(minHeap.poll() + " ");  // 1 2 3 5 7 8 9
        System.out.print("\n[최대 힙에서 꺼낸 순서] ");
        while (!maxHeap.isEmpty()) System.out.print(maxHeap.poll() + " ");  // 9 8 7 5 3 2 1

        // ---- (2) K번째로 큰 값 ----
        int k = 3;
        PriorityQueue<Integer> pq = new PriorityQueue<>();   // 최소 힙, 크기 K 유지
        for (int x : nums) {
            pq.offer(x);
            if (pq.size() > k) pq.poll();   // 가장 작은 것부터 버려 K개만 남김
        }
        System.out.println("\n\n[" + k + "번째로 큰 값] = " + pq.peek());  // 7

        // ---- (3) 객체 우선순위: 마감이 급한 작업부터 ----
        record Task(String name, int deadline) {}
        PriorityQueue<Task> tasks =
                new PriorityQueue<>(Comparator.comparingInt(Task::deadline));  // deadline 작을수록 우선
        tasks.offer(new Task("보고서", 5));
        tasks.offer(new Task("긴급 버그", 1));
        tasks.offer(new Task("회의 준비", 3));

        System.out.println("\n[마감 임박 순 처리]");
        while (!tasks.isEmpty()) {
            Task t = tasks.poll();
            System.out.println("  D-" + t.deadline() + " : " + t.name());
        }
    }
}
