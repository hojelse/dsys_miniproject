import java.io.Serializable;

public class Connect implements Serializable {
  /**
	 *
	 */
	private static final long serialVersionUID = 1L;
public final String from;
  public final String to;
  public final int step;

  public Connect(String from, String to, int step) {
    this.from = from;
    this.to = to;
    this.step = step;
  }
}
