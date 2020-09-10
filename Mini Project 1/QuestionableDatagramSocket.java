import java.io.IOException;
import java.net.SocketException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.Random;

public class QuestionableDatagramSocket extends DatagramSocket{
    public Random rnd;
    public DatagramPacket reorderHolder;
    
    public QuestionableDatagramSocket(int port) throws SocketException{
        super(port);
        rnd = new Random();
        reorderHolder = new DatagramPacket(new byte[0], 0);
    }

    public void questionableReceive(DatagramPacket p) throws IOException{
        int rndInt = rnd.nextInt(4);
        
        receive(p);

        switch(rndInt){
            case 0: //DISCARD
                System.out.println("Discarding");
                break;
            case 1: //REORDER
                //SEND CONTENTS OF reorderHolder, HOLD p IN FIELD reorderHolder 
                System.out.println("Reordering");
                if(reorderHolder != null){
                    p.setAddress(p.getAddress());
                    p.setPort(p.getPort());
                    send(reorderHolder);
                }
                reorderHolder = p;
                break;
            case 2: //DUPLICATE
                //ECHO TO SENDER
                System.out.println("Duplicating");
                System.out.println("Sending");
                p.setAddress(p.getAddress());
                p.setPort(p.getPort());
                send(p);
            case 3: //SEND
                //ECHO TO SENDER
                System.out.println("Sending");
                p.setAddress(p.getAddress());
                p.setPort(p.getPort());
                send(p);
        }
    }

    public void questionableSend(DatagramPacket p) throws IOException {
        int rndInt = rnd.nextInt(4);

        switch(rndInt){
            case 0: //DISCARD
                System.out.println("Discarding");
                break;
            case 1: //REORDER
                //SEND CONTENTS OF reorderHolder, HOLD p IN FIELD reorderHolder 
                System.out.println("Reordering");
                send(reorderHolder);
                reorderHolder = p;
                break;
            case 2: //DUPLICATE
                //ECHO TO SENDER
                System.out.println("Duplicating");
                System.out.println("Sending");
                send(p);
            case 3: //SEND
                //ECHO TO SENDER
                System.out.println("Sending");
                send(p);
        }
    }
}