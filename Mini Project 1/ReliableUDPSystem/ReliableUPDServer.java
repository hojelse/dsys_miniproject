package ReliableUDPSystem;

import java.net.*;
import java.io.*;

public class ReliableUPDServer {
    private static int incomingPort = 7007;
    private static ObjectTransformer objectTransformer;

    public static void main(String args[]) {

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(incomingPort);
            System.out.println("Server listening on port " + incomingPort);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        objectTransformer = new ObjectTransformer();

        try {

            while (true) {
                byte[] buffer = new byte[1000]; // Allocate a buffer into which the reply message is written
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivedPacket);

                PacketData receivedMessage = objectTransformer.bytesToMessage(receivedPacket.getData());

                switch(receivedMessage.getType()){
                    case SYN:
                        System.out.println("Recevied SYN["+receivedMessage.getSyn()+"] from: " + receivedPacket.getAddress());
                        PacketData syn_ack_data = new PacketData(PacketTypeEnum.SYN_ACK);
                        syn_ack_data.setSynAck(receivedMessage.getSeq() + 1);
                        syn_ack_data.setSeq(153);
                        byte[] syn_ack_array = objectTransformer.messageToBytes(syn_ack_data);
                        DatagramPacket packet = new DatagramPacket(syn_ack_array, syn_ack_array.length, receivedPacket.getAddress(), receivedPacket.getPort());
                        socket.send(packet);
                        break;
                    case SYN_ACK:
                        break;
                    case ACK:
                        break;
                    case MSG:
                        System.out.println(receivedMessage.getData());
                        break;
                }
              
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
