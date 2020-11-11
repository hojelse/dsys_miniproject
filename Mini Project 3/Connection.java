import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {
  private String ip;
  private int port;
  private Socket socket;

  public Connection(String ip, int port, Socket socket) {
    this.ip = ip;
    this.port = port;
    this.socket = socket;
  }

  public Connection(String ip, int port) throws UnknownHostException, IOException {
    this(ip, port, new Socket(ip, port));
  }

  @Override
  public String toString() {
    return ip + ":" + port;
  }

  public static Connection fromString(String address) throws NumberFormatException, UnknownHostException, IOException {
    var parts = address.split(":");
    return new Connection(parts[0], Integer.parseInt(parts[1]));
  }

  public String getIP() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  public Socket getSocket() {
    return socket;
  }

  public void update(String ip, int port) throws IOException {
    socket = new Socket(ip, port);
    update(ip, port, socket);
  }

  public void update(String ip, int port, Socket socket) throws IOException {
    this.socket.close();
    this.ip = ip;
    this.port = port;
    this.socket = socket;
  }
}
