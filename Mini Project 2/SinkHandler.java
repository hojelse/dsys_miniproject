import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class SinkHandler extends Handler {

	@Override
	public void run() {
    try {
      InputStream is = getConnection().getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while (true) {
		  	System.out.println(br.readLine());
			}
    } catch (IOException ignored) {}
	}
}
