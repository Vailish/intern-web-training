import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 범용 TCP 클라이언트: 키보드로 입력한 줄을 서버로 보내고, 서버가 보낸 것은 무엇이든 화면에 출력합니다.
 *
 * 실행:  java "-Dfile.encoding=UTF-8" TcpClient.java                    (localhost 9000 = 에코 서버)
 *        java "-Dfile.encoding=UTF-8" TcpClient.java localhost 8080     (스프링 앱에 HTTP를 "손으로" 보내기)
 * 종료:  Ctrl + Z 후 Enter (Windows) / Ctrl + D (macOS·리눅스) → 보내기를 끝내고 응답을 마저 받은 뒤 종료
 *
 * 이 클라이언트는 에코 서버 전용이 아닙니다. TCP 위에서 "텍스트"를 주고받을 뿐이라,
 * HTTP 서버에 접속해 HTTP 요청 문장을 직접 입력할 수도 있습니다. (05 문서 실습)
 *
 * 연결에 실패하면 "어느 단계에서 왜 실패했는지"를 알려 줍니다. (06 문서 실습)
 */
public class TcpClient {

    // 연결 시도를 기다리는 최대 시간. 응답이 없으면 이 시간 뒤에 timeout 이 납니다.
    private static final int CONNECT_TIMEOUT_MS = 3000;

    // 키보드 입력이 끝났는지 (두 스레드가 함께 보므로 volatile)
    private static volatile boolean inputDone = false;

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9000;

        // try-with-resources: 연결에 실패해서 일찍 끝나든 정상 종료하든, 소켓을 반드시 닫습니다.
        // (닫지 않으면 OS 자원이 남습니다 → 04 문서의 CLOSE_WAIT 참고)
        try (Socket socket = new Socket()) {
            try {
                System.out.println("[연결 시도] " + host + ":" + port + " ...");
                // 이 한 줄 안에서 DNS 조회 → 3-way handshake 가 일어납니다.
                socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            } catch (UnknownHostException e) {
                fail("DNS", "이름을 IP로 바꾸지 못했습니다. 호스트 이름 오타나 네트워크 연결을 확인하세요.", e);
                return;
            } catch (ConnectException e) {
                fail("TCP", "상대 컴퓨터에는 닿았지만, 그 포트에서 기다리는 프로그램이 없습니다(RST 수신). 서버 실행 여부와 포트 번호를 확인하세요.", e);
                return;
            } catch (SocketTimeoutException e) {
                fail("TCP", CONNECT_TIMEOUT_MS + "ms 동안 아무 응답이 없습니다(SYN에 무응답). 방화벽·보안그룹 차단이나 잘못된 IP를 의심하세요.", e);
                return;
            }

            System.out.println("[연결 성립] 내 주소 " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort()
                    + "  →  서버 " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
            System.out.println("           (보낼 내용을 입력하고 Enter. 끝내려면 Ctrl+Z 후 Enter)");

            // 받기 담당 스레드: 서버가 보내는 것을 오는 대로 화면에 출력합니다.
            Thread reader = new Thread(() -> printAll(socket));
            reader.start();

            // 보내기 담당(메인 스레드): 키보드 한 줄 = 한 줄 전송. 줄 끝은 HTTP 규칙과 같은 CRLF(\r\n).
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in, keyboardCharset()));
            OutputStream out = socket.getOutputStream();
            try {
                String line;
                while ((line = keyboard.readLine()) != null) {
                    if (!reader.isAlive()) {
                        break; // 서버가 이미 연결을 닫았으면 더 보내지 않고 끝냅니다.
                    }
                    out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                inputDone = true;
                if (reader.isAlive()) {
                    // "나는 더 보낼 게 없다"(FIN)만 알리고, 서버의 남은 응답은 끝까지 받습니다.
                    socket.shutdownOutput();
                    reader.join();
                }
            } catch (IOException e) {
                System.out.println("[전송 실패] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    /** 서버가 연결을 닫을 때까지 받은 내용을 화면에 출력합니다. */
    @SuppressWarnings("resource") // 스트림을 여기서 닫으면 소켓 전체가 닫힙니다. 닫기는 main 의 try-with-resources 가 맡습니다.
    private static void printAll(Socket socket) {
        try {
            // InputStreamReader 로 읽으면 한글(3바이트)이 패킷 경계에서 잘려 와도 깨지지 않게 이어 붙여 줍니다.
            InputStreamReader in = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            char[] buf = new char[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                System.out.print(new String(buf, 0, n));
                System.out.flush();
            }
            System.out.println("\n[서버가 연결을 닫았습니다 (FIN 수신)]" + (inputDone ? "" : "  Enter 를 누르면 종료합니다."));
        } catch (IOException e) {
            System.out.println("\n[연결 끊김] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * 키보드 입력의 문자 인코딩.
     * 콘솔에서 직접 타이핑하면 콘솔의 인코딩(한글 Windows는 보통 MS949)을 쓰고,
     * 파일·파이프로 입력을 넣으면(콘솔 없음) 기본 인코딩(-Dfile.encoding)을 씁니다.
     * 이렇게 해야 타이핑한 한글이 서버로 갈 때 깨지지 않습니다. (Console.charset()은 Java 17부터)
     */
    private static Charset keyboardCharset() {
        Console console = System.console();
        return console != null ? console.charset() : Charset.defaultCharset();
    }

    private static void fail(String stage, String meaning, Exception e) {
        System.out.println("[실패 단계] " + stage);
        System.out.println("[의미]      " + meaning);
        System.out.println("[원본 예외] " + e.getClass().getName() + ": " + e.getMessage());
    }
}
