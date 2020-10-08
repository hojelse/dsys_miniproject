import java.net.*;
import java.io.*;

public class IP {
  public static void main(String[] args) throws Exception {
    final DatagramSocket socket = new DatagramSocket();
    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
    var ip = socket.getLocalAddress().getHostAddress();
    socket.close();
    System.out.println("Local IP: " + ip);

    URL whatismyip = new URL("http://checkip.amazonaws.com");
    BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

    ip = in.readLine();
    System.out.println("Public IP: " + ip);
  }
}
