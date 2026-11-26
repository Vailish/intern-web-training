import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 문자 인코딩: "글자"가 실제로 몇 바이트, 어떤 숫자로 저장되는지 보고 한글 깨짐을 일부러 재현합니다.
 * (문서: 02_숫자와_문자는_어떻게_저장되나)
 *
 * 실행:  java "-Dfile.encoding=UTF-8" EncodingLab.java
 *
 * 관찰 포인트
 *  1. 같은 "가" 라도 UTF-8 은 3바이트, MS949(EUC-KR 확장) 는 2바이트
 *  2. 저장(인코딩)과 읽기(디코딩)의 규칙이 다르면 글자가 깨진다 → 깨진 모양으로 원인을 추측할 수 있다
 *  3. String.length() 는 "바이트 수"가 아니다 → DB 컬럼 길이·전송 크기 계산에 주의
 *  4. BOM(EF BB BF): 파일 맨 앞에 붙는 눈에 안 보이는 3바이트
 */
public class EncodingLab {

    private static final Charset MS949 = Charset.forName("MS949");

    public static void main(String[] args) {
        System.out.println("=== 0. 지금 이 JVM의 기본 인코딩 ===");
        System.out.println("Charset.defaultCharset() = " + Charset.defaultCharset());
        System.out.println("(이 값이 OS·실행 옵션마다 달라서 '내 PC에선 되는데 서버에선 깨지는' 일이 생깁니다)");

        System.out.println();
        System.out.println("=== 1. 같은 글자, 다른 바이트 ===");
        String[] samples = {"A", "가", "한글", "😀"};
        for (String s : samples) {
            System.out.printf("%-4s  length()=%d  UTF-8 %d바이트 [%s]   MS949 %d바이트 [%s]%n",
                    s, s.length(),
                    s.getBytes(StandardCharsets.UTF_8).length, hex(s.getBytes(StandardCharsets.UTF_8)),
                    s.getBytes(MS949).length, hex(s.getBytes(MS949)));
        }
        System.out.println("→ 'A'(영문)는 둘 다 1바이트로 같다 (ASCII 호환). 그래서 영문은 안 깨지고 한글만 깨진다.");
        System.out.println("→ 이모지는 MS949에 없어서 '?'(3F)로 바뀐다 = 데이터 손실. length()도 2로 나온다(자바 char 2개).");
        System.out.println("   글자 수를 정확히 세려면 codePointCount: \"😀\".codePointCount = "
                + "😀".codePointCount(0, "😀".length()));

        System.out.println();
        System.out.println("=== 2. 한글 깨짐 재현 (저장 규칙 ≠ 읽기 규칙) ===");
        String original = "안녕하세요";
        byte[] utf8Bytes = original.getBytes(StandardCharsets.UTF_8);
        byte[] ms949Bytes = original.getBytes(MS949);
        System.out.println("원본                           : " + original);
        System.out.println("UTF-8로 저장 → MS949로 읽기    : " + new String(utf8Bytes, MS949));
        System.out.println("UTF-8로 저장 → 서유럽(1252)로 읽기: " + new String(utf8Bytes, Charset.forName("windows-1252")));
        System.out.println("MS949로 저장 → UTF-8로 읽기    : " + new String(ms949Bytes, StandardCharsets.UTF_8));
        System.out.println("→ 'ì•ˆë…•' 같은 모양 = UTF-8 바이트를 서유럽 문자로 읽음 (한 글자 3바이트가 3글자로 보임)");
        System.out.println("→ '�' = 읽는 규칙(UTF-8)으로는 해석할 수 없는 바이트를 대체 문자(U+FFFD)로 바꾼 것");

        System.out.println();
        System.out.println("=== 3. 한 번 잘못 읽은 걸 되돌릴 수 있나? ===");
        String broken = new String(utf8Bytes, StandardCharsets.ISO_8859_1);
        String restored = new String(broken.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        System.out.println("ISO-8859-1 로 잘못 읽은 것 복구 : " + restored + "   <- 1바이트=1글자라 바이트가 보존돼 복구됨");
        String broken2 = new String(ms949Bytes, StandardCharsets.UTF_8);
        String restored2 = new String(broken2.getBytes(StandardCharsets.UTF_8), MS949);
        System.out.println("UTF-8 로 잘못 읽은 것 복구      : " + restored2 + "   <- '�'로 바뀐 순간 원래 바이트가 사라져 복구 불가");
        System.out.println("→ 게시판·로그에서 보이는 '占쏙옙' 이 바로 이것: '�'(EF BF BD)가 저장된 뒤 MS949로 읽힌 흔적");

        System.out.println();
        System.out.println("=== 4. BOM ===");
        byte[] withBom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'G', 'E', 'T'};
        String s = new String(withBom, StandardCharsets.UTF_8);
        System.out.println("BOM + \"GET\" 을 문자열로 읽으면 화면엔 [" + s + "] 처럼 보이지만");
        System.out.println("length() = " + s.length() + ",  equals(\"GET\") = " + s.equals("GET") + ",  첫 글자 코드 = U+"
                + String.format("%04X", (int) s.charAt(0)));
        System.out.println("→ 07_Network 에서 PowerShell 파이프로 보낸 HTTP 요청이 400 이 났던 이유가 이것");
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
