# Setup
Run  
```javac *.java```  
from the root directory to compile.

# To run Node.java
Run  
```java Node.java <localport> <IP> <port>```  
Where \<localport> is the local port that other nodes should use to connect to the node.  
\<IP> and \<port> are optional parameters, that define the IP and port of another node.  
To start a node without connecting to another node, you could for example run  
```java Node.java 1337```  
To start a node that connects to this node, you could then for example run  
```java Node.java 1338 10.26.55.65 1337```  

# To run Put.java
Run  
```java Put.java <IP> <port> <key> <value>```  
Where \<IP> and \<port> specify the node to put to.  
\<key> is an integer that specifies the key of the key-value pair,  
and \<value> is a string that is the value.  
You can for example run  
```java Put.java 10.26.55.65 1337 3 sample```

# To run Get.java
Run  
```java Get.java <IP> <port> <key>```  
Where \<IP> and \<port> specify a node to get from.  
\<key> specifies the key of the key-value pair that you want.  
If you wanted to get the value from the example that we used in the previous section, run  
```java Get.java 10.26.55.65 1337 3```  
Keep in mind that the IP and port just needs to point to a node that is connected to  
the same net work as the node with the value. You don't necessarily need to point to the exact node.
