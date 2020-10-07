import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

public class Service {
  public static int inSocketPort = 10001;
  public static int outSocketPort = 10002;
  public static int subscriptionPort = 10003;

  static DatagramSocket inSocket;
  static DatagramSocket outSocket;
  static DatagramSocket subSocket;

  static Set<Entry<String, Integer>> sinks = new HashSet<>();

  public static void main(String[] args) {
    try {
      inSocket = new DatagramSocket(inSocketPort);
      inSocket.setSoTimeout(500);

      outSocket = new DatagramSocket(outSocketPort);
      subSocket = new DatagramSocket(subscriptionPort);

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
