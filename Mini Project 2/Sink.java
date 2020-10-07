import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Sink {
  static DatagramSocket socket;
  static int port;
  static int serviceSubscriptionPort;
  static String serviceSubscriptionIp;

  public static void main(String[] args) {
    port = Integer.parseInt(args[0]);
    serviceSubscriptionPort = Integer.parseInt(args[1]);
    serviceSubscriptionIp = args[2];

    try {
      socket = new DatagramSocket(port);
      subscribe();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Subscribed to Service.");
    System.out.println("Listening for messages from Service...");

    DatagramPacket packet = new DatagramPacket(new byte[1000], 1000);
    while (true)
      receive(packet);
  }

  private static void receive(DatagramPacket packet) {
    try {
      socket.receive(packet);
      System.out.println("Received: " + new String(packet.getData()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void subscribe() throws IOException {
    DatagramPacket packet = new DatagramPacket(new byte[10], 10);
    packet.setAddress(InetAddress.getByName(serviceSubscriptionIp));
    packet.setPort(serviceSubscriptionPort);
    socket.send(packet);
  }
}
