#Publishing Events with the Java client for Azure Event Hubs 

The vast majority of Event Hub applications using this and the other client libraries are and will be event publishers. 
And for most of these publishers, publishing events is extremely simple and handled with just a few API gestures.

##Getting Started

This library is available for use in Maven projects from the Maven Central Repository, and can be referenced using the
following dependency declaration inside of your Maven project file:    

```XML
    <dependency> 
   		<groupId>com.microsoft.azure</groupId> 
   		<artifactId>azure-eventhubs-clients</artifactId> 
   		<version>0.6.0</version> 
   	</dependency>   
 ```
 
 For different types of build environments, the latest released JAR files can also be [explicitly obtained from the 
 Maven Central Repository]() or from [the Release distribution point on GitHub]().  


For a simple event publisher, you'll need to import the *com.microsoft.azure.eventhubs* package for the Event Hub client classes
and the *com.microsoft.azure.servicebus* package for utility classes like common exceptions that are shared with the  
Azure Service Bus Messaging client. 
 
 
```Java
    import com.microsoft.azure.eventhubs.*;
    import com.microsoft.azure.servicebus.*;
```        

Using an Event Hub connection string, which holds all required connection information including an authorization key or token 
(see [Connection Strings](#connection-strings)), you then create an *EventHubClient* instance.   
   
```Java
    final String namespaceName = "----ServiceBusNamespaceName-----";
    final String eventHubName = "----EventHubName-----";
    final String sasKeyName = "-----SharedAccessSignatureKeyName-----";
    final String sasKey = "---SharedAccessSignatureKey----";
    ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
		
    EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
```

Once you have the client in hands, you can package any arbitrary payload as a plain array of bytes and send it. The samples 
we use to illustrate the functionality send a UTF-8 encoded JSON data, but you can transfer any format you wish. 

```Java
    EventData sendEvent = new EventData(payloadBytes);
    ehClient.send(sendEvent).get();
```
         
The entire client API is built for Java 8's concurrent task model, generally returning 
[*CompletableFuture<T>*](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html), so the 
*.get()* suffixing the operations in the snippets above just wait until the respective operation is complete.

##AMQP 1.0
Azure Event Hubs allows for publishing events using the HTTPS and AMQP 1.0 protocols. The Azure Event Hub endpoints
also support AMQP over the WebSocket protocol, allowing event traffic to leverage the same outbound TCP port as 
HTTPS. 

This client library is built on top of the [Apache Qpid Proton-J]() libraries and supports AMQP, which is significantly 
more efficient at publishing event streams than HTTPS. AMQP 1.0 is an international standard published as ISO/IEC 19464:2014.  

AMQP is session-oriented and sets up the required addressing information and authorization information just once for each 
send link, while HTTPS requires doing so with each sent message. AMQP also has a compact binary format to express common 
event properties, while HTTPS requires passing message metadata in a verbose text format. AMQP can also keep a significant 
number of events "in flight" with asynchronous and robust acknowledgement flow, while HTTPS enforces a strict request-reply 
pattern.

AMQP 1.0 is a TCP based protocol. For Azure Event Hubs, all traffic *must* be protected using TLS (SSL) and is using 
TCP port 5672.  

The current release of Proton-J does not yet support the WebSocket protocol. If your event publishers routinely operate 
in environments where tunneling over the HTTPS TCP port 443 is required due to Firewall policy reasons, please refer to
the section [Publishing via HTTPS](#Publishing via HTTPS) below for a temporary alternative.         

##Connection Strings

Azure Event Hubs and Azure Service Bus share a common format for connection strings. A connection string holds all required
information to set up a connection with an Event Hub. The format is a simple property/value list of the form 
{property}={value} with pairs separated by ampersands (&). 

| Property              |  Description                                               |
|-----------------------|------------------------------------------------------------| 
| Endpoint              | URI for the Event Hubs namespace. Typically has the form *sb://{namespace}.servicebus.windows.net/*   |
| EntityPath            | Relative path of the Event Hub in the namespace. Commonly this is just the Event Hub name                   |  
| SharedAccessKeyName   | Name of a Shared Access Signature rule configured for the Event Hub or the Event Hub name. For publishers, the rule must include "Send" permissions. |
| SharedAccessKey       | Base64-encoded value of the Shared Access Key for the rule |
| SharedAccessSignature | A previously issued Shared Access Signature token  (not yet supported; will be soon)        |
 
A connection string will therefore have the following form:

```
  Endpoint=sb://clemensveu.servicebus.windows.net&EntityPath=myeventhub&SharedAccessSignature=....
```

##Advanced Operations

The publisher example shown in the overview above sends an event into the Event Hub without further qualification. This is 
the preferred and most flexible and reliable option. For specific needs, Event Hubs offers two extra options to 
qualify send operations: Publisher policies and partion addressing.     

###Partition Addressing

Any Event Hub's event store is split up into at least 4 partitions, each maintaining a separate event log. You can think 
of partitions like lanes on a highway. The more events the Event Hub needs to handle, the more lanes (partitions) you have 
to add. Each partition can handle at most the equivalent of 1 "throughput unit", equivalent to at most 1000 events per 
second and at most 1 Megabyte per second.

In some cases, publisher applications need to address partitions directly in order to pre-categorize events for consumption.
A partition is directly addressed either by using the partition's identifier or by using some string (partition key) that gets 
consistently hashed to a particular partition.

This capability, paired with a large number of partitions, may appear attractive for implementing a fine grained, per publisher 
subscription scheme similar to what Topics offer in Service Bus Messaging - but it's not at all how the capability should be used
and it's likely not going to yield satisfying results. 
 
Partition addressing is designed as a routing capability that consistently assigns events from the same sources to the same partition allowing 
downstream consumer systems to be optimized, but under the assumption of very many of such sources (hundreds, thousands) share 
the same partition. If you need fine-grained content-based routing, Service Bus Topics might be the better option. 

####Using Partition Keys

Of the two addressing options, the preferable one is to let the hash algorithm map the event to the appropriate partition.
The gesture is a straightforward extra override to the send operation supplying the partition key: 

```Java
    EventData sendEvent = new EventData(payloadBytes);
>   ehClient.send(sendEvent, partitionKey).get();
```
     
####Using Partition Ids

If you indeed need to target a specific partition, for instance because you must use a particular distribution strategy, 
you can send directly to the partition, but doing so requires an extra gesture so that you don't accidentally choose this
option. To send to a partition you explicitly need to create a client object that is tued to the partition as shown below:

```Java
    EventHubClient ehClient = EventHubClient.createFromConnectionString(str).get();
>	EventHubSender sender = ehClient.createPartitionSender("0").get();
    EventData sendEvent = new EventData(payloadBytes);
    sender.send(sendEvent).get();
```

#### Publisher Policies

Event Hub Publisher Policies are not yet supported by this client and will be supported in a future release.
 
####Special considerations for partitions and publisher policies

Using partitions or publisher policies (which are effectively a special kind of partition key) may impact throughput 
and availability of your Event Hub solution. 

When you do a regular send operation that does not prescribe a particular partition, the Event Hub will choose a 
partition at random, ensuring about equal distribution of events across partitions. Sticking with the above analogy, 
all highway lanes get the same traffic. 

If you explicitly choose the partition key or partition-id, it's up to you to take care that traffic is evenly 
distributed, otherwise you may end up with a traffic jam (in the form of throttling) on one partition while there's 
little or no traffic on another partition. 

Also, like every other aspect of distributed systems, the log storage backing any partition may rarely and briefly slow 
down or experience congestion. If you leave choosing the target partition for an event to Event Hubs, it can flexibly
react to such availability blips for publishers.        

Generally, you should *not* use partitioning as a traffic prioritization scheme, and you should *not* use it 
for fine grained assignment of particular kinds of events to a particular partitions. Partitions are a load 
distribution mechanism, not a filtering model.
