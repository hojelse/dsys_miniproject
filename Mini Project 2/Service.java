import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Service {
  public static final String SUBSCRIPTION_ADDRESS = "localhost";
  public static final int SOURCE_SUBSCRIPTION_PORT = 2020;
  public static final int SINK_SUBSCRIPTION_PORT = 4040;

  public static void main(String[] args) throws IOException {
    Service sv = new Service();

    // Listen for subscriptions

    ServerSocket sourceSubscriptionSocket = new ServerSocket(SOURCE_SUBSCRIPTION_PORT);
    ServerSocket sinkSubscriptionSocket = new ServerSocket(SINK_SUBSCRIPTION_PORT);

    sv.launchListenerThread(sourceSubscriptionSocket, new SourceHandler());
    sv.launchListenerThread(sinkSubscriptionSocket, new SinkHandler());

    // -> notify
    sv.subscribe(new Sink());
    sv.subscribe(new Sink());


    // Emulation of Sources
    Scanner sc = new Scanner(System.in);
    while(sc.hasNextLine()) sv.notifySubscribers(sc.nextLine());
    sc.close();
  }

  public void launchListenerThread(ServerSocket serverSocket, Handler handler) {
    new Thread(new Runnable(){
      @Override
      public void run() {
        while (true) {

          System.out.println("SERVER: Listening for incoming connections...");
          try {
            Socket connection = serverSocket.accept(); // waits here until a client connects
            handler.setConnection(connection);
          } catch (IOException ignore) {}

          new Thread(handler).start();
        }
      }

    });
  }

  public void notifySubscribers(String message) {
    for (Sink sink : subscribers) {
      sink.print(message);
    }
  }

  public void subscribe(Sink sink) {
    subscribers.add(sink);
  }

  public void add(Source source) {

  }
}
