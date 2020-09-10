
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class QuestionableUPDClient {
    private static String serverIP = "10.26.55.161";
    private static int outgoingPort = 7007;

    public static void main(String args[]) {
        QuestionableDatagramSocket socket = null;

        Scanner s = new Scanner(System.in);
        while (socket == null) {
            try {
                System.out.println("Choose a port:");
                int incomingPort = s.nextInt();
                s.nextLine();
                if (incomingPort > 1024) {
                    socket = new QuestionableDatagramSocket(incomingPort);
                }
                System.out.println("");
            } catch (BindException e) {
                System.out.println("Port already bound, try another");
            } catch (Exception e) {
                e.printStackTrace();
                s.close();
                return;
            }
        }

        try {
            while (true) { // Keep asking user for messages.
                System.out.println("Type a message..");

                // Read a message from standard input
                String msg = s.nextLine();
                byte[] msgBytes = msg.getBytes();

                // Send the message
                InetAddress aHost = InetAddress.getByName(serverIP);
                DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, aHost, outgoingPort);
                boolean isReordering = socket.questionableSend(request);

                if (!isReordering) {

                    System.out.println("Messeage sent. Waiting for reply from");

                    System.out.println(aHost);

                    // Receive reply
                    byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply message is written
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    socket.receive(reply);

                    // Print reply message
                    System.out.println("Received reply: \"" + new String(reply.getData()).trim() + "\"");
                }

            }
        } catch (SocketException e) { // Handle socket errors
            System.out.println("Socket exception: " + e.getMessage());
        } catch (IOException e) { // Handle IO errors
            System.out.println("IO exception: " + e.getMessage());
        } finally { // Close socket and scanner
            s.close();
            if (socket != null)
                socket.close();
        }
    }
}
