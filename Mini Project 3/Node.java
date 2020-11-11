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

  private Connection toNode = null;
  private Connection fromNode = null;
  private Connection selfAddress;

  public Node(String[] args) throws Exception {
    var localPort = Integer.parseInt(args[0]);
    // (optional) node to connect to
    var ip = args.length > 2 ? args[1] : null;
    Integer port = args.length > 2 ? Integer.parseInt(args[2]) : null;

    HashMap<Integer, Put> puts = new HashMap<>();
    ServerSocket serverSocket = new ServerSocket(localPort);

    selfAddress = new Connection(getSelfAddress(), localPort);
    HashSet<Connection> connections = new HashSet<>();

    if (ip != null && port != null) {
      // 1. We want to connect to a Node
      try {
        // Blue (1338) connects to Green (1337)
        toNode = new Connection(ip, port);
        connections.add(toNode);
        var connect = new Connect(selfAddress.toString(), toAddressString(ip, port), 0);
        var os = new ObjectOutputStream(toNode.getSocket().getOutputStream());
        os.writeObject(connect);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    Runtime.getRuntime().addShutdownHook(
      new Thread(() -> {
        try {
          if (toNode != null) toNode.getSocket().close();
          if (fromNode != null) fromNode.getSocket().close();
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
          var socketIp = socket.getInetAddress().getHostAddress();
          var socketPort = socket.getPort();
          var connection = new Connection(socketIp, socketPort, socket);
          System.out.println("New connection from " + connection);
          synchronized (connections) {
            connections.add(connection);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    while (true) {
      synchronized(connections) {
        List<Connection> toBeRemoved = new ArrayList<>();
        for (Connection connection : connections) {
            try {
                // Read messages from sockets
                String output;
                Object object = new ObjectInputStream(connection.getSocket().getInputStream()).readObject();
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
                  var fromAddress = Connection.fromString(connect.from);
                  var toAddress = Connection.fromString(connect.to);

                  System.out.println(connect);

                  // System.out.println("connect from: " + fromAddress.toString());
                  // System.out.println("connect to: " + toAddress.toString());

                  System.out.println("step " + connect.step);

                  switch(connect.step) {
                    case 0: // Green (1337) receives from blue (1338)
                      if (this.fromNode == null) {
                        // this.toNode = socket;
                        // this.fromNode = socket;
                      }

                      var prevFrom = this.fromNode;
                      this.fromNode = Connection.fromString(connect.from);

                      new ObjectOutputStream(prevFrom.getSocket().getOutputStream()).writeObject(
                        // new Connect(this.selfAddress.toString(), connect.to, 1) // Grøn til grøn
                        new Connect(fromNode.toString(), connect.from, 1) // Rød til blå
                      );
                      break;

                    case 1:
                      this.toNode.update(toAddress.getIP(), toAddress.getPort());
                      if (this.fromNode == null) {
                        this.fromNode = this.toNode;
                      }
                      new ObjectOutputStream(this.toNode.getSocket().getOutputStream()).writeObject(
                        new Connect(this.selfAddress.toString(), toAddress.toString(), 2)
                      );
                      break;

                    case 2:
                      this.fromNode = connection;
                      break;
                  }

                  // System.out.println("from: " + Address.fromSocket(fromNode).toString());
                  // System.out.println("to: " + Address.fromSocket(toNode).toString());

                } else {
                  throw new Exception("wtf is dis object?");
                }

            } catch (EOFException ex) {
                connection.getSocket().close();
                toBeRemoved.add(connection);
                // System.out.println("End of file reached");
            } catch (SocketException e) {
              toBeRemoved.add(connection);
            }
        }
        for (var socket : toBeRemoved) {
            socket.getSocket().close();
            connections.remove(socket);
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
