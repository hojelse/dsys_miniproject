import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.AbstractMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;

public class Service {
  public static int inSocketPort;
  public static int outSocketPort;
  public static int subSocketPort;

  static DatagramSocket inSocket;
  static DatagramSocket outSocket;
  static DatagramSocket subSocket;

  static Set<Entry<String, Integer>> sinks = new HashSet<>();

  public static void main(String[] args) {
    if (args.length < 3) {

      System.out.println("");
      System.out.println("Required arguments:  inPort  outPort  subPort");
      System.out.println("");
      System.out.println("  inPort:  Port for ingoing messages");
      System.out.println("  outPort: Port for outgoing messages");
      System.out.println("  subPort: Port for incoming subscriptions");

      System.exit(0);
    } else {
      inSocketPort = Integer.parseInt(args[0]);
      outSocketPort = Integer.parseInt(args[1]);
      subSocketPort = Integer.parseInt(args[2]);
      try {
        inSocket = new DatagramSocket(inSocketPort);
        outSocket = new DatagramSocket(outSocketPort);
        subSocket = new DatagramSocket(subSocketPort);

        inSocket.setSoTimeout(500);
        subSocket.setSoTimeout(500);
      } catch (BindException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      new Thread(new SubscriptionsHandler()).start();

      System.out.println("Listening for messages from Sources...");
      do {
        try {
          DatagramPacket p = new DatagramPacket(new byte[1000], 1000);
          inSocket.receive(p);
          System.out.println("Received: " + new String(p.getData()));
          for (Entry<String, Integer> sink : sinks) {
            var ip = sink.getKey();
            var port = sink.getValue();
            p.setAddress(InetAddress.getByName(ip));
            p.setPort(port);
            outSocket.send(p);
          }
        } catch (SocketTimeoutException e) {
          continue;
        } catch (IOException e) {
          // fak
          e.printStackTrace();
        }
      } while (true);
    }
  }

  private static class SubscriptionsHandler implements Runnable {
    public void run() {
      System.out.println("Listening for Sink subscriptions...");
      while (true) {
        DatagramPacket p = new DatagramPacket(new byte[1000], 1000);
        try {
          subSocket.receive(p);
        } catch (SocketTimeoutException e) {
          continue;
        } catch (IOException e) {
          e.printStackTrace();
        }
        String ip = p.getAddress().toString();
        ip = ip.replace("/", "");
        int port = p.getPort();

        String message = new String(p.getData()).trim();
        if (message.equals("unsubscribe"))
          removeSubscription(ip, port);
        if (message.equals("subscribe"))
          addSubscription(ip, port);
      }
    }
  }

  private static void removeSubscription(String ip, int port) {
    for (Entry<String, Integer> entry : sinks) {
      if (entry.getKey().equals(ip) && entry.getValue() == port) {
        sinks.remove(entry);
        System.out.println("Remove " + ip + ":" + port + " as Sink");
        return;
      }
    }
    System.out.println("Remove Failed: The Sink " + ip + ":" + port + " was not found");
  }

  private static void addSubscription(String ip, int port) {
    sinks.add(new AbstractMap.SimpleEntry<String, Integer>(ip, port));
    System.out.println("Add " + ip + ":" + port + " as Sink");
  }
}
