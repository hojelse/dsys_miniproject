import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Estimator {
  public static void runEstimator(int datagramSize, int numberOfDatagrams, int intervalBetweenTransmissionsInMs)
      throws IOException, InterruptedException {
    List<DatagramPacket> packets = new ArrayList<>();
    for (int i = 0; i < numberOfDatagrams; i++) {
      packets.add(new DatagramPacket(createBytesFromSize(datagramSize, i).toString().getBytes(), datagramSize,
          InetAddress.getByName("localhost"), 7007));
    }

    QuestionableDatagramSocket socket = new QuestionableDatagramSocket(1337);
    socket.setReceiveBufferSize(2 * datagramSize * numberOfDatagrams);
    socket.setSoTimeout(2000);

    for (DatagramPacket datagramPacket : packets) {
      socket.questionableSend(datagramPacket);
      TimeUnit.MILLISECONDS.sleep(intervalBetweenTransmissionsInMs);
    }

    List<DatagramPacket> receivedPackets = new ArrayList<>();

    while (true) {
      try {
        byte[] buffer = new byte[datagramSize];
        DatagramPacket p = new DatagramPacket(buffer, datagramSize);
        socket.receive(p);
        receivedPackets.add(p);
      } catch (IOException e) {
        break;
      }
    }
    socket.close();

    int errors = 0;
    int discards = 0;
    int duplicates = 0;
    int reorders = 0;

    int exp = Integer.parseInt(new String(packets.get(0).getData()));
    int curr;
    int bound = receivedPackets.size();
    for(int i = 0; i < bound; i++){
      curr = Integer.parseInt(new String(receivedPackets.get(i).getData()));
      if(curr != exp){
        errors++;
        if(curr > exp){ //DISCARD OR REORDER
          int next;
          if(i == bound-1){
            next = Integer.MAX_VALUE;
          } else {
            next = Integer.parseInt(new String(receivedPackets.get(i+1).getData()));
          }
          if(next < curr){ //REORDER
            reorders++;
            exp+=2;
            i++;
          } else { //DISCARD
            discards++;
            exp++;
            i--;
          }
        } else if(curr < exp){ //DUPLICATE
          duplicates++;
        }
      } else {
        exp++;
      }
    }

    float pctErrors = (errors / (packets.size() * 1f)) * 100;
    float pctDiscards = (discards / (packets.size() * 1f)) * 100;
    float pctDuplicats = (duplicates / (packets.size() * 1f)) * 100;
    float pctReorders = (reorders / (packets.size() * 1f)) * 100;

    System.out.println("Errors: " + errors + " ["+ pctErrors +"%]");
    System.out.println("Discards: " + discards + " ["+ pctDiscards +"%]");
    System.out.println("Duplicates: " + duplicates + " ["+ pctDuplicats +"%]");
    System.out.println("Reorders: " + reorders + " ["+ pctReorders +"%]");
  }

  private static StringBuffer createBytesFromSize(int size, int packetNumber) {
    StringBuffer buf = new StringBuffer(size);
    while (buf.length() < size) {
      int message = (int) Math.pow(10, size) / 10 + packetNumber;
      buf.append(message);
    }
    buf.trimToSize();
    return buf;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Estimator.runEstimator(4, 1000, 5);
  }
}
