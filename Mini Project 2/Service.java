import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Service {
  public static int sourceSocketPort;
  public static int sinkSocketPort;
  public static int subSocketPort;

  static ServerSocket sourceSubSocket;
  static ServerSocket sinkSubSocket;

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
        sourceSubSocket = new ServerSocket(sourceSocketPort);
        sinkSubSocket = new ServerSocket(sinkSocketPort);
      } catch (BindException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      System.out.println("Listening for source connections");

      new Thread((() -> {
        while (true) {
          try {
            Socket socket = sourceSubSocket.accept();
            System.out.println("Connected source: " + socket.toString());
            synchronized (sourceSockets) {
              sourceSockets.add(socket);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      })).start();

      System.out.println("Listening for messages from Sources...");

      new Thread((() -> {
        while (true) {
          try {
            Socket socket = sinkSubSocket.accept();
            System.out.println("Connected sink: " + socket.toString());
            synchronized (sourceSockets) {
              sinkSockets.add(socket);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      })).start();

      System.out.println("Listening for sink connections");

      while (true) {
        List<Socket> socketsToRemove = new ArrayList<>();

        synchronized (sourceSockets) {
          for (Socket socket : sourceSockets) { // sinkSockets
            handleSocket(socket, socketsToRemove, false);
          }
        }

        synchronized (sinkSockets) {
          for (Socket socket : sinkSockets) { // sinkSockets
            handleSocket(socket, socketsToRemove, true);
          }
        }

        for (Socket socket : socketsToRemove) {
          // 1. Call shutdown
          socket.shutdownInput();
          socket.shutdownOutput();

          // 2. Continue to read until read throws IOException (since read throws IOException if socket is closed)
          while (true) {
            try {
              socket.getInputStream().read();
            } catch (IOException exception) {
              break;
            }
          }
          // 3. Now we can safely close the socket
          socket.close();
          sinkSockets.remove(socket);
          sourceSockets.remove(socket);
          System.out.println("Disconneted " + socket.toString());
        }
      }
    }
  }

  public static void handleSocket(Socket socket, List<Socket> socketsToRemove, boolean isSink) {
    var buffer = new byte[1000];
    InputStream inputStream;
    try {
      inputStream = socket.getInputStream();
      if (socket.getInputStream().available() > 0) {
        int readBytes = inputStream.read(buffer);

        if (readBytes > 0) {
          for (Socket sink : sinkSockets) {
              var os = sink.getOutputStream();
              os.write(buffer);
          }
        }
      }
    } catch (IOException err) {
      // sink disconnected
      socketsToRemove.add(socket);
    }
  }
}

