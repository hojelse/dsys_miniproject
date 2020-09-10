import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Estimator {
  public static void runEstimator(
    int datagramSize,
    int numberOfDatagrams,
    int intervalBetweenTransmissionsInMs
  ) throws IOException, InterruptedException {
    DatagramPacket[] packets = new DatagramPacket[numberOfDatagrams];
    for (int i = 0; i < numberOfDatagrams; i++) {
      packets[i] = new DatagramPacket(
        createBytesFromSize(datagramSize).toString().getBytes(),
        datagramSize,
        InetAddress.getByName("10.26.8.199"), 7007
      );
    }

    DatagramSocket socket = new DatagramSocket(1337);
    socket.setReceiveBufferSize(2 * datagramSize * numberOfDatagrams);

    for (DatagramPacket datagramPacket : packets) {
      socket.send(datagramPacket);
      TimeUnit.MILLISECONDS.sleep(intervalBetweenTransmissionsInMs);
    }

    int numberOfLostDatagrams = 0;
    double percentageOfLostDatagrams = 0;
    int numberOfDuplicatedDatagrams = 0;
    double percentageOfDuplicatedDatagrams = 0;
    double numberOfReorderedPackets = 0;

    List<DatagramPacket> receivedPackets = new ArrayList<>();

    long startTime = System.currentTimeMillis();

    long currentTime = System.currentTimeMillis();
    while (currentTime - startTime < 2 * 1000) {
      byte[] buffer = new byte[datagramSize];
      DatagramPacket p = new DatagramPacket(buffer, datagramSize);
      socket.receive(p);
      receivedPackets.add(p);
      currentTime = System.currentTimeMillis();
    }


    numberOfLostDatagrams = numberOfDatagrams - receivedPackets.size();
    percentageOfLostDatagrams = numberOfLostDatagrams / numberOfDatagrams * 100;

    socket.close();

    System.out.println("numberOfLostDatagrams: " + numberOfLostDatagrams);
    System.out.println("percentageOfLostDatagrams: " + percentageOfLostDatagrams + "%");
    System.out.println("numberOfDuplicatedDatagrams: " + numberOfDuplicatedDatagrams);
    System.out.println("percentageOfDuplicatedDatagrams: " + percentageOfDuplicatedDatagrams);
    System.out.println("numberOfReorderedPackets: " + numberOfReorderedPackets);
  }

  private static StringBuffer createBytesFromSize(int size) {
    StringBuffer buf = new StringBuffer(size);
    while (buf.length() < size) {
      buf.append('A');
    }
    buf.trimToSize();
    return buf;
  }
  public static void main(String[] args) throws IOException, InterruptedException {
     Estimator.runEstimator(
       1000,
       5,
       5
     );
  }
}
