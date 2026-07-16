import java.util.*;

/**
 * [07단계] 문자열 파싱 — split / StringTokenizer / 정규식, 그리고 함정
 *
 * 카카오 등 국내 코딩테스트는 "문자열을 잘라 파싱하는" 문제가 매우 많습니다.
 *   (1) split 과 그 함정 (정규식 특수문자, 뒤쪽 빈 문자열)
 *   (2) StringTokenizer (빠르고 빈 토큰을 안 만듦)
 *   (3) 정규식 replaceAll 로 원하는 문자만 남기기
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex21_StringParsing.java
 *   java -cp out Ex21_StringParsing
 */
public class Ex21_StringParsing {

    public static void main(String[] args) {
        // ---- (1) split 기본 ----
        String csv = "kim,lee,park,choi";
        System.out.println("[split 기본] " + Arrays.toString(csv.split(",")));

        // 함정 A: split의 인자는 "정규식"이다. 점(.)은 '아무 문자'라 그대로 쓰면 안 됨!
        String ip = "192.168.0.1";
        System.out.println("\n[split 함정 - 정규식 특수문자]");
        System.out.println("  split(\".\")   → " + Arrays.toString(ip.split(".")) + "   (전부 사라짐! 잘못)");
        System.out.println("  split(\"\\\\.\") → " + Arrays.toString(ip.split("\\.")) + "   (이스케이프해야 정상)");

        // 함정 B: 뒤쪽 빈 문자열은 기본적으로 잘려나감. 살리려면 limit=-1
        String trailing = "a,b,,";
        System.out.println("\n[split 함정 - 뒤쪽 빈 값]");
        System.out.println("  split(\",\")    길이 " + trailing.split(",").length
                + " → " + Arrays.toString(trailing.split(",")));
        System.out.println("  split(\",\",-1) 길이 " + trailing.split(",", -1).length
                + " → " + Arrays.toString(trailing.split(",", -1)));

        // ---- (2) StringTokenizer: 공백 파싱에 빠르고 빈 토큰 안 생김 ----
        String line = "  10   20 30 ";
        StringTokenizer st = new StringTokenizer(line);   // 기본 구분자 = 공백/탭/개행
        System.out.println("\n[StringTokenizer] 토큰 " + st.countTokens() + "개:");
        while (st.hasMoreTokens()) System.out.println("  " + st.nextToken());

        // ---- (3) 정규식으로 원하는 문자만 추출 ----
        String messy = "abc123def456!!";
        System.out.println("\n[정규식 replaceAll]");
        System.out.println("  숫자만 남기기  [^0-9] 제거 → " + messy.replaceAll("[^0-9]", ""));
        System.out.println("  문자만 남기기  [^a-zA-Z] 제거 → " + messy.replaceAll("[^a-zA-Z]", ""));
        // 문자열이 구분자(비숫자)로 "시작"하면 맨 앞에 빈 문자열이 남는다(뒤쪽과 달리 앞쪽은 유지됨).
        System.out.println("  연속 숫자 덩어리 분리 → " + Arrays.toString(messy.split("[^0-9]+"))
                + "   (맨 앞 \"\"는 문자열이 비숫자로 시작해서 생김)");
    }
}
