import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class Get implements Serializable {

    private static final long serialVersionUID = 4661606996498963783L;

    public final int key;
    public final String ip;
    public final int port;

    public Object result;

    public Get(int key, String ip, int port) throws Exception {
        this.key = key;
        this.ip = ip;
        this.port = port;
        
    }

    public static void main(String[] args) throws Exception {

        var ip = args[0];
        int port = Integer.parseInt(args[1]);
        int key = Integer.parseInt(args[2]);
        ServerSocket origin = new ServerSocket();
        var endpoint = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), 0);
        origin.bind(endpoint);

        Socket socket = new Socket(ip, port);

        Get get = new Get(key, origin.getInetAddress().getHostAddress(), origin.getLocalPort());

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(get);
        
        Socket s = origin.accept();
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        get.result = ois.readObject();
        System.out.println(get.result);
        socket.close();
        origin.close();
    }

    public Object result() {
        return result;
    }

    @Override
    public String toString() {
        return "Get(" + key + "," + ip + "," + port + ")";
    }

}
