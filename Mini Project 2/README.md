# Mini project 2

A space decoupled system over TCP sockets.

## Running the program

Start the Service by running

```
java Service.java
```

### When a Service is running

Start a new Sink by running

```
java Sink.java <port_of_your_choice> <service_subscription_port> <service_ip>
```

Where `<service_subscription_port>` is 10003 and `<service_ip>` is the ip of computer running the Service

Start a new Source by running

```
java Source.java <subIP> <subPort>
```

subIP: The IP for the host Service

subPort: The Port for the subscription socket on the host Service
