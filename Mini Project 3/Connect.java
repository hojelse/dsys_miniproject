import java.io.Serializable;

public class Connect implements Serializable {
  /**
	 *
	 */
  private static final long serialVersionUID = 1L;
  public final String serverSocketAddress;
  public final int serverSocketPort;
  public final int step;

  public Connect(String serverSocketAddress, int serverSocketPort, int step) {
    this.serverSocketAddress = serverSocketAddress;
    this.serverSocketPort = serverSocketPort;
    this.step = step;
  }
}
