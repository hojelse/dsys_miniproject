import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Node {

  public static void main(String[] args) throws Exception {

    int localPort = Integer.parseInt(args[0]);
    var ip = args.length > 1 ? args[1] : "localhost";

    HashMap<Integer, Put> puts = new HashMap<>();
    String outgoingNode;
    String ingoingNode;
    
    ServerSocket serverSocket = new ServerSocket(localPort);
    
    HashSet<Socket> sockets = new HashSet<>();
    
    Runtime.getRuntime().addShutdownHook(
      new Thread(() -> {
        try {
          serverSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      })
    );

    
    new Thread(() -> {
      while (true) {
        try {
          Socket socket = serverSocket.accept();
          System.out.println("Connected " + socket.toString());
          synchronized (sockets) {
            sockets.add(socket);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }).start();

    while (true) {
      synchronized(sockets) {
        List<Socket> toBeRemoved = new ArrayList<>();
        for (Socket socket : sockets) {
            try {
                // Read messages from sockets
                String output;
                Object object = new ObjectInputStream(socket.getInputStream()).readObject();
                if (object instanceof Put) {
                  Put input = (Put) object;
                  System.out.println(input.toString());
                } else if (object instanceof Get) {
                  System.out.println("it was get!!!");
                  Get get = (Get) object;
                  if (puts.containsKey(get.key)) {
                    //TODO add stuff
                    //Put put = new Put()
                  }



                } else {
                  throw new Exception("wtf is dis object?");
                }

            } catch (EOFException ex) {
                socket.close();
                toBeRemoved.add(socket);
                System.out.println("End of file reached");
            }
        }
        for (var socket : toBeRemoved) {
            sockets.remove(socket);
        }


      }
    }

  }
}
