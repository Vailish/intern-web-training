import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * UDP 클라이언트: 입력한 줄을 데이터그램 하나로 보내고, 답장을 잠깐 기다립니다.
 *
 * 실행:  java "-Dfile.encoding=UTF-8" UdpClient.java                  (localhost 9001)
 * 종료:  Ctrl + Z 후 Enter (Windows) / Ctrl + D (macOS·리눅스)
 *
 * ⭐ 핵심 실험: UdpServer 를 끈 상태에서 보내 보세요.
 *    TCP 는 연결 단계에서 바로 "Connection refused" 가 나지만,
 *    UDP 는 send() 가 아무 에러 없이 끝납니다. 상대가 있는지 확인하지 않고 "던지고 끝"이기 때문입니다.
 *    답장이 오지 않는 것으로만 알 수 있습니다.
 */
public class UdpClient {

    private static final int REPLY_TIMEOUT_MS = 2000;

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9001;
        InetAddress address = InetAddress.getByName(host);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(REPLY_TIMEOUT_MS);
            System.out.println("[UDP 클라이언트] " + host + ":" + port + " 로 보냅니다. (연결 단계 없음)");
            System.out.println("                 (보낼 내용을 입력하고 Enter. 끝내려면 Ctrl+Z 후 Enter)");

            // 콘솔에서 타이핑한 한글이 깨지지 않도록 콘솔의 인코딩으로 읽습니다. (TcpClient.keyboardCharset 참고)
            Console console = System.console();
            Charset keyboardCharset = console != null ? console.charset() : Charset.defaultCharset();
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in, keyboardCharset));
            String line;
            while ((line = keyboard.readLine()) != null) {
                byte[] data = line.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, address, port));
                System.out.println("[전송 완료] \"" + line + "\"  ← send() 는 에러 없이 끝났습니다");

                try {
                    byte[] buf = new byte[1500];
                    DatagramPacket reply = new DatagramPacket(buf, buf.length);
                    socket.receive(reply);
                    System.out.println("[답장] " + new String(reply.getData(), 0, reply.getLength(), StandardCharsets.UTF_8));
                } catch (SocketTimeoutException e) {
                    System.out.println("[답장 없음] " + REPLY_TIMEOUT_MS + "ms 동안 응답이 없습니다. 메시지가 도착했는지 UDP는 알려주지 않습니다.");
                }
            }
        }
    }
}
