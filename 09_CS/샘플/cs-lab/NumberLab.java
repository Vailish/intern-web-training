import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 숫자 표현의 함정 4가지를 직접 확인합니다.  (문서: 02_숫자와_문자는_어떻게_저장되나)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" NumberLab.java
 *
 * 관찰 포인트
 *  1. int 는 32비트라서 약 21억을 넘으면 "음수로" 뒤집힌다 (오버플로, 에러 안 남!)
 *  2. 0.1 + 0.2 는 0.3 이 아니다 (2진수로 0.1을 정확히 못 적음) → 돈 계산에 double 금지
 *  3. BigDecimal 도 만드는 방법을 틀리면 소용없다 (new BigDecimal(0.1) vs new BigDecimal("0.1"))
 *  4. Integer 를 == 로 비교하면 127까지는 true, 128부터 false (캐시 때문)
 */
public class NumberLab {

    public static void main(String[] args) {
        System.out.println("=== 1. 정수 오버플로 ===");
        int max = Integer.MAX_VALUE;
        System.out.println("Integer.MAX_VALUE      = " + max);
        System.out.println("Integer.MAX_VALUE + 1  = " + (max + 1) + "   <- 에러 없이 음수가 됨");
        System.out.println("2진수 MAX_VALUE        = " + Integer.toBinaryString(max) + "  (31자리, 맨 앞 부호비트 0 생략)");
        System.out.println("2진수 MAX_VALUE + 1    = " + Integer.toBinaryString(max + 1) + "  (부호비트가 1 → 음수)");
        System.out.println("-1 의 2진수            = " + Integer.toBinaryString(-1) + "  (2의 보수: 모든 비트가 1)");

        // 실무 예: 누적 조회수, 밀리초 계산이 int 범위를 넘는 경우
        int days = 30;
        int wrongMillis = days * 24 * 60 * 60 * 1000;          // int 끼리 곱해서 넘침
        long rightMillis = days * 24L * 60 * 60 * 1000;        // 24L 하나로 long 계산이 됨
        System.out.println("30일을 밀리초로 (int)  = " + wrongMillis + "   <- 틀림");
        System.out.println("30일을 밀리초로 (long) = " + rightMillis);
        try {
            Math.multiplyExact(max, 2);                         // 넘치면 예외를 던지는 안전한 버전
        } catch (ArithmeticException e) {
            System.out.println("Math.multiplyExact     → " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== 2. 실수(double)의 오차 ===");
        System.out.println("0.1 + 0.2              = " + (0.1 + 0.2));
        System.out.println("0.1 + 0.2 == 0.3 ?     = " + (0.1 + 0.2 == 0.3));
        double sum = 0;
        for (int i = 0; i < 10; i++) sum += 0.1;
        System.out.println("0.1 을 10번 더하면     = " + sum);
        System.out.println("1.1 * 3                = " + (1.1 * 3));

        System.out.println();
        System.out.println("=== 3. BigDecimal 은 '문자열'로 만든다 ===");
        System.out.println("new BigDecimal(0.1)    = " + new BigDecimal(0.1) + "   <- double 오차가 그대로 들어감");
        System.out.println("new BigDecimal(\"0.1\")  = " + new BigDecimal("0.1"));
        System.out.println("BigDecimal.valueOf(0.1)= " + BigDecimal.valueOf(0.1) + "   <- 이것도 안전");
        BigDecimal a = new BigDecimal("0.1");
        BigDecimal b = new BigDecimal("0.2");
        System.out.println("0.1 + 0.2 (BigDecimal) = " + a.add(b));
        // 나누기는 반올림 규칙을 정하지 않으면 예외가 난다 (1/3 = 0.333... 끝이 없음)
        try {
            BigDecimal.ONE.divide(new BigDecimal("3"));
        } catch (ArithmeticException e) {
            System.out.println("1 / 3 (규칙 없음)      → " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println("1 / 3 (소수 2자리 반올림)= " + BigDecimal.ONE.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP));
        // equals 는 소수점 자릿수(scale)까지 비교한다 → 값 비교는 compareTo
        BigDecimal x = new BigDecimal("1.0");
        BigDecimal y = new BigDecimal("1.00");
        System.out.println("1.0 equals 1.00 ?      = " + x.equals(y) + "   <- 자릿수가 달라서 false");
        System.out.println("1.0 compareTo 1.00     = " + x.compareTo(y) + "      <- 0 이면 값이 같음");

        System.out.println();
        System.out.println("=== 4. Integer 를 == 로 비교하면? ===");
        Integer i1 = 127, i2 = 127;
        Integer j1 = 128, j2 = 128;
        System.out.println("Integer 127 == 127     = " + (i1 == i2) + "    <- -128~127 은 같은 객체를 재사용(캐시)");
        System.out.println("Integer 128 == 128     = " + (j1 == j2) + "   <- 서로 다른 객체 → 주소 비교라 false");
        System.out.println("Integer 128 equals 128 = " + j1.equals(j2) + "    <- 값 비교는 equals");
    }
}
