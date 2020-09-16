1a. Implement an RFC862 server adhering to the RFC 862 specification. Use port 7007 instead of 7. You only need implement the UDP part ("UDP Based ...”).  (if you made a UDP echo server in the first hand-in you have already finished this point)

Submit your solution as a single Java-file RFC862.java.

1. Write a drop-in replacement for DatagramSocket which randomly either discards, duplicates, reorders or simply sends datagrams. Inherit from DatagramSocket.
When sending the datagrams "Hello" and "Goodbye" the following datagrams could be sent:
Discard: "Goodbye"  
Duplicate:  "Hello", "Hello", "Goodbye"
Reorder: "Goodbye", "Hello"
Send: "Hello", "Goodbye"

Test your implementation with a (modified) UDP-client.

(Hint, useful Java libraries: java.util.Random, java.net.DatagramSocket)

Submit your solution as a single Java-file QuestionableDatagramSocket.java.

2. Write programs to estimate UDP datagram loss.
Specifically program a client that will interact with your echo-server using your questionable datagramsocket.

Your programs must accept as input 

(a) datagram size
(b) number of datagrams to send 
(c) interval between transmissions.  (Use a timeout instead of threading your application)
The program must output 
(a) absolute number and percentage of lost datagrams and 
(b) absolute number and percentage of duplicated datagrams. 
(c) absolute number of reordered packets
It is acceptable if your estimate cannot distinguish between losing a single request, losing a single response, or losing both a request and a response.
Use your loss estimator to demonstrate:

Datagram loss on a local connection on a single machine (i.e., no physical net).
Datagram loss on Wifi. 
Datagram loss on ethernet.
Datagram loss on the Internet (i.e., transmitting across multiple physical nets). (only do this if you can actually do it, e.g have access to your routers port forwarding)
Indicate for each of these four cases the parameters (a-c above) you used to elicit the loss and the observed lossage (i-ii above). Explain where and why you expect the loss to be happening. 

Submit a single java source Estimator.java and a .txt file briefly summarising your findings.

3. Write programs to reliably communicate over UDP. 
Using only DatagramSocket, write programs A,B which together reliably transmits a string of length less than 255 characters. Program A accepts at startup a destination ip and port and a string, then transmits the string, somehow ensures that the string correctly arrived, says so and terminates. Program B repeatedly receives such strings and prints them. 
Your transmission mechanism must guarantee that for each invocation of Program A with string S, Program B prints S exactly once. 

Optional: Design the server, so that it can handle multiple concurrent clients.

Submit two java source files ReliableUDPClient.java and ReliableUDPServer.java

Rules.

You must submit in groups; your submission must be in the form of single .zip archive, in which you have allocated the specific files in folders named "1", "2" and so forth according to the tasks. Note that approval of your submission is a prerequisite for attending the examination. 

Your submission must take the form of a single zip-archive  named "group-X.zip" where X is your group number; the archive must contain a single directory "group-X" containing the above-mentioned source- and text-files. Submissions not on this form will be rejected without further review. 

Hints.

Design your solutions as a group, then split out to implement. Re-convene as a group when your design comes apart.
You can use threads but they might add complexity, the assignment is solvable without, if you do decide to use threads then make sure to document it well!
Google and Stackoverflow are your friends when you are wondering how to do something specific in Java, say, how to convert a string to a byte array. Check dates, though: Some popular answers on stackoverflow have been obsoleted by subsequent java releases.
If you have any question, reach out in the Discussion forum on Learnit or on Discord.
