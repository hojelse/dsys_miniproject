import java.io.IOException;
import java.net.SocketException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.Random;

public class QuestionableDatagramSocket extends DatagramSocket {

    private DatagramPacket reorderHolder; 
    private boolean reorder = false;
    public Random rnd;

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

    @Override
    public void send(DatagramPacket p){
        int rndInt = rnd.nextInt(4);
        cases c = cases.values()[rndInt];

        try{
            if (reorder) {
                super.send(p);
                super.send(reorderHolder);
                reorder = false;
            } else {
                switch (c) {
                    case DISCARD:
                        System.out.println("dis");
                        discards++;
                        break;
                    case REORDER:
                        System.out.println("re");
                        reorders++;
                        reorderHolder = p;
                        reorder = true;
                        throw new RuntimeException("Reorder");
                    case DUPLICATE:
                        System.out.println("dup");
                        duplicates++;
                        super.send(p);
                        super.send(p);
                        break;
                    case SEND:
                        System.out.println("send");
                        sends++;
                        super.send(p);
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
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
