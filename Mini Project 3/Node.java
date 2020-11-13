import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Node {

  private Socket toNodeSocket = null;
  private Address toNodeAddress;

  private Socket secondToNodeSocket = null;
  private Address secondToNodeAddress;

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
    var endpoint = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), localPort);
    serverSocket.bind(endpoint);
    System.out.println("*** ServerSocket on " + serverSocket);

    if (ip != null && port != null) {
      //Connect to a Node
      try {
        toNodeAddress = new Address(ip, port);
        toNodeSocket = new Socket(InetAddress.getByName(toNodeAddress.ip).getHostAddress(), toNodeAddress.port);
        Connect connect = new Connect(0, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        var oos = new ObjectOutputStream(toNodeSocket.getOutputStream());
        oos.writeObject(connect);
        toNodeSocket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      //Connect to nothing
      try {
        toNodeAddress = null;
        secondToNodeAddress = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Close sockets on shutdown
    Runtime.getRuntime().addShutdownHook(
      new Thread(() -> {
        try {
          if (toNodeSocket != null) toNodeSocket.close();
          if (secondToNodeSocket != null) secondToNodeSocket.close();
          serverSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      })
    );

    // Listen for incoming connections
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
          handleServiceRequest(object);
        } catch (Exception e) {
          e.printStackTrace();
        }
        serviceSocket.close();
        serviceSocket = null;
      }
    }
  }

  private void handleServiceRequest(Object object) throws Exception {
    System.out.println("Service request: " + object);
    if (object instanceof Put) {
      put((Put) object);
    } else if (object instanceof Get) {
      get((Get) object);
    } else if (object instanceof Connect) {
      if (toNodeAddress == null && secondToNodeAddress == null) {
        connectAsSingleNode((Connect) object);
      } else {
        connect((Connect) object);
      }
    } else {
      throw new Exception("Unsupported service request");
    }
  }

  private void put(Put put) {
    puts.put(put.key, put);
    if(put.makeCopy()) {
      put.markAsCopied();
      try {
        toNodeSocket = new Socket(InetAddress.getByName(toNodeAddress.ip).getHostAddress(), toNodeAddress.port);
        ObjectOutputStream oos = new ObjectOutputStream(toNodeSocket.getOutputStream());
        oos.writeObject(put);
        toNodeSocket.close();
      } catch (ConnectException e) {
        try {
          secondToNodeSocket = new Socket(InetAddress.getByName(secondToNodeAddress.ip).getHostAddress(), secondToNodeAddress.port);
          ObjectOutputStream oos = new ObjectOutputStream(secondToNodeSocket.getOutputStream());
          oos.writeObject(put);
          secondToNodeSocket.close();
        } catch (Exception e2) {
          e2.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void get(Get get) {
    Object result = new Object();
    if (puts.containsKey(get.key)) {
      result = puts.get(get.key);
      try {
        Socket s = new Socket(InetAddress.getByName(get.ip).getHostAddress(), get.port);
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(result);
        s.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      try {
        toNodeSocket = new Socket(InetAddress.getByName(toNodeAddress.ip).getHostAddress(), toNodeAddress.port);
        ObjectOutputStream oos = new ObjectOutputStream(toNodeSocket.getOutputStream());
        oos.writeObject(get);
        toNodeSocket.close();
      } catch (ConnectException e) {
        try {
          secondToNodeSocket = new Socket(InetAddress.getByName(secondToNodeAddress.ip).getHostAddress(), secondToNodeAddress.port);
          ObjectOutputStream oos = new ObjectOutputStream(secondToNodeSocket.getOutputStream());
          oos.writeObject(get);
          secondToNodeSocket.close();
        } catch (Exception e2) {
          e2.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void connect(Connect connect) throws IOException {
    System.out.println("Connect step " + connect.step);

    ObjectOutputStream oos;

    switch(connect.step) {
      case 0:
        secondToNodeSocket = new Socket(InetAddress.getByName(secondToNodeAddress.ip).getHostAddress(), secondToNodeAddress.port);
        oos = new ObjectOutputStream(secondToNodeSocket.getOutputStream());
        oos.writeObject(new Connect(2, connect.serverSocketAddress1, connect.serverSocketPort1));
        toNodeSocket.close();

        toNodeSocket = new Socket(InetAddress.getByName(toNodeAddress.ip).getHostAddress(), toNodeAddress.port);
        oos = new ObjectOutputStream(toNodeSocket.getOutputStream());
        oos.writeObject(new Connect(1, connect.serverSocketAddress1, connect.serverSocketPort1, secondToNodeAddress.ip, secondToNodeAddress.port));
        toNodeSocket.close();

        secondToNodeAddress = new Address(connect.serverSocketAddress1, connect.serverSocketPort1);

        break;

      case 1:
        toNodeAddress = new Address(connect.serverSocketAddress1, connect.serverSocketPort1);
        secondToNodeAddress = new Address(connect.serverSocketAddress2, connect.serverSocketPort2);

        break;

      case 2: 
        Socket s = new Socket(connect.serverSocketAddress1, connect.serverSocketPort1);
        oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(new Connect(3, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort(), toNodeAddress.ip, toNodeAddress.port));
        s.close();

        break;

      case 3: 
        toNodeAddress = new Address(connect.serverSocketAddress1, connect.serverSocketPort1);
        secondToNodeAddress = new Address(connect.serverSocketAddress2, connect.serverSocketPort2);

        break;

    }
    System.out.println("toNode: " + toNodeAddress);
    System.out.println("secondToNode: " + secondToNodeAddress);
  }

  private void connectAsSingleNode(Connect connect) {
    System.out.println("Connecting as single node");
    try {
      secondToNodeAddress = new Address(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
      Socket s = new Socket(serviceSocket.getInetAddress().getHostAddress(), connect.serverSocketPort1);
      toNodeAddress = new Address(s.getInetAddress().getHostAddress(), s.getPort());
      ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      oos.writeObject(new Connect(3, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort(), connect.serverSocketAddress1, connect.serverSocketPort1));
      s.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("toNode: " + toNodeAddress);
    System.out.println("secondToNode: " + secondToNodeAddress);
  }

  public static void main(String[] args) throws Exception {
    new Node(args);
  }
}
