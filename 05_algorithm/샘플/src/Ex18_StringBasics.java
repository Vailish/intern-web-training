/**
 * [07단계] 문자열 기본기 — char↔int, 주요 메서드, ==의 함정, 뒤집기
 *
 * 코딩테스트 문자열 문제의 90%는 이 기본기에서 갈립니다.
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex18_StringBasics.java
 *   java -cp out Ex18_StringBasics
 */
public class Ex18_StringBasics {

    public static void main(String[] args) {
        // ---- (1) char <-> int (아스키 코드 활용) ----
        char digit = '7';
        int d = digit - '0';                 // 문자 '7' → 숫자 7
        System.out.println("[char↔int]");
        System.out.println("  '7' - '0' = " + d);

        char letter = 'c';
        int idx = letter - 'a';              // 'a'=0, 'b'=1, 'c'=2 ... 알파벳 인덱스
        System.out.println("  'c' - 'a' = " + idx + "  (0~25 인덱스로 int[26] 만들 때 씀)");
        System.out.println("  (char)('a' + 2) = " + (char) ('a' + 2));   // 다시 문자로

        // ---- (2) 자주 쓰는 String 메서드 ----
        String s = "Hello, World";
        System.out.println("\n[String 메서드] s = \"" + s + "\"");
        System.out.println("  length()      = " + s.length());
        System.out.println("  charAt(1)     = " + s.charAt(1));
        System.out.println("  substring(7)  = " + s.substring(7));       // 7번부터 끝까지
        System.out.println("  substring(0,5)= " + s.substring(0, 5));    // [0,5) 반열린구간
        System.out.println("  indexOf(\"o\")  = " + s.indexOf("o"));
        System.out.println("  contains(\"World\") = " + s.contains("World"));
        System.out.println("  toUpperCase() = " + s.toUpperCase());
        System.out.println("  replace(\"l\",\"L\") = " + s.replace("l", "L"));

        // ---- (3) 문자열 비교는 == 이 아니라 .equals() ----
        String a = new String("apple");
        String b = new String("apple");
        System.out.println("\n[비교의 함정]");
        System.out.println("  a == b       = " + (a == b) + "   ← 주소 비교(다른 객체라 false!)");
        System.out.println("  a.equals(b)  = " + a.equals(b) + "   ← 내용 비교(올바름)");

        // ---- (4) 문자열 뒤집기 ----
        String rev = new StringBuilder("algorithm").reverse().toString();
        System.out.println("\n[뒤집기] algorithm → " + rev);

        // ---- (5) char 분류 유틸 ----
        System.out.println("\n[Character 유틸]");
        System.out.println("  isDigit('5')   = " + Character.isDigit('5'));
        System.out.println("  isLetter('A')  = " + Character.isLetter('A'));
        System.out.println("  isLetterOrDigit('!') = " + Character.isLetterOrDigit('!'));
        System.out.println("  toLowerCase('A') = " + Character.toLowerCase('A'));
    }
}
