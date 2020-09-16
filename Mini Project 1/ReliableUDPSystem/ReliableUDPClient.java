package ReliableUDPSystem;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ReliableUDPClient {
    private static DatagramSocket socket = null;
    private static ObjectTransformer objectTransformer;
    private static InetAddress serverIP;
    private static int tries;

    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        while (socket == null) {
            try {
                System.out.println("Choose a port:");
                int incomingPort = s.nextInt();
                s.nextLine();
                if (incomingPort > 1024) {
                    socket = new DatagramSocket(incomingPort);
                    socket.setSoTimeout(200);
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

        objectTransformer = new ObjectTransformer();

        try {
            serverIP = InetAddress.getByName("localhost");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Type a message..");
        String msg = s.nextLine();

        tries = 0;
        boolean SYN_ACK_RECV = false;
        while(!SYN_ACK_RECV && tries < 5) {
            try {
                PacketData syn_data = new PacketData(PacketTypeEnum.SYN);
                syn_data.setSyn(111);

                byte[] syn_array = objectTransformer.messageToBytes(syn_data);
                DatagramPacket syn_packet = new DatagramPacket(syn_array, syn_array.length, serverIP, 7007);

                socket.send(syn_packet);

                byte[] recv_buffer = new byte[1000];
                DatagramPacket recv_packet = new DatagramPacket(recv_buffer, recv_buffer.length);
                socket.receive(recv_packet);
                PacketData recv_data = objectTransformer.bytesToMessage(recv_packet.getData());
                System.out.println(recv_data.getSynAck());
            } catch (SocketTimeoutException e) {
                tries++;
            } catch (Exception e) {
                tries++;
            }
        }
    }
}
