import java.util.*;

/**
 * [07단계] 문자 빈도 세기 + 아나그램 판별
 *
 *   (1) int[26] 으로 소문자 빈도 세기 (해시맵보다 빠르고 간단)
 *   (2) 아나그램: 두 문자열이 같은 문자를 같은 개수로 갖는가? (순서만 다름)
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex19_CharFrequency_Anagram.java
 *   java -cp out Ex19_CharFrequency_Anagram
 */
public class Ex19_CharFrequency_Anagram {

    /** 소문자 문자열의 빈도를 int[26]으로 */
    static int[] frequency(String s) {
        int[] cnt = new int[26];
        for (char c : s.toCharArray()) cnt[c - 'a']++;   // 'a'~'z' → 0~25
        return cnt;
    }

    /** 두 문자열이 아나그램인가 */
    static boolean isAnagram(String a, String b) {
        if (a.length() != b.length()) return false;      // 길이 다르면 즉시 false
        int[] cnt = new int[26];
        for (int i = 0; i < a.length(); i++) {
            cnt[a.charAt(i) - 'a']++;   // a에서 +1
            cnt[b.charAt(i) - 'a']--;   // b에서 -1
        }
        for (int c : cnt) if (c != 0) return false;      // 하나라도 0이 아니면 다름
        return true;
    }

    public static void main(String[] args) {
        String word = "banana";
        int[] freq = frequency(word);
        System.out.println("[빈도] \"" + word + "\"");
        for (int i = 0; i < 26; i++) {
            if (freq[i] > 0) System.out.println("  " + (char) ('a' + i) + " : " + freq[i]);
        }

        System.out.println("\n[아나그램 판별]");
        String[][] tests = {
                {"listen", "silent"},
                {"anagram", "nagaram"},
                {"rat", "car"},
                {"hello", "world"},
        };
        for (String[] t : tests) {
            System.out.printf("  %-9s vs %-9s → %s%n",
                    t[0], t[1], isAnagram(t[0], t[1]) ? "아나그램 O" : "아니오 X");
        }

        // 참고: 정렬로도 판별 가능 (더 짧지만 O(N log N))
        System.out.println("\n[정렬 방식] listen vs silent → "
                + Arrays.equals(sortedChars("listen"), sortedChars("silent")));
    }

    static char[] sortedChars(String s) {
        char[] c = s.toCharArray();
        Arrays.sort(c);
        return c;
    }
}
