import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPServer {
    private static int serverPort = 6969;
    private static int clientPort = 1337;

    public static void main(String args[]) {
        System.out.println("oh no i am server");

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
                // Receive reply
                byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply message is written
                DatagramPacket incomingMessage = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(incomingMessage);

                // Print reply message
                System.out.println("");
                System.out.println("Received: " + new String(incomingMessage.getData()).trim());
                System.out.println("Sending back: " + new String(incomingMessage.getData()).trim());

                // Read a message from standard input
                String msg = new String(incomingMessage.getData()).trim();
                byte[] msgBytes = msg.getBytes();

                // Send the message

                InetAddress aHost = InetAddress.getByName("localhost");
                DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, aHost, serverPort);
                aSocket.send(request);

                System.out.println(aHost);

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
