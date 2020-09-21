import java.io.IOException;
import java.net.SocketException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;
import java.util.Stack;

public class QuestionableDatagramSocket extends DatagramSocket {

    private Stack<DatagramPacket> packets = new Stack<>();
    public Random rnd;

    int sends = 0;
    int reorders = 0;
    int discards = 0;
    int duplicates = 0;

    public QuestionableDatagramSocket(int port) throws SocketException {
        super(port);
        rnd = new Random();
    }

    enum cases {
        DISCARD, REORDER, DUPLICATE, SEND
    }

    @Override
    public void send(DatagramPacket p) {
        int rndInt = rnd.nextInt(4);
        cases c = cases.values()[rndInt];

        try {
            switch (c) {
                case DISCARD:
                    discards++;
                    break;
                case REORDER:
                    reorders++;
                    packets.push(p);
                    break;
                case DUPLICATE:
                    duplicates++;
                    packets.push(p);
                    packets.push(p);
                    sendAllPackets();
                    break;
                case SEND:
                    sends++;
                    packets.push(p);
                    sendAllPackets();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAllPackets() throws IOException {
        while (!packets.empty()) {
            super.send(packets.pop());
        }
    }

    public void printStats() {
        System.out.println("======= Actual error tally =======");
        System.out.println("Sends: " + sends);
        System.out.println("Discards: " + discards);
        System.out.println("Duplicates: " + duplicates);
        System.out.println("Reorders: " + reorders);
    }
}
