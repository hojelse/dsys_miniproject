import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sink {
  static DatagramSocket socket;
  static int port;
  static int serviceSubscriptionPort;
  static String serviceSubscriptionIP;

  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("");
      System.out.println("Required arguments:  inPort  subIP  subPort");
      System.out.println("");
      System.out.println("  inPort: Ingoing port for this Sink");
      System.out.println("  subIP: The IP for the host Service");
      System.out.println("  subPort: The Port for the subscription socket on the host Service");
      System.exit(0);
    } else {
      port = Integer.parseInt(args[0]);
      serviceSubscriptionIP = args[1];
      serviceSubscriptionPort = Integer.parseInt(args[2]);

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Sink.unsubscribe();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }));

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
  }

  private static void receive(DatagramPacket packet) {
    try {
      socket.receive(packet);
      System.out.println("Received: " + new String(packet.getData()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void unsubscribe() throws IOException {
    subOrUnsub("unsubscribe");
  }

  private static void subscribe() throws IOException {
    subOrUnsub("subscribe");
  }

  private static void subOrUnsub(String message) throws IOException {
    DatagramPacket packet = new DatagramPacket(new byte[10], 10);
    byte[] bytes = message.getBytes();
    packet.setData(bytes);
    packet.setAddress(InetAddress.getByName(serviceSubscriptionIP));
    packet.setPort(serviceSubscriptionPort);
    socket.send(packet);
  }
}
