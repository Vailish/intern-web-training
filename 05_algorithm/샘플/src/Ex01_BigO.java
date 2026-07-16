/**
 * [01단계] 시간복잡도를 눈으로 체감하기 — O(N) vs O(N^2)
 *
 * 같은 입력 크기 N에 대해 두 알고리즘의 실제 실행 시간을 재서 비교합니다.
 * N을 2배로 늘릴 때:
 *   - O(N)   : 시간도 약 2배
 *   - O(N^2) : 시간이 약 4배  ← 이 급격한 증가가 "시간초과"의 정체
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex01_BigO.java
 *   java -cp out Ex01_BigO
 */
public class Ex01_BigO {

    /** O(N) : 배열을 한 번만 훑어 합을 구함 */
    static long sumLinear(int[] arr) {
        long sum = 0;
        for (int x : arr) sum += x;   // N번
        return sum;
    }

    /** O(N^2) : 모든 (i, j) 쌍을 확인 (일부러 이중 반복) */
    static long countPairsQuadratic(int[] arr) {
        long count = 0;
        for (int i = 0; i < arr.length; i++)
            for (int j = 0; j < arr.length; j++)   // N × N번
                if (arr[i] + arr[j] > 0) count++;
        return count;
    }

    public static void main(String[] args) {
        // N을 2배씩 키우며 두 방식의 시간을 비교
        int[] sizes = {10_000, 20_000, 40_000, 80_000};

        System.out.println("        N |      O(N) 시간 |   O(N^2) 시간");
        System.out.println("----------+----------------+---------------");

        for (int n : sizes) {
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) arr[i] = i - n / 2;  // 대충 채움

            long t1 = System.nanoTime();
            sumLinear(arr);
            long linearMs = (System.nanoTime() - t1) / 1_000_000;

            long t2 = System.nanoTime();
            countPairsQuadratic(arr);
            long quadMs = (System.nanoTime() - t2) / 1_000_000;

            System.out.printf("%9d | %11d ms | %10d ms%n", n, linearMs, quadMs);
        }

        System.out.println();
        System.out.println("관찰 포인트:");
        System.out.println(" - N이 2배 될 때 O(N)은 시간도 대략 2배.");
        System.out.println(" - O(N^2)는 시간이 대략 4배씩 폭증 → N=100,000만 돼도 감당 불가.");
        System.out.println(" - 그래서 N이 크면(예: 10만 이상) O(N^2) 설계는 처음부터 버려야 합니다.");
    }
}
