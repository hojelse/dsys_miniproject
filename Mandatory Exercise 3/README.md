# Kafka setup

### Prerequisites
On linux, install java and download + extract kafka
```
sudo apt update
sudo apt install default-jdk

curl https://mirrors.dotsrc.org/apache/kafka/2.6.0/kafka_2.13-2.6.0.tgz kafka.tgz
tar -xzf kafka.tgz

cd kafka_2.13-2.6.0
```
   
### Terminal 1 - zookeeper
```
bin/zookeeper-server-start.sh config/zookeeper.properties
```

### Terminal 2 - server 2
```
bin/kafka-server-start.sh config/server.properties
```

### Terminal 3 - server 2
Copy server.properties to server2.properties and change `broker.id=1` , `log.dirs=/tmp/kafka-logs-2` and create new field `port = 9093`
```
bin/kafka-server-start.sh config/server2.properties
```

### Terminal 4 - Create topic and run consumer
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic my-topic
bin/kafka-topics.sh --list --zookeeper localhost:2181
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my-topic
```

### Terminal 5 - Producer 1
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic my-topic
```

### Terminal 6 - Producer 2
```
bin/kafka-console-producer.sh --broker-list localhost:9093 --topic my-topic
```

### Console 4, 5 and 6
![](https://i.gyazo.com/3acba2792d6b913e17f3f7ee50dc2821.png)
