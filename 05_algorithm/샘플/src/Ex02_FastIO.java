import java.io.*;
import java.util.*;

/**
 * [01단계] 빠른 입출력 템플릿 + String 덧셈의 위험 체감
 *
 * 두 가지를 보여줍니다.
 *  (1) 코테 표준 입출력 템플릿(BufferedReader + StringTokenizer + StringBuilder)의 형태
 *  (2) 반복문 안 "String +=" (O(N^2)) vs "StringBuilder" (O(N)) 의 실제 시간 차이
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex02_FastIO.java
 *   java -cp out Ex02_FastIO
 *
 * (표준입력을 받는 예제가 아니라, 시간 차이를 보여주는 데모입니다.
 *  실제 문제 풀이용 입력 템플릿은 아래 readExample() 메서드의 형태를 참고하세요.)
 */
public class Ex02_FastIO {

    public static void main(String[] args) {
        // ---- String 덧셈 vs StringBuilder 시간 비교 ----
        int n = 50_000;   // 이 값을 키우면 String += 는 급격히 느려집니다

        long t1 = System.nanoTime();
        String bad = "";
        for (int i = 0; i < n; i++) {
            bad += i;                 // ❌ 매번 전체 문자열을 새로 복사 → O(N^2)
        }
        long badMs = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(i);             // ✅ 뒤에 이어붙이기만 → O(N)
        }
        String good = sb.toString();
        long goodMs = (System.nanoTime() - t2) / 1_000_000;

        System.out.println("N = " + n + " 번 문자열 이어붙이기");
        System.out.println(" String +=      : " + badMs + " ms");
        System.out.println(" StringBuilder  : " + goodMs + " ms");
        System.out.println(" (결과 길이 동일: " + (bad.length() == good.length()) + ")");
        System.out.println();
        System.out.println("→ 반복문 안에서 문자열을 만들 땐 예외 없이 StringBuilder를 씁니다.");
    }

    /**
     * 실제 문제에서 쓰는 입력 읽기 템플릿 예시.
     * "첫 줄에 N, 다음 N줄에 두 정수 a b가 주어지면 a+b를 각각 출력" 형태.
     * (main에서 호출하지는 않습니다. 형태만 익히세요.)
     */
    static void readExample() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();

        int n = Integer.parseInt(br.readLine().trim());
        for (int i = 0; i < n; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int a = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());
            out.append(a + b).append('\n');
        }
        System.out.print(out);   // 모아서 한 번에 출력
    }
}
