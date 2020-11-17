import java.io.Serializable;

public class Connect implements Serializable {

  private static final long serialVersionUID = 1L;
  public final String serverSocketAddress1;
  public final int serverSocketPort1;
  public final String serverSocketAddress2;
  public final int serverSocketPort2;
  public final int step;

  public Connect(int step, String serverSocketAddress1, int serverSocketPort1, String serverSocketAddress2, int serverSocketPort2) {
    this.serverSocketAddress1 = serverSocketAddress1;
    this.serverSocketPort1 = serverSocketPort1;
    this.serverSocketAddress2 = serverSocketAddress2;
    this.serverSocketPort2 = serverSocketPort2;
    this.step = step;
  }

  public Connect(int step, String serverSocketAddress1, int serverSocketPort1) {
    this.serverSocketAddress1 = serverSocketAddress1;
    this.serverSocketPort1 = serverSocketPort1;
    this.serverSocketAddress2 = null;
    this.serverSocketPort2 = -1;
    this.step = step;
  }
}
