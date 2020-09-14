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

    // Generate packets
    List<DatagramPacket> packets = new ArrayList<>();
    for (int i = 0; i < numberOfDatagrams; i++) {
      packets.add(new DatagramPacket(createBytesFromSize(datagramSize, i).toString().getBytes(), datagramSize,
          InetAddress.getByName("10.26.55.230"), 7007));
    }

    // Setup socket
    DatagramSocket socket = new DatagramSocket(1337);
    socket.setReceiveBufferSize(2 * datagramSize * numberOfDatagrams);
    socket.setSoTimeout(2000);

    // Send packets
    for (DatagramPacket datagramPacket : packets) {
      socket.send(datagramPacket);
      TimeUnit.MILLISECONDS.sleep(intervalBetweenTransmissionsInMs);
    }

    // Handle receive packets
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

    // Setup error detection
    int errors = 0;
    int discards = 0;
    int duplicates = 0;
    int reorders = 0;

    // Detect/count errors
    int[] parsedReceivedPackets = new int[receivedPackets.size()];
    for (int i = 0; i < receivedPackets.size(); i++) {
      int parsedData = Integer.parseInt(new String(receivedPackets.get(i).getData()));
      parsedReceivedPackets[i] = parsedData;
    }

    // Selection sort with error detection
    int[] a = parsedReceivedPackets;

    int n = a.length;
    for (int i = 0; i < n; i++) {
      int min = i;
      for (int j = i + 1; j < n; j++) {
        if (a[j] <= a[min]) {
          min = j;
        }
      }

      // Tally Errors
      if (a[i] == a[min] && i != min)
        duplicates++;

      if (a[i] > a[min]) {
        reorders++;
        System.out.println("reorder: " + i);
      }

      exch(a, i, min);
    }

    discards = packets.size() - (n - duplicates);
    errors = discards + reorders + duplicates;

    // Calculate and print statistics
    float pctErrors = (errors / (packets.size() * 1f)) * 100;
    float pctDiscards = (discards / (packets.size() * 1f)) * 100;
    float pctDuplicats = (duplicates / (packets.size() * 1f)) * 100;

    System.out.println("======= Estimated error tally =======");
    // System.out.println("Errors: " + errors + " [" + pctErrors + "%]");
    System.out.println("Discards: " + discards + " [" + pctDiscards + "%]");
    System.out.println("Duplicates: " + duplicates + " [" + pctDuplicats + "%]");
    System.out.println("Reorders: " + reorders);

    // socket.printStats();
  }

  private static void exch(int[] a, int i, int j) {
    int swap = a[i];
    a[i] = a[j];
    a[j] = swap;
  }

  private static StringBuffer createBytesFromSize(int size, int packetNumber) {
    StringBuffer buf = new StringBuffer(size);
    while (buf.length() < size) {
      String format = "%0" + size + "d";
      String message = String.format(format, packetNumber);
      buf.append(message);
    }
    buf.trimToSize();
    return buf;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Estimator.runEstimator(10, 10000, 1);
  }
}
