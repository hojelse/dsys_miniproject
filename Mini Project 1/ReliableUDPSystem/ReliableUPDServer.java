package ReliableUDPSystem;

import java.net.*;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.io.*;

public class ReliableUPDServer {
    private static int incomingPort = 7007;
    private static HashSet<String> msgIds = new HashSet<>();

    public static void main(String args[]) {

        // Open socket
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(incomingPort);
            System.out.println("Server listening on port " + incomingPort);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            while (true) {
                byte[] receivedBuffer = new byte[1000];
                DatagramPacket receivedPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
                socket.receive(receivedPacket);

                int idLength = 36;

                String receivedMsg = new String(receivedPacket.getData()).trim();

                String id = receivedMsg.substring(0, idLength);
                String msg = receivedMsg.substring(idLength, receivedMsg.length());

                // Check msgid set before print
                if (msgIds.contains(id))
                    continue;
                msgIds.add(id);
                System.out.println(msg);

                // Send acknowledgment
                byte[] sendBuffer = id.getBytes();
                DatagramPacket ackPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                        receivedPacket.getAddress(), receivedPacket.getPort());
                socket.send(ackPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
