import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
        sendObject(toNodeAddress.ip, toNodeAddress.port, 
          new Connect(0, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort()));
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
      sendToPeer(put);
    }
  }

  private void get(Get get) {
    if(get.isSigned()) {
      System.out.println("Get signed by "+ get.getSignature());
      if(get.getSignature().equals(getThisServerSocketAddress())) {
        sendObject(get.ip, get.port, "No such put");
        return;
      }
    }
    else get.sign(getThisServerSocketAddress());

    if (puts.containsKey(get.key)) {
      sendObject(get.ip, get.port, puts.get(get.key));
    }
    else {
      sendToPeer(get);
    }
  }

  private void connect(Connect connect) throws IOException {
    System.out.println("Connect step " + connect.step);

    switch(connect.step) {
      case 0:
        sendObject(secondToNodeAddress.ip, secondToNodeAddress.port, 
          new Connect(2, connect.serverSocketAddress1, connect.serverSocketPort1));

        sendObject(toNodeAddress.ip, toNodeAddress.port, 
          new Connect(1, connect.serverSocketAddress1, connect.serverSocketPort1, secondToNodeAddress.ip, secondToNodeAddress.port));

        secondToNodeAddress = new Address(connect.serverSocketAddress1, connect.serverSocketPort1);

        break;

      case 1:
        toNodeAddress = new Address(connect.serverSocketAddress1, connect.serverSocketPort1);
        secondToNodeAddress = new Address(connect.serverSocketAddress2, connect.serverSocketPort2);

        break;

      case 2:
        sendObject(connect.serverSocketAddress1, connect.serverSocketPort1, 
          new Connect(1, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort(), toNodeAddress.ip, toNodeAddress.port));

        break;

    }
    System.out.println("toNode: " + toNodeAddress);
    System.out.println("secondToNode: " + secondToNodeAddress);
  }

  private void connectAsSingleNode(Connect connect) {
    System.out.println("Connecting as single node");
    try {
      secondToNodeAddress = getThisServerSocketAddress();
      toNodeAddress = new Address(serviceSocket.getInetAddress().getHostAddress(), connect.serverSocketPort1);
      sendObject(serviceSocket.getInetAddress().getHostAddress(), connect.serverSocketPort1, 
        new Connect(1, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort(), connect.serverSocketAddress1, connect.serverSocketPort1));  
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("toNode: " + toNodeAddress);
    System.out.println("secondToNode: " + secondToNodeAddress);
  }

  public Address getThisServerSocketAddress() {
    return new Address(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
  }

  public Socket openSocket(Address to) throws IOException, UnknownHostException {
    return openSocket(InetAddress.getByName(to.ip).getHostAddress(), to.port);
  }

  public Socket openSocket(String toIp, int toPort) throws IOException, UnknownHostException {
    return new Socket(InetAddress.getByName(toIp).getHostAddress(), toPort);
  }

  public void sendObject(Address to, Object object) {
    sendObject(to.ip, to.port, object);
  }

  public boolean sendObject(String ip, int port, Object object) {
    try {
      Socket s = openSocket(ip, port);
      new ObjectOutputStream(s.getOutputStream()).writeObject(object);
      s.close();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  private void sendToPeer(Object object) {
    if(!sendObject(toNodeAddress.ip, toNodeAddress.port, object)) {
      sendObject(secondToNodeAddress.ip, secondToNodeAddress.port, object);
    }
  }

  public static void main(String[] args) throws Exception {
    new Node(args);
  }
}
