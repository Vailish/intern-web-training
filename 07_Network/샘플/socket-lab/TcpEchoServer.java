import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * TCP 에코 서버: 클라이언트가 보낸 줄을 그대로 돌려줍니다.
 *
 * 실행:  java "-Dfile.encoding=UTF-8" TcpEchoServer.java          (기본 포트 9000)
 *        java "-Dfile.encoding=UTF-8" TcpEchoServer.java 9100     (포트 지정)
 * 종료:  Ctrl + C
 *
 * 관찰 포인트
 *  - ServerSocket 을 만들면 그 포트에서 "대기(LISTEN)" 합니다.  →  netstat -ano | findstr :9000
 *  - accept() 가 리턴되는 순간 = 3-way handshake 가 끝나 연결이 성립(ESTABLISHED)된 순간입니다.
 *  - 클라이언트 쪽 포트 번호가 매번 다른 것(임시 포트)을 확인해 보세요.
 */
public class TcpEchoServer {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9000;

        // try-with-resources: 서버가 끝날 때 소켓을 자동으로 닫습니다.
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[TCP 서버] " + port + " 포트에서 대기(LISTEN) 중... (종료: Ctrl+C)");

            while (true) {
                // 클라이언트가 접속할 때까지 여기서 멈춰 기다립니다.
                Socket client = server.accept();
                // 여러 클라이언트를 동시에 받을 수 있도록 접속마다 스레드를 하나씩 붙입니다.
                new Thread(() -> handle(client)).start();
            }
        }
    }

    private static void handle(Socket client) {
        String who = client.getInetAddress().getHostAddress() + ":" + client.getPort();
        System.out.println("[연결 성립] " + who + "  ← 클라이언트의 IP:임시포트");

        try (client;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {

            String line;
            // readLine() 이 null 이면 상대가 연결을 닫은 것(FIN 수신)입니다.
            while ((line = in.readLine()) != null) {
                System.out.println("[수신] " + who + " > " + line);
                out.print("[echo] " + line + "\r\n");
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("[오류] " + who + " " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println("[연결 종료] " + who);
    }
}
