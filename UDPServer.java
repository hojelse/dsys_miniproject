import java.net.*;
import java.io.*;

public class UDPServer {
    private static int outgoingPort = 1337;
    private static int incomingPort = 7007;

    public static void main(String args[]) {
        System.out.println("Server listening on port " + incomingPort);

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(incomingPort);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {

            while (true) { // Keep asking user for messages.
                // Receive reply
                byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply message is written
                DatagramPacket incomingMessage = new DatagramPacket(buffer, buffer.length);
                socket.receive(incomingMessage);

                // Print reply message
                System.out.println("Received: \"" + new String(incomingMessage.getData()).trim() + "\" from "
                        + incomingMessage.getAddress());

                // Read a message from standard input
                String msg = new String(incomingMessage.getData()).trim();
                byte[] msgBytes = msg.getBytes();

                // Send the message
                InetAddress replyAddress = incomingMessage.getAddress();
                DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, replyAddress, outgoingPort);
                socket.send(request);

                System.out.println(
                        "Send back: \"" + new String(incomingMessage.getData()).trim() + "\" to " + replyAddress);

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
