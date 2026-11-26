import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 레이스 컨디션(경쟁 상태): 여러 스레드가 같은 변수를 동시에 고치면 숫자가 사라집니다.
 * (문서: 04_동시성_레이스컨디션과_락)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" RaceConditionLab.java
 *        java "-Dfile.encoding=UTF-8" RaceConditionLab.java 8 200000   (스레드 수, 스레드당 증가 횟수)
 *
 * 관찰 포인트
 *  - count++ 는 한 줄이지만 실제로는 "읽기 → 1 더하기 → 쓰기" 3단계라서 중간에 끼어들 수 있다
 *  - 실행할 때마다 결과가 다르다 (재현이 어려운 버그의 전형)
 *  - synchronized / AtomicInteger 는 항상 정확하지만, 비용이 든다 (시간 비교)
 */
public class RaceConditionLab {

    // 1) 잠금 없는 카운터
    //    volatile = "매번 메인 메모리에서 읽고 쓴다(가시성)"는 뜻일 뿐, count++ 를 한 덩어리로 만들어 주지는 않는다.
    //    (volatile 을 빼면 JIT 컴파일러가 반복문을 통째로 최적화해 버려, 회차마다 결과가 들쭉날쭉해 관찰이 어렵다)
    static class UnsafeCounter {
        private volatile int count = 0;
        void increment() { count++; }                 // 읽기 → +1 → 쓰기 (원자적이지 않음)
        int get() { return count; }
    }

    // 2) synchronized: 한 번에 한 스레드만 들어오게 잠금
    static class SyncCounter {
        private int count = 0;
        synchronized void increment() { count++; }
        synchronized int get() { return count; }
    }

    // 3) AtomicInteger: CPU의 원자적 명령(CAS)을 사용
    static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger();
        void increment() { count.incrementAndGet(); }
        int get() { return count.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = args.length > 0 ? parsePositive(args[0], "스레드 수") : 4;
        int perThread = args.length > 1 ? parsePositive(args[1], "스레드당 증가 횟수") : 100_000;
        long expected = (long) threads * perThread;
        System.out.printf("스레드 %d개 × 각 %,d번 증가 → 기대값 %,d%n", threads, perThread, expected);
        System.out.println("CPU 코어 수(availableProcessors) = " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        for (int round = 1; round <= 5; round++) {
            UnsafeCounter unsafe = new UnsafeCounter();
            long ms = run(threads, perThread, unsafe::increment);
            System.out.printf("[volatile만     ] %d회차 결과 %,11d  (잃어버린 증가 %,d)  %dms%n",
                    round, unsafe.get(), expected - unsafe.get(), ms);
        }

        SyncCounter sync = new SyncCounter();
        long syncMs = run(threads, perThread, sync::increment);
        System.out.printf("[synchronized   ]       결과 %,11d  %dms%n", sync.get(), syncMs);

        AtomicCounter atomic = new AtomicCounter();
        long atomicMs = run(threads, perThread, atomic::increment);
        System.out.printf("[AtomicInteger  ]       결과 %,11d  %dms%n", atomic.get(), atomicMs);

        System.out.println();
        System.out.println("→ 잠금이 없으면 실행할 때마다 값이 다르고 기대값보다 작다. 에러도 경고도 없다.");
        System.out.println("→ volatile 을 붙여도 소용없다: volatile 은 '보이게' 할 뿐 '한 번에 하나씩'을 보장하지 않는다.");
        System.out.println("   (스레드 수를 1로 주면 항상 정확하다 = '혼자 테스트했을 땐 괜찮던데요'가 증거가 못 되는 이유)");
        System.out.println("→ 스프링 빈(싱글톤)에 상태 필드를 두면 요청 스레드들이 정확히 이 상황이 된다 → SingletonStateBug.java");
    }

    /** threads 개의 스레드가 각각 perThread 번 action 을 실행하고, 모두 끝날 때까지 기다린 시간(ms)을 돌려준다. */
    private static long run(int threads, int perThread, Runnable action) throws InterruptedException {
        List<Thread> list = new ArrayList<>();
        CountDownLatch ready = new CountDownLatch(threads);   // 모든 스레드가 준비됐는지
        CountDownLatch go = new CountDownLatch(1);            // "출발!" 신호 (동시에 시작해야 충돌이 잘 보임)
        for (int t = 0; t < threads; t++) {
            Thread th = new Thread(() -> {
                ready.countDown();
                try {
                    go.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                for (int i = 0; i < perThread; i++) action.run();
            });
            list.add(th);
            th.start();
        }
        ready.await();
        long start = System.nanoTime();
        go.countDown();
        for (Thread th : list) th.join();            // 모든 스레드가 끝날 때까지 기다림
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static int parsePositive(String s, String name) {
        try {
            int v = Integer.parseInt(s);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            System.err.println(name + "는 1 이상의 정수여야 합니다: " + s);
            System.exit(1);
            return -1; // 도달하지 않음
        }
    }
}
