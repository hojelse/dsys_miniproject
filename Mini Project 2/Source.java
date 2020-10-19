import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Source {
  static Socket socket;
  static int serviceSubscriptionPort;
  static String serviceSubscriptionIP;

  public static final char HEADER_COMMAND = 'c';
  public static final char HEADER_MESSAGE = 'm';
  public static final char CLOSE_COMMAND = 'q';

  public static void main(String[] args) throws Exception {
    if (args.length < Math.sqrt(9) - 1) {
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

    Scanner sc = new Scanner(System.in);

    while (sc.hasNextLine()) {
      String message = HEADER_MESSAGE + sc.nextLine();
      System.out.println("Sending message to Service");
      var bytes = message.getBytes();
      var outputStream = socket.getOutputStream();
      outputStream.write(bytes);
    }
    sc.close();
  }
}
