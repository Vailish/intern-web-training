import java.util.*;

/**
 * [03단계] 파라메트릭 서치 — "답 자체를 이진탐색"
 *
 * 문제(랜선 자르기 유형 — 파라메트릭 서치의 대표 예):
 *   길이가 제각각인 랜선들을 잘라 "같은 길이 X"짜리를 need개 이상 만들고 싶다.
 *   만들 수 있는 X의 최댓값은?
 *
 * 발상:
 *   - X가 작을수록 많이 만들 수 있고, 클수록 적게 만들어짐 (단조성).
 *   - "X로 need개를 만들 수 있나?"는 O(N)에 판정 가능.
 *   - 그러니 X를 1..최대길이 로 이진탐색하면 O(N log(최대길이)).
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex08_ParametricSearch.java
 *   java -cp out Ex08_ParametricSearch
 */
public class Ex08_ParametricSearch {

    /** 길이 cut으로 잘랐을 때 총 몇 조각이 나오는지 (판정 함수) */
    static long countPieces(int[] lengths, int cut) {
        long cnt = 0;
        for (int len : lengths) cnt += len / cut;
        return cnt;                 // long: 조각 수가 int를 넘을 수 있음
    }

    static int maxCutLength(int[] lengths, int need) {
        int lo = 1, hi = 0;
        for (int len : lengths) hi = Math.max(hi, len);   // 답의 상한

        int answer = 0;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (countPieces(lengths, mid) >= need) {
                answer = mid;       // 이 길이로 충분 → 후보 저장, 더 길게 시도
                lo = mid + 1;
            } else {
                hi = mid - 1;       // 부족 → 더 짧게
            }
        }
        return answer;
    }

    public static void main(String[] args) {
        int[] lengths = {802, 743, 457, 539};   // 랜선 길이들
        int need = 11;                          // 11개 이상 필요

        int ans = maxCutLength(lengths, need);
        System.out.println("랜선: " + Arrays.toString(lengths));
        System.out.println("필요 개수: " + need);
        System.out.println("→ 만들 수 있는 최대 길이 = " + ans);   // 200
        System.out.println("  검증: 길이 " + ans + "로 자르면 "
                + countPieces(lengths, ans) + "조각 (>= " + need + " 만족)");
        System.out.println("  검증: 길이 " + (ans + 1) + "로 자르면 "
                + countPieces(lengths, ans + 1) + "조각 (부족)");
    }
}
