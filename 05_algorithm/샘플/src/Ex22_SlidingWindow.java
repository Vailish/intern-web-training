import java.util.*;

/**
 * [07단계] 슬라이딩 윈도우 — 중복 없는 가장 긴 부분 문자열
 *
 * "연속된 구간(부분 문자열)"을 다루는 문제의 핵심 기법.
 * 두 포인터(left, right)로 구간을 유지하며, 조건이 깨지면 left를 당겨 구간을 줄인다.
 * 전체를 한 번씩만 훑으므로 O(N).
 *
 * 예: "abcabcbb" → 가장 긴 "중복 없는" 부분 문자열은 "abc" (길이 3)
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex22_SlidingWindow.java
 *   java -cp out Ex22_SlidingWindow
 */
public class Ex22_SlidingWindow {

    static int longestUnique(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();   // 문자 → 마지막 등장 위치
        int left = 0, best = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            // 현재 문자가 창 안에서 이미 나왔다면, left를 그 다음 칸으로 점프
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
                left = lastSeen.get(c) + 1;
            }
            lastSeen.put(c, right);
            best = Math.max(best, right - left + 1);          // 현재 창 길이로 갱신
        }
        return best;
    }

    public static void main(String[] args) {
        String[] tests = {"abcabcbb", "bbbbb", "pwwkew", "dvdf", "abba"};
        System.out.println("[중복 없는 가장 긴 부분 문자열 길이]");
        for (String s : tests) {
            System.out.printf("  %-10s → %d%n", "\"" + s + "\"", longestUnique(s));
        }
        System.out.println("\n  abcabcbb→3(abc), bbbbb→1(b), pwwkew→3(wke), dvdf→3(vdf), abba→2(ab)");
    }
}
