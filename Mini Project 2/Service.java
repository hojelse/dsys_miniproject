import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

public class Service {
  static int inSocketPort = 10001;
  static int outSocketPort = 10002;
  static int subscriptionPort = 10003;

  static DatagramSocket inSocket;
  static DatagramSocket outSocket;
  static DatagramSocket subSocket;

  static Set<Entry<String, Integer>> sinks = new HashSet<>();

  public static void main(String[] args) {
    try {
      inSocket = new DatagramSocket(inSocketPort);
      inSocket.setSoTimeout(10);

      outSocket = new DatagramSocket(outSocketPort);
      subSocket = new DatagramSocket(subscriptionPort);

      sinks.add(new AbstractMap.SimpleEntry<String, Integer>("10.26.8.25", 9101));
      sinks.add(new AbstractMap.SimpleEntry<String, Integer>("localhost", 9101));

      subSocket.setSoTimeout(10);
    } catch (BindException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    do {
      try {
        DatagramPacket p = new DatagramPacket(new byte[1000], 1000);
        inSocket.receive(p);
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

class Source {
  public static void main(String[] args) {

  }
}

