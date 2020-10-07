import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Sink {
  static DatagramSocket s;
  public static void main(String[] args) throws IOException {
    s = new DatagramSocket(Integer.parseInt(args[0]));
    var p = new DatagramPacket(new byte[1000], 1000);
    while (true) {
      s.receive(p);
      System.out.println(new String(p.getData()));
    }
  }
}
