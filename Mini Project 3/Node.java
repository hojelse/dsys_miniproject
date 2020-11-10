import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Node {

  private Socket toNode = null;
  private Socket fromNode = null;
  private Address selfAddress;

  public Node(String[] args) throws Exception {
    var localPort = Integer.parseInt(args[0]);
    // (optional) node to connect to
    var ip = args.length > 2 ? args[1] : null;
    Integer port = args.length > 2 ? Integer.parseInt(args[2]) : null;

    HashMap<Integer, Put> puts = new HashMap<>();
    ServerSocket serverSocket = new ServerSocket(localPort);

    selfAddress = new Address(getSelfAddress(), localPort);
    HashSet<Socket> sockets = new HashSet<>();

    if (ip != null && port != null) {
      // 1. We want to connect to a Node
      try {
        toNode = new Socket(ip, port);
        sockets.add(toNode);
        var connect = new Connect(selfAddress.toString(), toAddressString(ip, port), 0);
        var os = new ObjectOutputStream(toNode.getOutputStream());
        os.writeObject(connect);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    Runtime.getRuntime().addShutdownHook(
      new Thread(() -> {
        try {
          if (toNode != null) toNode.close();
          if (fromNode != null) fromNode.close();
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
          // System.out.println("Connected " + socket.toString());
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
                    // outgoingNode.
                  }
                } else if (object instanceof Connect) {

                  Connect connect = (Connect) object;
                  var fromAddress = Address.fromString(connect.from);
                  var toAddress = Address.fromString(connect.to);

                  System.out.println(connect);

                  // System.out.println("connect from: " + fromAddress.toString());
                  // System.out.println("connect to: " + toAddress.toString());

                  System.out.println("step " + connect.step);

                  switch(connect.step) {
                    case 0:
                      if (this.fromNode == null) {
                        this.toNode = socket;
                        this.fromNode = socket;
                      }

                      var prevFrom = this.fromNode;
                      this.fromNode = socket;

                      new ObjectOutputStream(prevFrom.getOutputStream()).writeObject(
                        new Connect(this.selfAddress.toString(), connect.to, 1)
                      );
                      break;

                    case 1:
                      this.toNode.close();
                      this.toNode = new Socket(toAddress.getIP(), toAddress.getPort());
                      if (this.fromNode == null) {
                        this.fromNode = this.toNode;
                      }
                      new ObjectOutputStream(this.toNode.getOutputStream()).writeObject(
                        new Connect(this.selfAddress.toString(), toAddress.toString(), 2)
                      );
                      break;

                    case 2:
                      this.fromNode = socket;
                      break;
                  }

                  // System.out.println("from: " + Address.fromSocket(fromNode).toString());
                  // System.out.println("to: " + Address.fromSocket(toNode).toString());

                } else {
                  throw new Exception("wtf is dis object?");
                }

            } catch (EOFException ex) {
                socket.close();
                toBeRemoved.add(socket);
                // System.out.println("End of file reached");
            } catch (SocketException e) {
              toBeRemoved.add(socket);
            }
        }
        for (var socket : toBeRemoved) {
            socket.close();
            sockets.remove(socket);
        }
      }
    }
  }

  private String getSelfAddress() throws SocketException, UnknownHostException {
    final DatagramSocket selfSocket = new DatagramSocket();
    selfSocket.connect(InetAddress.getByName("8.8.8.8"/*8888 tak peter ;)*/), 0);
    var selfIP = selfSocket.getLocalAddress().getHostAddress();
    selfSocket.close();
    return selfIP;
  }

  public static void main(String[] args) throws Exception {
    new Node(args);
  }

  private static String toAddressString(String ip, int port) {
    return ip + ":" + port;
  }
}
