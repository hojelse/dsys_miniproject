import java.io.IOException;
import java.net.Socket;

public class Sink {
  static Socket socket;
  static int serviceSubscriptionPort;
  static String serviceSubscriptionIP;

  public static final char HEADER_COMMAND = 'c';
  public static final char HEADER_MESSAGE = 'm';
  public static final char CLOSE_COMMAND = 'q';

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("");
      System.out.println("Required arguments:  subIP  subPort");
      System.out.println("");
      System.out.println("  subIP: The IP for the host Service");
      System.out.println("  subPort: The Port for the subscription socket on the host Service");
      return;
    }

    serviceSubscriptionIP = args[0];
    serviceSubscriptionPort = Integer.parseInt(args[1]);

    Runtime.getRuntime().addShutdownHook((new Thread(() -> {
      try {
        var shutdown = (HEADER_COMMAND+""+CLOSE_COMMAND).getBytes();
        socket.getOutputStream().write(shutdown);
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    })));

    try {
      socket = new Socket(serviceSubscriptionIP, serviceSubscriptionPort);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    System.out.println("Listening for messages from Service...");
    System.out.println("Subscribed to Service.");

    while (true) {
      var buffer = new byte[1000];
      var inputStream = socket.getInputStream();

      if (inputStream.available() > 0) {
        var readBytes = inputStream.read(buffer);
        if (readBytes > 0)
          System.out.println(new String(buffer).trim());
      }
    }
  }
}
