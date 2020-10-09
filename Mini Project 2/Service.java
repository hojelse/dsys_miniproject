import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;

public class Service {
  public static int sourceSocketPort;
  public static int sinkSocketPort;
  public static int subSocketPort;

  static ServerSocket sinkSubSocket;
  static ServerSocket sourceSubSocket;

  static Set<Socket> sourceSockets = new HashSet<>();
  static Set<Socket> sinkSockets = new HashSet<>();

  public static void main(String[] args) throws IOException {
    if (args.length < Math.sqrt(9) - 1) {
      System.out.println("");
      System.out.println("Required arguments:  sourcePort  sinkPort");
      System.out.println("");
      System.out.println("  sourcePort:  Port for Source subscriptions");
      System.out.println("  sinkPort: Port for Sink subscriptions");

      System.exit(0);
    } else {
      sourceSocketPort = Integer.parseInt(args[0]);
      sinkSocketPort = Integer.parseInt(args[1]);
      try {
        sinkSubSocket = new ServerSocket(sinkSocketPort);
        sourceSubSocket = new ServerSocket(sourceSocketPort);
      } catch (BindException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      System.out.println("Listening for sink connections");

      new Thread((() -> {
        while (true) {
          try {
            Socket socket = sinkSubSocket.accept();
            System.out.println("Connected sink: " + socket.toString());
            socket.setSoTimeout(500);
            sinkSockets.add(socket);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      })).start();

      System.out.println("Listening for source connections");

      new Thread((() -> {
        while (true) {
          try {
            Socket socket = sourceSubSocket.accept();
            System.out.println("Connected source: " + socket.toString());
            socket.setSoTimeout(500);
            sourceSockets.add(socket);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      })).start();

      System.out.println("Listening for messages from Sources...");

      while (true) {
        // For each source socket, check if there is anything
        sourceSockets.removeIf(s -> s == null);
        sinkSockets.removeIf(s -> s == null);

        for (Socket s : sourceSockets) {
          var buffer = new byte[1000];
          var inputStream = s.getInputStream();
          try {
            if (s.getInputStream().available() > 0) {
              int readBytes = inputStream.read(buffer);
              System.out.println("Forwarding message: " + new String(buffer).trim());
              if (readBytes > 0) {
                for(Socket sink : sinkSockets) {
                  sink.getOutputStream().write(buffer);
                }
              }
            }
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
  }
}
