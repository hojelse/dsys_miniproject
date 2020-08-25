import java.net.*;
import java.io.*;

public class UDPServer {
    private static int serverPort = 6969;
    private static int clientPort = 1337;

    public static void main(String args[]) {
        System.out.println("Server listening on port " + serverPort);

        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(clientPort);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {

            while (true) { // Keep asking user for messages.
                // Receive reply
                byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply message is written
                DatagramPacket incomingMessage = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(incomingMessage);

                // Print reply message
                System.out.println("Received: \"" + new String(incomingMessage.getData()).trim() + "\" from "
                        + incomingMessage.getAddress());

                // Read a message from standard input
                String msg = new String(incomingMessage.getData()).trim();
                byte[] msgBytes = msg.getBytes();

                // Send the message
                InetAddress replyAddress = incomingMessage.getAddress();
                DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, replyAddress, serverPort);
                aSocket.send(request);

                System.out.println(
                        "Send back: \"" + new String(incomingMessage.getData()).trim() + "\" to " + replyAddress);

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
