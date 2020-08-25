
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPClient {
    private static String serverIP = "10.26.9.128"; // Kristoffers IP
    private static int clientPort = 6969;
    private static int serverPort = 1337;

    public static void main(String args[]) {

        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(clientPort);
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
                DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, aHost, serverPort);
                aSocket.send(request);

                System.out.println("Messeage sent. Waiting for reply from");

                System.out.println(aHost);

                // Receive reply
                byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply message is written
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);

                // Print reply message
                System.out.println("Received reply: \"" + new String(reply.getData()).trim() + "\"");

            }
        } catch (SocketException e) { // Handle socket errors
            System.out.println("Socket exception: " + e.getMessage());
        } catch (IOException e) { // Handle IO errors
            System.out.println("IO exception: " + e.getMessage());
        } finally { // Close socket
            if (aSocket != null)
                aSocket.close();
        }
    }
}
