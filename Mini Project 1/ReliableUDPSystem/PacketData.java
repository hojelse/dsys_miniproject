package ReliableUDPSystem;

import java.io.Serializable;

public class PacketData implements Serializable{
    static final long serialVersionUID = 1L;

    private PacketTypeEnum type;
    private int syn;
    private int syn_ack;
    private int ack;
    private int seq;
    private String data;

    public PacketData(PacketTypeEnum type){
        this.type = type;
    }

    public void setSyn(int seq){
        syn = seq;
    }
    public void setSynAck(int seq){
        syn_ack = seq;
    }
    public void setAck(int seq){
        ack = seq;
    }
    public void setSeq(int seq){
        this.seq = seq;
    }
    public void setData(String data){
        this.data = data;
    }

    public PacketTypeEnum getType(){
        return type;
    }
    public int getSyn(){
        return syn;
    }
    public int getSynAck(){
        return syn_ack;
    }
    public int getAck(){
        return ack;
    }
    public int getSeq(){
        return seq;
    }
    public String getData(){
        return data;
    }
}
