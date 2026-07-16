import java.util.*;

/**
 * [02단계] 스택 / 큐 / 덱 — ArrayDeque 하나로 다 쓰기
 *
 *   (1) 스택(LIFO): 올바른 괄호 검사
 *   (2) 큐(FIFO): 순서대로 처리
 *   (3) 덱(Deque): 양쪽 끝에서 넣고 빼기
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex04_StackQueueDeque.java
 *   java -cp out Ex04_StackQueueDeque
 */
public class Ex04_StackQueueDeque {

    /** 스택으로 괄호 짝 검사: 여는 건 push, 닫는 건 pop */
    static boolean isValidParentheses(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else {                       // ')'
                if (stack.isEmpty()) return false;  // 닫는데 열린 게 없음
                stack.pop();
            }
        }
        return stack.isEmpty();            // 안 닫힌 게 남으면 실패
    }

    public static void main(String[] args) {
        // ---- (1) 스택: 괄호 검사 ----
        String[] tests = {"(())", "(()", ")(", "()()"};
        System.out.println("[스택 - 괄호 검사]");
        for (String t : tests) {
            System.out.printf("  %-6s → %s%n", t, isValidParentheses(t) ? "올바름" : "잘못됨");
        }

        // ---- (2) 큐: 먼저 온 순서대로 처리 ----
        System.out.println("\n[큐 - FIFO 처리 순서]");
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("1번 손님");
        queue.offer("2번 손님");
        queue.offer("3번 손님");
        while (!queue.isEmpty()) {
            System.out.println("  처리: " + queue.poll());   // 들어온 순서대로
        }

        // ---- (3) 덱: 양쪽 끝 조작 ----
        System.out.println("\n[덱 - 양방향]");
        Deque<Integer> dq = new ArrayDeque<>();
        dq.offerLast(2);    // 뒤에
        dq.offerFirst(1);   // 앞에
        dq.offerLast(3);    // 뒤에
        System.out.println("  현재: " + dq);              // [1, 2, 3]
        System.out.println("  앞에서 꺼냄: " + dq.pollFirst());  // 1
        System.out.println("  뒤에서 꺼냄: " + dq.pollLast());   // 3
        System.out.println("  남은 것: " + dq);           // [2]
    }
}
