import java.io.Serializable;

public class Repair implements Serializable{
    
    private static final long serialVersionUID = -1384716847830623082L;

    private Address caller;
    private Address nextNode;
    private Address secondNextNode;
    private boolean isComplete;

    public Repair(Address caller) {
        this.caller = caller;
        isComplete = false;
    }

    public void complete(Address nextNode, Address secondNextNode) {
        this.nextNode = nextNode;
        this.secondNextNode = secondNextNode;
        isComplete = true;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public Address getCaller() {
        return caller;
    }

    public Address getNextNode() {
        return nextNode;
    }

    public Address getSecondNextNode() {
        return secondNextNode;
    }
}
