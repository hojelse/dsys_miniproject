# Mini project 2

A system for communication.

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
> its not implemented yet xd
```
java Source.java
```

## Arguments for choice of UDP or TCP.

We use DatagramSocket so we use UDP

> TODO argue

## Are the processes Web Services?

> idk

## Is the system time and/or space de-coupled?

¯\_(ツ)_/¯

## Is the system a message queue?

Yes, the Service' buffer is a queue.

## Is the system a publish/subscribe system?

Kinda.. maybe..

## What are the failure modes of the system?

yikes idk
