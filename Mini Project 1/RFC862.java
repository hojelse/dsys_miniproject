import java.net.*;
import java.io.*;

public class RFC862 {
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
                DatagramPacket message = new DatagramPacket(buffer, buffer.length);
                socket.receive(message);

                // System.out.println("Received: \"" + new String(message.getData()).trim() + "\" from "
                //         + message.getAddress());

                message.setAddress(message.getAddress());
                message.setPort(message.getPort());
                socket.send(message);

                // System.out.println(
                //         "Send back: \"" + new String(message.getData()).trim() + "\" to " + message.getAddress());

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
