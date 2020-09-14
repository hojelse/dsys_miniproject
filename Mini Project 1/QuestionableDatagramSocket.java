import java.io.IOException;
import java.net.SocketException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.Random;

public class QuestionableDatagramSocket extends DatagramSocket {

    private boolean reorder = false;
    public Random rnd;
    public DatagramPacket reorderHolder;

    int sends = 0;
    int reorders = 0;
    int discards = 0;
    int duplicates = 0;

    public QuestionableDatagramSocket(int port) throws SocketException {
        super(port);
        rnd = new Random();
        reorderHolder = new DatagramPacket(new byte[0], 0);
    }

    enum cases {
        DISCARD, REORDER, DUPLICATE, SEND
    }

    public boolean questionableSend(DatagramPacket p) throws IOException {
        int rndInt = rnd.nextInt(4);
        cases c = cases.values()[rndInt];

        if (reorder) {
            send(p);
            send(reorderHolder);
            reorder = false;
        } else {

            switch (c) {
                case DISCARD:
                    discards++;
                    break;
                case REORDER:
                    reorders++;
                    reorderHolder = p;
                    reorder = true;
                    return true;
                case DUPLICATE:
                    duplicates++;
                    send(p);
                    send(p);
                    break;
                case SEND:
                    sends++;
                    send(p);
                    break;
            }
        }
        return false;
    }

    public void printStats() {
        System.out.println("======= Actual error tally =======");
        System.out.println("Sends: " + sends);
        System.out.println("Discards: " + discards);
        System.out.println("Duplicates: " + duplicates);
        System.out.println("Reorders: " + reorders);
    }
}
