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
  private boolean toNodeFailure = false;

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
            System.out.println("*** Connected " + serviceSocket.toString());
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    System.out.println("Listening for service requests...");
    while (true) {
      System.out.print("");
      if (toNodeFailure) {
        System.out.println("Sending repair");
        toNodeAddress = secondToNodeAddress;
        sendObject(toNodeAddress, new Repair(getThisServerSocketAddress()));
        toNodeFailure = false;
      }
      else if (serviceSocket != null) {
        System.out.println("*** Service request incoming");
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
    } else if (object instanceof Repair) {
      repair((Repair) object);
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
      if(!sendObject(toNodeAddress.ip, toNodeAddress.port, put)) {
        toNodeFailure = true;
        sendObject(secondToNodeAddress.ip, secondToNodeAddress.port, put);
      }
    }
  }

  private void get(Get get) {
    if(!get.isSigned()) {
      get.sign(getThisServerSocketAddress());
    }
    else System.out.println("Get signed by "+ get.getSignature());

    if (puts.containsKey(get.key)) {
      sendObject(get.ip, get.port, puts.get(get.key));
    }
    else {
      if(!get.getSignature().equals(toNodeAddress)) {
        if(!sendObject(toNodeAddress, get)) {
          toNodeFailure = true;
          if(!get.getSignature().equals(secondToNodeAddress)) {
            sendObject(secondToNodeAddress, get);
          } else {
            sendObject(get.ip, get.port, "No such put");
          }
        }
      } else {
        sendObject(get.ip, get.port, "No such put");
      }
    }
  }

  private void repair(Repair repair) {
    if (!repair.isComplete()) {
      repair.complete(getThisServerSocketAddress(), toNodeAddress);
    }
    if (repair.getCaller().equals(toNodeAddress)) {
      secondToNodeAddress = repair.getNextNode();
      System.out.println("Sending repair to caller");
      System.out.println("toNode: " + toNodeAddress);
      System.out.println("secondToNode: " + secondToNodeAddress);
    }
    if (repair.getCaller().equals(getThisServerSocketAddress())) {
      secondToNodeAddress = repair.getSecondNextNode();
      System.out.println("Finializing repair");
      System.out.println("toNode: " + toNodeAddress);
      System.out.println("secondToNode: " + secondToNodeAddress);
    } else {
      sendObject(toNodeAddress, repair);
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

  public boolean sendObject(Address to, Object object) {
    return sendObject(to.ip, to.port, object);
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

  public static void main(String[] args) throws Exception {
    new Node(args);
  }
}
