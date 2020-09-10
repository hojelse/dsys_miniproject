import java.io.IOException;
import java.net.SocketException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.Random;

public class QuestionableDatagramSocket extends DatagramSocket {

    private boolean reorder = false;
    public Random rnd;
    public DatagramPacket reorderHolder;

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

        switch(c){
            case DISCARD:
                System.out.println("");
                System.out.println("Discarding");
                System.out.println("");
                break;
                case REORDER:
                System.out.println("");
                System.out.println("Reordering... Awaiting next message");
                System.out.println("");
                reorderHolder = p;
                reorder = true;
                return true;
                case DUPLICATE:
                System.out.println("");
                System.out.println("Duplicating");
                System.out.println("");
                send(p);
                send(p);
                break;
                case SEND:
                System.out.println("");
                System.out.println("Sending");
                System.out.println("");
                send(p);
                break;
        }}
        return false;
    }
}
