package main.java.Mandatory_Exercise_1;

import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class UDPServer {
    private static int outgoingPort = 1337;
    private static int incomingPort = 7007;

    public static void main(String args[]) {
        System.out.println("Server listening on port " + incomingPort);
        DatagramSocket socket0;
        try {
            socket0 = new DatagramSocket(incomingPort);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Connection c;
        Statement stmt;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:55513/w01", "postgres", "databasekode");




        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        try {

            while (true) { // Keep asking user for messages.
                // Receive reply
                byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply message is written
                DatagramPacket incomingMessage = new DatagramPacket(buffer, buffer.length);
                socket0.receive(incomingMessage);

                // Print reply message
                System.out.println("Received: \"" + new String(incomingMessage.getData()).trim() + "\" from "
                        + incomingMessage.getAddress());

                // Read a message from standard input
                String msg = new String(incomingMessage.getData()).trim();
                byte[] msgBytes = msg.getBytes();

                // Send the message
                InetAddress replyAddress = incomingMessage.getAddress();
                DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, replyAddress, outgoingPort);
                socket0.send(request);

                System.out.println(
                        "Send back: \"" + new String(incomingMessage.getData()).trim() + "\" to " + replyAddress);

            }
        } catch (SocketException e) { // Handle socket errors
            System.out.println("Socket exception: " + e.getMessage());
        } catch (IOException e) { // Handle IO errors
            System.out.println("IO exception: " + e.getMessage());
        } finally { // Close socket
            if (socket0 != null)
                socket0.close();
        }
    }
}
