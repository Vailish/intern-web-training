import java.util.*;

/**
 * [07단계] 회문(팰린드롬) 검사 — 투 포인터
 *
 * 회문: 거꾸로 읽어도 같은 문자열. (예: 기러기, level, "racecar")
 * 투 포인터: 양 끝에서 가운데로 좁혀오며 한 쌍씩 비교 → O(N), 추가 메모리 없음.
 *
 * 심화: 영문/숫자만 보고 대소문자·공백·기호는 무시하는 버전도 포함
 *       (실전 코딩테스트에서 자주 요구됨).
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex20_Palindrome.java
 *   java -cp out Ex20_Palindrome
 */
public class Ex20_Palindrome {

    /** 순수 회문 검사 (문자 그대로) */
    static boolean isPalindrome(String s) {
        int lo = 0, hi = s.length() - 1;
        while (lo < hi) {
            if (s.charAt(lo) != s.charAt(hi)) return false;
            lo++;
            hi--;
        }
        return true;
    }

    /** 영문/숫자만 비교, 대소문자·기호·공백 무시 */
    static boolean isPalindromeClean(String s) {
        int lo = 0, hi = s.length() - 1;
        while (lo < hi) {
            // 왼쪽에서 영숫자가 아닌 문자는 건너뜀
            while (lo < hi && !Character.isLetterOrDigit(s.charAt(lo))) lo++;
            // 오른쪽에서도
            while (lo < hi && !Character.isLetterOrDigit(s.charAt(hi))) hi--;
            if (Character.toLowerCase(s.charAt(lo)) != Character.toLowerCase(s.charAt(hi)))
                return false;
            lo++;
            hi--;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println("[순수 회문 검사]");
        for (String s : new String[]{"level", "racecar", "hello", "기러기"}) {
            System.out.printf("  %-8s → %s%n", s, isPalindrome(s) ? "회문 O" : "아니오 X");
        }

        System.out.println("\n[기호·대소문자 무시 회문 검사]");
        for (String s : new String[]{"A man, a plan, a canal: Panama", "Was it a car or a cat I saw?", "race a car"}) {
            System.out.printf("  %-35s → %s%n", "\"" + s + "\"",
                    isPalindromeClean(s) ? "회문 O" : "아니오 X");
        }
    }
}
