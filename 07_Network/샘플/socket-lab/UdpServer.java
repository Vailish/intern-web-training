import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

/**
 * UDP 에코 서버: 받은 메시지(데이터그램)를 보낸 쪽으로 그대로 돌려줍니다.
 *
 * 실행:  java "-Dfile.encoding=UTF-8" UdpServer.java          (기본 포트 9001)
 * 종료:  Ctrl + C
 *
 * TCP 서버와 비교해 보세요.
 *  - accept() 가 없습니다. "연결"이라는 개념 자체가 없기 때문입니다.
 *  - 누가 보냈는지는 매 패킷(DatagramPacket)에 적힌 주소로만 압니다.
 */
public class UdpServer {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9001;

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("[UDP 서버] " + port + " 포트에서 수신 대기 중... (종료: Ctrl+C)");
            byte[] buf = new byte[1500];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // 데이터그램 하나가 도착할 때까지 대기

                String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                String who = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                System.out.println("[수신] " + who + " > " + msg);

                byte[] reply = ("[echo] " + msg).getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(reply, reply.length, packet.getAddress(), packet.getPort()));
            }
        }
    }
}
