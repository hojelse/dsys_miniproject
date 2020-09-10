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
          InetAddress.getByName("10.26.8.199"), 7007));
    }

    QuestionableDatagramSocket socket = new QuestionableDatagramSocket(1337);
    socket.setReceiveBufferSize(2 * datagramSize * numberOfDatagrams);
    socket.setSoTimeout(2000);

    for (DatagramPacket datagramPacket : packets) {
      socket.questionableSend(datagramPacket);
      TimeUnit.MILLISECONDS.sleep(intervalBetweenTransmissionsInMs);
    }

    int numberOfLostDatagrams = 0;
    double percentageOfLostDatagrams = 0;
    int numberOfDuplicatedDatagrams = 0;
    double percentageOfDuplicatedDatagrams = 0;
    double numberOfReorderedPackets = 0;

    List<DatagramPacket> receivedPackets = new ArrayList<>();

    boolean skip = false;
    while (true) {
      try {
        byte[] buffer = new byte[datagramSize];
        DatagramPacket p = new DatagramPacket(buffer, datagramSize);

        skip = !skip;
        if (skip) {
          socket.receive(p);
          continue;
        }

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
    int offset = 0;
    int bound = Math.max(receivedPackets.size(), packets.size()) - 1;
    for (int i = 0; i < bound; i++) {
      int currPacket = Integer.parseInt(new String(packets.get(i).getData()));
      int currReceivedPacket = Integer.parseInt(new String(receivedPackets.get(i + offset).getData()));
      System.out.println(currPacket + " : " + currReceivedPacket);
      if (currPacket != currReceivedPacket) {
        if (currPacket < currReceivedPacket) {
          discards++;
          offset--;
        } else if (currPacket > currReceivedPacket) {
          duplicates++;
          offset++;
        }
        errors++;
      }
    }

    numberOfLostDatagrams = numberOfDatagrams - receivedPackets.size();
    percentageOfLostDatagrams = numberOfLostDatagrams / (numberOfDatagrams * 1d) * 100;

    System.out.println("Errors: " + errors);
    System.out.println("Discards: " + discards);
    System.out.println("Duplicates: " + duplicates);
    System.out.println("numberOfLostDatagrams: " + numberOfLostDatagrams);
    System.out.println("percentageOfLostDatagrams: " + percentageOfLostDatagrams + "%");
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
