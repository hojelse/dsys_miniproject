import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class Put implements Serializable{

    private static final long serialVersionUID = -1384716847830623082L;

    public final int key;
    public final String value;
    private boolean makeCopy;

    public Put(int key, String value) {
        this.key = key;
        this.value = value;
        makeCopy = true;
    }
    
    public static void main(String[] args) throws Exception {
        var ip = args[0];
        int port = Integer.parseInt(args[1]);
        int key = Integer.parseInt(args[2]);
        var value = args[3];

        Socket socket = new Socket(ip, port);
        System.out.println("Connected");

        Put put = new Put(key, value);

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(put);
        socket.close();
    }

    public void markAsCopied() {
        makeCopy = false;
    }

    public boolean makeCopy() {
        return makeCopy;
    }

    @Override
    public String toString() {
        return "Put(" + key + "," + value + ")";
    }
}