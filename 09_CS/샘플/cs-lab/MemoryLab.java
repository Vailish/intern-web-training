import java.util.ArrayList;
import java.util.List;

/**
 * JVM 메모리: 스택과 힙이 넘치면 어떤 에러가 나는지 직접 봅니다.
 * (문서: 05_JVM_메모리와_GC)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" MemoryLab.java stack                 (StackOverflowError)
 *        java "-Xmx64m" "-Dfile.encoding=UTF-8" MemoryLab.java heap        (OutOfMemoryError, 힙을 64MB로 제한)
 *        java "-Dfile.encoding=UTF-8" MemoryLab.java string                (== 와 equals, 문자열 풀)
 *        java "-Dfile.encoding=UTF-8" MemoryLab.java                       (stack + string 만 실행)
 *
 * 주의: heap 실험은 반드시 -Xmx64m 처럼 힙을 작게 제한하고 실행하세요.
 *       제한 없이 돌리면 PC 메모리의 1/4 까지 쓰느라 오래 걸리고 PC가 느려질 수 있습니다.
 */
public class MemoryLab {

    private static int depth = 0;

    // 실무에서 흔한 메모리 누수 패턴: static 컬렉션에 계속 넣기만 하고 빼지 않음
    private static final List<byte[]> LEAKY_CACHE = new ArrayList<>();

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "default";
        switch (mode) {
            case "stack" -> stack();
            case "heap" -> heap();
            case "string" -> string();
            case "default" -> { stack(); System.out.println(); string(); }
            default -> {
                System.err.println("사용법: MemoryLab.java [stack|heap|string]");
                System.exit(1);
            }
        }
    }

    /** 1. 메서드를 호출할 때마다 스택에 칸(프레임)이 쌓인다. 끝없는 재귀는 스택을 다 채운다. */
    static void stack() {
        System.out.println("=== 1. 스택 넘치기 (끝나지 않는 재귀) ===");
        try {
            recurse();
        } catch (StackOverflowError e) {
            System.out.printf("StackOverflowError! 약 %,d번 호출 후 스택이 가득 참%n", depth);
            System.out.println("→ 원인 대부분: 종료 조건 없는 재귀, 또는 서로를 계속 부르는 메서드");
            System.out.println("  (스프링 예: 엔티티 양방향 연관관계를 JSON 으로 바꿀 때 A→B→A→B... 무한 반복)");
        }
    }

    private static void recurse() {
        depth++;
        recurse();
    }

    /** 2. new 로 만든 객체는 힙에 쌓인다. 누군가 계속 참조하고 있으면 GC가 치우지 못한다. */
    static void heap() {
        long maxMb = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        System.out.println("=== 2. 힙 넘치기 (static 리스트에 계속 쌓기 = 메모리 누수) ===");
        System.out.println("최대 힙(-Xmx) = " + maxMb + "MB");
        if (maxMb > 512) {
            System.out.println("힙이 너무 큽니다. 다음처럼 제한해서 다시 실행하세요:");
            System.out.println("  java \"-Xmx64m\" \"-Dfile.encoding=UTF-8\" MemoryLab.java heap");
            return;
        }
        int mb = 0;
        try {
            while (true) {
                LEAKY_CACHE.add(new byte[1024 * 1024]);   // 1MB 씩 추가, 절대 지우지 않음
                mb++;
                if (mb % 10 == 0) System.out.println("  " + mb + "MB 쌓음...");
            }
        } catch (OutOfMemoryError e) {
            int stored = LEAKY_CACHE.size();
            LEAKY_CACHE.clear();                          // 참조를 끊어 GC가 치울 수 있게 함 (출력할 여유 확보)
            System.out.println("OutOfMemoryError: " + e.getMessage() + "  (약 " + stored + "MB 에서 실패)");
            System.out.println("→ GC는 '아무도 참조하지 않는' 객체만 치운다. static 리스트가 잡고 있으면 영원히 못 치운다.");
            System.out.println("→ 실무 단골 원인: static Map 캐시에 넣기만 함, 한 번에 수십만 건 조회해 List 로 받기");
        }
    }

    /** 3. == 는 "같은 객체(주소)인가", equals 는 "내용이 같은가" */
    static void string() {
        System.out.println("=== 3. 문자열 == vs equals ===");
        String a = "hello";                 // 문자열 리터럴 → 문자열 풀(String Pool)에서 재사용
        String b = "hello";
        String c = new String("hello");     // new → 힙에 새 객체
        String d = c.intern();              // 풀에 있는 같은 내용의 객체를 돌려받음
        System.out.println("a == b          : " + (a == b) + "   (둘 다 풀의 같은 객체)");
        System.out.println("a == c          : " + (a == c) + "  (c 는 new 로 만든 다른 객체)");
        System.out.println("a.equals(c)     : " + a.equals(c) + "   (내용은 같음)");
        System.out.println("a == c.intern() : " + (a == d) + "   (intern 은 풀의 객체를 돌려줌)");

        String input = new StringBuilder("hel").append("lo").toString();   // 사용자 입력처럼 실행 중에 만들어진 문자열
        System.out.println("실행 중 만든 \"hello\" == \"hello\" : " + (input == a) + "  ← 로그인 비교를 == 로 하면 이렇게 실패");
        System.out.println("→ 문자열·Integer·객체 비교는 항상 equals. == 는 null 체크와 기본형(int, long)에만.");

        // 불변(immutable): concat 은 원본을 바꾸지 않고 새 문자열을 만든다
        String s = "abc";
        s.concat("def");
        System.out.println("s.concat(\"def\") 후 s : " + s + "   (String 은 불변 → 결과를 받아야 함: s = s.concat(...))");
    }
}
