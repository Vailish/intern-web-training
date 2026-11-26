import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * 데드락(교착 상태): 두 스레드가 서로 상대가 가진 잠금을 기다리며 영원히 멈춥니다.
 * (문서: 04_동시성_레이스컨디션과_락)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" DeadlockLab.java          (데드락 발생 → 자동 감지 후 종료)
 *        java "-Dfile.encoding=UTF-8" DeadlockLab.java fixed    (잠금 순서를 통일한 해결 버전)
 *
 * 관찰 포인트
 *  - 데드락은 에러도 예외도 없다. 그냥 "멈춘다" (CPU 사용률도 0%에 가까움)
 *  - 이 프로그램은 JVM 기능(ThreadMXBean)으로 데드락을 찾아 보여 주고 스스로 종료한다
 *  - 실무에서 멈춘 자바 프로세스는 jstack <PID> (또는 jcmd <PID> Thread.print) 로 같은 정보를 본다
 *    PID 확인: jps -l
 */
public class DeadlockLab {

    // 예: 계좌 이체.  A→B 이체는 A를 잠그고 B를 잠금, B→A 이체는 B를 잠그고 A를 잠금
    private static final Object ACCOUNT_A = new Object();
    private static final Object ACCOUNT_B = new Object();

    public static void main(String[] args) throws InterruptedException {
        boolean fixed = args.length > 0 && args[0].equals("fixed");
        System.out.println(fixed ? "=== 해결 버전: 항상 A → B 순서로 잠금 ===" : "=== 데드락 버전: 서로 반대 순서로 잠금 ===");

        Thread t1 = new Thread(() -> transfer("이체1(A→B)", ACCOUNT_A, ACCOUNT_B), "transfer-A-to-B");
        Thread t2 = fixed
                ? new Thread(() -> transfer("이체2(B→A)", ACCOUNT_A, ACCOUNT_B), "transfer-B-to-A")  // 순서 통일
                : new Thread(() -> transfer("이체2(B→A)", ACCOUNT_B, ACCOUNT_A), "transfer-B-to-A"); // 반대 순서
        t1.setDaemon(true);   // 데드락에 걸려도 main 이 끝나면 프로그램이 종료되도록
        t2.setDaemon(true);
        t1.start();
        t2.start();

        // 최대 3초 동안 0.5초마다 데드락 여부를 확인
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        for (int i = 0; i < 6; i++) {
            Thread.sleep(500);
            if (!t1.isAlive() && !t2.isAlive()) {
                System.out.println("→ 두 이체 모두 정상 완료. 데드락 없음.");
                return;
            }
            long[] ids = mx.findDeadlockedThreads();
            if (ids != null) {
                System.out.println();
                System.out.println("🔒 데드락 감지! (jstack 으로 보면 'Found one Java-level deadlock' 으로 나오는 상황)");
                for (ThreadInfo info : mx.getThreadInfo(ids)) {
                    System.out.printf("  스레드 '%s' 상태=%s, 기다리는 잠금=%s, 그 잠금을 가진 스레드='%s'%n",
                            info.getThreadName(), info.getThreadState(), info.getLockName(), info.getLockOwnerName());
                }
                System.out.println("→ 해결: 모든 코드가 잠금을 '같은 순서'로 잡게 한다 (예: 계좌번호가 작은 쪽 먼저).");
                System.out.println("   다시 실행:  java \"-Dfile.encoding=UTF-8\" DeadlockLab.java fixed");
                return;
            }
        }
        System.out.println("3초 안에 데드락이 감지되지 않았습니다(타이밍에 따라 드물게 그럴 수 있음). 다시 실행해 보세요.");
    }

    private static void transfer(String name, Object first, Object second) {
        synchronized (first) {
            System.out.println(name + ": 첫 번째 계좌 잠금 획득, 두 번째 계좌를 기다리는 중...");
            sleep(100);                              // 상대 스레드가 반대쪽 잠금을 잡을 시간을 줌
            synchronized (second) {
                System.out.println(name + ": 두 계좌 모두 잠금 획득 → 이체 완료");
            }
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
