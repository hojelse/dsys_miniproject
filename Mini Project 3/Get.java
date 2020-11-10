import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class Get implements Serializable {

    private static final long serialVersionUID = 4661606996498963783L;

    public final int key;
    public final String ip2;
    public final int port2;

    public Get(int key, String ip2, int port2) {
        this.key = key;
        this.ip2 = ip2;
        this.port2 = port2;
    }

    public static void main(String[] args) throws Exception {

        var ip = args[0];
        int port = Integer.parseInt(args[1]);
        int key = Integer.parseInt(args[2]);

        Socket socket = new Socket(ip, port);

        Get get = new Get(key, socket.getLocalAddress().toString(), socket.getLocalPort());

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(get);
        socket.close();
    }

    @Override
    public String toString() {
        return "Get(" + key + "," + ip2 + "," + port2 + ")";
    }

}
