import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 스프링 없이 재현하는 "싱글톤 빈에 상태를 두면 생기는 사고".
 * (문서: 04_동시성_레이스컨디션과_락)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" SingletonStateBug.java
 *
 * 상황
 *  - 스프링의 @Service, @Controller 는 기본이 싱글톤: 객체 1개를 모든 요청 스레드가 같이 씁니다.
 *  - 톰캣은 요청마다 스레드 풀에서 스레드를 하나씩 꺼내 같은 객체의 메서드를 동시에 호출합니다.
 *  - 그 객체가 "필드"에 요청별 값을 저장하면, 다른 사용자의 값으로 덮어써질 수 있습니다.
 *
 * 관찰 포인트
 *  - BadGreetingService: 필드 currentUser 에 이름을 저장 → 남의 이름으로 인사하는 응답이 나온다
 *  - GoodGreetingService: 지역 변수만 사용 → 항상 정확
 */
public class SingletonStateBug {

    /** ❌ 요청별 데이터를 필드에 저장하는 서비스 (스프링에서 @Service 라고 상상하세요) */
    static class BadGreetingService {
        private String currentUser;                 // 모든 스레드가 공유하는 필드!

        String greet(String user) {
            this.currentUser = user;                // 1) 내 이름을 필드에 저장
            simulateWork();                         // 2) DB 조회 등 시간이 걸리는 작업 (그 사이 다른 스레드가 덮어씀)
            return "안녕하세요, " + currentUser + "님"; // 3) 필드를 다시 읽음 → 남의 이름일 수 있음
        }
    }

    /** ✅ 요청별 데이터는 지역 변수(각 스레드의 스택)에만 둔다 */
    static class GoodGreetingService {
        String greet(String user) {
            String name = user;                     // 지역 변수: 스레드마다 따로 존재
            simulateWork();
            return "안녕하세요, " + name + "님";
        }
    }

    interface Greeter { String greet(String user); }

    public static void main(String[] args) throws InterruptedException {
        BadGreetingService bad = new BadGreetingService();    // 싱글톤처럼 1개만 만듦
        GoodGreetingService good = new GoodGreetingService();

        System.out.println("=== ❌ 필드에 상태를 저장하는 서비스 ===");
        int wrongBad = simulateRequests(bad::greet);
        System.out.println("=== ✅ 지역 변수만 쓰는 서비스 ===");
        int wrongGood = simulateRequests(good::greet);

        System.out.println();
        System.out.println("잘못된 응답 수: 필드 사용 " + wrongBad + "건 / 지역 변수 " + wrongGood + "건 (요청 20건 중)");
        System.out.println("→ 개인정보가 담긴 화면이라면 '다른 사람 정보 노출' 사고입니다.");
        System.out.println("→ 로컬에서 혼자 테스트하면 절대 안 보이고, 사용자가 몰리는 운영에서만 터집니다.");
    }

    /** 톰캣처럼 스레드 풀(10개)로 20명의 요청을 동시에 처리하고, 남의 이름이 섞인 응답 수를 센다. */
    private static int simulateRequests(Greeter service) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);   // 톰캣 요청 스레드 풀 흉내
        List<String> wrong = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startSignal = new CountDownLatch(1);         // 모두 동시에 출발시키기 위한 신호

        for (int i = 1; i <= 20; i++) {
            String user = "사용자" + i;
            pool.submit(() -> {
                try {
                    startSignal.await();
                    String response = service.greet(user);
                    if (!response.contains(user + "님")) {
                        wrong.add("[" + Thread.currentThread().getName() + "] " + user + " 의 요청 → 응답: " + response);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        startSignal.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            pool.shutdownNow();
            System.out.println("(시간 초과로 중단)");
        }
        wrong.stream().limit(5).forEach(System.out::println);
        if (wrong.size() > 5) System.out.println("... 외 " + (wrong.size() - 5) + "건");
        if (wrong.isEmpty()) System.out.println("모든 응답이 정확합니다.");
        return wrong.size();
    }

    private static void simulateWork() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
