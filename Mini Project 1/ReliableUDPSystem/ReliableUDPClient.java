package ReliableUDPSystem;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReliableUDPClient {
    private static DatagramSocket socket = null;
    private static String serverIP = null;
    private static int serverPort = 7007;
    private static InetAddress serverAdress;
    private static int tries;

    public static void main(String[] args) {

        // Ask for port
        Scanner sc = new Scanner(System.in);
        while (socket == null) {
            try {
                System.out.println("Choose a port:");
                int incomingPort = sc.nextInt();
                sc.nextLine();
                if (incomingPort > 1024) {
                    socket = new DatagramSocket(incomingPort);
                    socket.setSoTimeout(200);
                }
                System.out.println("");
            } catch (BindException e) {
                System.out.println("Port already bound, try another");
            } catch (Exception e) {
                e.printStackTrace();
                sc.close();
                return;
            }
        }

        // Ask for ip
        while (serverIP == null) {
            System.out.println("Choose a destination ip:");
            String input = sc.nextLine();

            String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);

            boolean matchFound = matcher.find();
            if (matchFound) {
                serverIP = input;
            } else {
                System.out.println("Not a valid ip");
            }
            System.out.println("");
        }

        try {
            serverAdress = InetAddress.getByName(serverIP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ask for message
        String msg;
        do {
            System.out.println("Type a message..");
            msg = sc.nextLine();
        } while (msg.length() > 255);
        sc.close();

        // Generate UUID

        UUID uuid = UUID.randomUUID();
        String msgid = uuid.toString();

        String msgWithId = msgid + msg;

        byte[] msgArray = msgWithId.getBytes();
        DatagramPacket packet = new DatagramPacket(msgArray, msgArray.length, serverAdress, serverPort);

        while (tries < 10) {
            try {
                socket.send(packet);

                // Receive
                byte[] receivedBuffer = new byte[1000];
                DatagramPacket receivedPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
                socket.receive(receivedPacket);

                String ack = new String(receivedPacket.getData()).trim();

                if (ack.equals(msgid)) {
                    System.out.println("Received ack");
                    break;
                }
            } catch (SocketTimeoutException ste) {
                tries++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
