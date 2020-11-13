import java.io.Serializable;

public class Address implements Serializable {

    private static final long serialVersionUID = 4661606992498963783L;

    public final String ip;
    public final int port;

    public Address(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString(){
        return ip + ":" + port;
    }

    public boolean equals(Address other) {
        if(ip.equals(other.ip) && port == other.port) return true;
        else return false;
    }
}
