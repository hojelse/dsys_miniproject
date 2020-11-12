import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class Get implements Serializable {

    private static final long serialVersionUID = 4661606996498963783L;

    public final int key;
    public final String ip;
    public final int port;

    public Object result;

    public Get(int key, String ip, int port) {
        this.key = key;
        this.ip = ip;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {

        var ip = args[0];
        int port = Integer.parseInt(args[1]);
        int key = Integer.parseInt(args[2]);

        Socket socket = new Socket(ip, port);

        Get get = new Get(key, socket.getLocalAddress().toString(), socket.getLocalPort());

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(get);
        
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        get.result = ois.readObject();
        System.out.println(get.result);
        socket.close();
    }

    public Object result() {
        return result;
    }

    @Override
    public String toString() {
        return "Get(" + key + "," + ip + "," + port + ")";
    }

}
