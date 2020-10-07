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
      inSocket.setSoTimeout(10000);

      outSocket = new DatagramSocket(outSocketPort);
      subSocket = new DatagramSocket(subscriptionPort);

      sinks.add(new AbstractMap.SimpleEntry<String, Integer>("10.26.8.25", 9101));
      sinks.add(new AbstractMap.SimpleEntry<String, Integer>("localhost", 9101));
      sinks.add(new AbstractMap.SimpleEntry<String, Integer>("localhost", 9000));

      subSocket.setSoTimeout(10000);
    } catch (BindException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    do {
      try {
        System.out.println("Waiting for source messages");
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
