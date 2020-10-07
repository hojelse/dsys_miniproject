
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPClient {
    private static String serverIP = "localhost"; // Kristoffers IP
    private static int incomingPort = 1338;
    private static int outgoingPort = 10001;

    public static void main(String args[]) {

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(incomingPort);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Scanner msgScan = new Scanner(System.in);

        try {
            while (true) { // Keep asking user for messages.
                System.out.println("Type a message..");

                // Read a message from standard input
                String msg = msgScan.nextLine();
                byte[] msgBytes = msg.getBytes();

                // Send the message

                InetAddress aHost = InetAddress.getByName(serverIP);
                DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, aHost, outgoingPort);
                socket.send(request);

                System.out.println("Messeage sent. Waiting for reply from");

                System.out.println(aHost);

                // // Receive reply
                // byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply
                // message is written
                // DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                // socket.receive(reply);

                // // Print reply message
                // System.out.println("Received reply: \"" + new String(reply.getData()).trim()
                // + "\"");

            }
        } catch (SocketException e) { // Handle socket errors
            System.out.println("Socket exception: " + e.getMessage());
        } catch (IOException e) { // Handle IO errors
            System.out.println("IO exception: " + e.getMessage());
        } finally { // Close socket
            if (socket != null)
                socket.close();
        }
    }
}
