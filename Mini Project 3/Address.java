import java.net.Socket;

public class Address {
  final String ip;
  final int port;

  public Address(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  @Override
  public String toString() {
    return ip + ":" + port;
  }

  public static Address fromString(String address) {
    var parts = address.split(":");
    return new Address(parts[0], Integer.parseInt(parts[1]));
  }

  public static Address fromSocket(Socket s) {
    return new Address(s.getInetAddress().getHostAddress(), s.getPort());
  }

  public String getIP() {
    return ip;
  }

  public int getPort() {
    return port;
  }
}
