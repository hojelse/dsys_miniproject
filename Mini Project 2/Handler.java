import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

abstract public class Handler implements Runnable {
  private Socket connection = null;

  public void setConnection(Socket connection) {
    this.connection = connection;
  }

  public Socket getConnection() {
    return connection;
  }
}
