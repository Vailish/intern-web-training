import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 스레드 풀: 톰캣이 요청을 처리하는 방식을 흉내 냅니다.
 * (문서: 03_운영체제_프로세스와_스레드)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" ThreadPoolLab.java          (스레드 2개, 요청 6개)
 *        java "-Dfile.encoding=UTF-8" ThreadPoolLab.java 6        (스레드 6개로 늘려 비교)
 *
 * 관찰 포인트
 *  - 요청 하나는 1초 걸리는데, 스레드가 2개뿐이면 3번째 요청부터는 "줄을 서서" 기다린다
 *  - 응답 시간 = 대기 시간 + 처리 시간.  사용자는 대기 시간까지 포함해서 "느리다"고 느낀다
 *  - 스레드 이름(pool-1-thread-1, 2)이 재사용되는 것 = 스레드를 매번 만들지 않고 빌려 쓰고 돌려준다
 */
public class ThreadPoolLab {

    public static void main(String[] args) throws Exception {
        int poolSize = 2;
        if (args.length > 0) {
            try {
                poolSize = Integer.parseInt(args[0]);
                if (poolSize < 1 || poolSize > 50) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.err.println("스레드 수는 1~50 사이 정수로 주세요: " + args[0]);
                System.exit(1);
            }
        }
        int requests = 6;
        System.out.printf("스레드 풀 크기 %d, 요청 %d개 (요청 하나 처리에 1초)%n%n", poolSize, requests);

        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        long t0 = System.nanoTime();
        List<Future<String>> results = new ArrayList<>();

        for (int i = 1; i <= requests; i++) {
            int id = i;
            long arrived = System.nanoTime();                 // 요청이 도착한 시각 (모두 거의 동시에 도착)
            results.add(pool.submit(() -> {
                long startedMs = ms(System.nanoTime() - arrived); // 스레드를 배정받기까지 기다린 시간
                Thread.sleep(1000);                              // DB 조회 등 1초 걸리는 처리
                long totalMs = ms(System.nanoTime() - arrived);
                return String.format("요청%d  처리 스레드=%-16s 대기 %4dms + 처리 1000ms = 응답 %4dms",
                        id, Thread.currentThread().getName(), startedMs, totalMs);
            }));
        }
        for (Future<String> f : results) System.out.println(f.get());

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.printf("%n전체 소요 %dms%n", ms(System.nanoTime() - t0));
        System.out.println("→ 스레드가 부족하면 처리 속도는 그대로인데 '대기'가 늘어 응답이 느려진다.");
        System.out.println("→ 그렇다고 스레드를 무한히 늘리면 메모리(스레드마다 스택)와 문맥 교환 비용이 커진다.");
        System.out.println("   스프링부트 내장 톰캣 기본값: server.tomcat.threads.max=200");
    }

    private static long ms(long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }
}
