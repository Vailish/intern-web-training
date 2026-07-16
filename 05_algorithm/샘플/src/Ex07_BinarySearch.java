import java.util.*;

/**
 * [03단계] 이진탐색 — 직접 구현 + lower/upper bound
 *
 *   (1) 기본 이진탐색: 값의 인덱스 찾기 (없으면 -1)
 *   (2) lowerBound: target "이상"이 처음 나오는 위치
 *   (3) upperBound: target "초과"가 처음 나오는 위치
 *       → (upperBound - lowerBound) = target의 개수
 *
 * 전제: 배열은 반드시 "정렬"되어 있어야 합니다.
 *
 * 실행 방법 (PowerShell, 샘플 폴더에서):
 *   javac -encoding UTF-8 -d out src/Ex07_BinarySearch.java
 *   java -cp out Ex07_BinarySearch
 */
public class Ex07_BinarySearch {

    /** target의 인덱스, 없으면 -1 */
    static int binarySearch(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;      // 오버플로 방지
            if (a[mid] == target) return mid;
            else if (a[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    /** target 이상(>=)이 처음 등장하는 인덱스 (없으면 a.length) */
    static int lowerBound(int[] a, int target) {
        int lo = 0, hi = a.length;             // hi가 length인 점에 주의
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] >= target) hi = mid;    // 조건 만족 → 더 왼쪽 탐색
            else lo = mid + 1;
        }
        return lo;
    }

    /** target 초과(>)가 처음 등장하는 인덱스 */
    static int upperBound(int[] a, int target) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] > target) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }

    public static void main(String[] args) {
        int[] a = {1, 2, 2, 2, 5, 7, 9};       // 이미 정렬됨, 2가 3개

        System.out.println("배열: " + Arrays.toString(a));
        System.out.println("\n[기본 이진탐색]");
        System.out.println("  5의 위치 = " + binarySearch(a, 5));   // 4
        System.out.println("  6의 위치 = " + binarySearch(a, 6));   // -1 (없음)

        System.out.println("\n[lower/upper bound로 개수 세기]");
        int lo = lowerBound(a, 2);
        int hi = upperBound(a, 2);
        System.out.println("  2 이상 첫 위치(lowerBound) = " + lo);   // 1
        System.out.println("  2 초과 첫 위치(upperBound) = " + hi);   // 4
        System.out.println("  → 2의 개수 = upper - lower = " + (hi - lo));  // 3

        System.out.println("\n[참고] 자바 내장 Arrays.binarySearch(a, 5) = "
                + Arrays.binarySearch(a, 5));
    }
}
