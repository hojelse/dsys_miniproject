import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Node {

  private Socket toNodeSocket = null;
  private Address toNodeAddress;

  private Socket fromNodeSocket = null;
  private Address fromNodeAddress;

  private boolean inNetwork;

  private Socket serviceSocket = null;
  private ServerSocket serverSocket;

  HashMap<Integer, Put> puts;

  public Node(String[] args) throws Exception {
    var localPort = Integer.parseInt(args[0]);
    // (optional) node to connect to
    var ip = args.length > 2 ? args[1] : null;
    Integer port = args.length > 2 ? Integer.parseInt(args[2]) : null;

    puts = new HashMap<>();

    serverSocket = new ServerSocket();
    var endpoint = new InetSocketAddress(InetAddress.getByName("127.0.0.1").getHostAddress(), localPort);
    serverSocket.bind(endpoint);
    System.out.println("*** ServerSocket on " + serverSocket);

    if (ip != null && port != null) { //Connect to a Node
      try {
        inNetwork = false;
        toNodeAddress = new Address(ip, port);
        toNodeSocket = new Socket(InetAddress.getByName(toNodeAddress.ip).getHostAddress(), toNodeAddress.port);
        Connect connect = new Connect(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort(), 0);
        var os = new ObjectOutputStream(toNodeSocket.getOutputStream());
        os.writeObject(connect);
        toNodeSocket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else { //Connect to nothing
      try {
        inNetwork = true;
        toNodeAddress = null;
        fromNodeAddress = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    Runtime.getRuntime().addShutdownHook(
      new Thread(() -> {
        try {
          if (toNodeSocket != null) toNodeSocket.close();
          if (fromNodeSocket != null) fromNodeSocket.close();
          serverSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      })
    );

    new Thread(() -> {
      while (true) {
        try {
          System.out.print("");
          if (serviceSocket == null) {
            serviceSocket = serverSocket.accept();
            System.out.println("Connected " + serviceSocket.toString());
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    System.out.println("Listening for service requests...");
    while (true) {
      System.out.print("");
      if (serviceSocket != null) {
        System.out.println("Service request incoming");
        try {
          Object object = new ObjectInputStream(serviceSocket.getInputStream()).readObject();
          HandleServiceRequest(object);
        } catch (Exception e) {
          e.printStackTrace();
        }
        serviceSocket.close();
        serviceSocket = null;
      }
    }
  }

  private void HandleServiceRequest(Object object) throws Exception {
    System.out.println("Service request: " + object);
    if (object instanceof Put) {
      Put((Put) object);
    } else if (object instanceof Get) {
      Get((Get) object);
    } else if (object instanceof Connect) {
      if (toNodeAddress == null && fromNodeAddress == null) {
        ConnectSingleNode((Connect) object);
      } else {
        Connect((Connect) object);
      }
    } else {
      throw new Exception("Unsupported service request");
    }
  }

  private void Put(Put put) {
    puts.put(put.key, put);
  }

  private void Get(Get get) {
    Object result;
    if (puts.containsKey(get.key)) {
      result = puts.get(get.key);
    }
    else {
      result = "No such put";
    }
    try {
      ObjectOutputStream oos = new ObjectOutputStream(serviceSocket.getOutputStream());
      oos.writeObject(result);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void Connect(Connect connect) throws IOException {
    System.out.println("Connect step " + connect.step);

    ObjectOutputStream oos;

    switch(connect.step) {
      case 0:
        fromNodeSocket = new Socket(InetAddress.getByName(fromNodeAddress.ip).getHostAddress(), fromNodeAddress.port);
        oos = new ObjectOutputStream(fromNodeSocket.getOutputStream());
        oos.writeObject(new Connect(connect.serverSocketAddress, connect.serverSocketPort, 1));

        fromNodeAddress = new Address(serviceSocket.getInetAddress().getHostAddress(), connect.serverSocketPort);
        break;

      case 1:
        toNodeSocket = new Socket("127.0.0.1", connect.serverSocketPort);
        toNodeAddress = new Address(toNodeSocket.getInetAddress().getHostAddress(), toNodeSocket.getPort());

        oos = new ObjectOutputStream(toNodeSocket.getOutputStream());
        oos.writeObject(new Connect(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort(), 2));
        oos.close();
        break;

      case 2:
        fromNodeAddress = new Address(serviceSocket.getInetAddress().getHostAddress(), connect.serverSocketPort);

        inNetwork = true;
        break;
    }
    System.out.println("toNode: " + toNodeAddress);
    System.out.println("fromNode: " + fromNodeAddress);
  }

  private void  ConnectSingleNode(Connect connect) {
    System.out.println("Connecting as single node");
    try {
      fromNodeAddress = new Address(serviceSocket.getInetAddress().getHostAddress(), connect.serverSocketPort);
      Socket s = new Socket(InetAddress.getByName("127.0.0.1").getHostAddress(), connect.serverSocketPort);
      toNodeAddress = new Address(s.getInetAddress().getHostAddress(), s.getPort());
      ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      oos.writeObject(new Connect(connect.serverSocketAddress, serverSocket.getLocalPort(), 2));
      s.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("toNode: " + toNodeAddress);
    System.out.println("fromNode: " + fromNodeAddress);
  }

  public static void main(String[] args) throws Exception {
    new Node(args);
  }
}
