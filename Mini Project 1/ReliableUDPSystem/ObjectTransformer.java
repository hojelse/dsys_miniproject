package ReliableUDPSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectTransformer {
    public byte[] messageToBytes(PacketData message){
        byte[] res = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(message);
            out.flush();
            res = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public PacketData bytesToMessage(byte[] bytes){
        Object o = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            o = in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (PacketData) o;
    }
}
