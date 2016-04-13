#Consuming Events with the Java client for Azure Event Hubs 

Consuming events from Event Hubs is different from typical messaging infrastuctures like queues or topic 
subscriptions where a consumer simply fetches the "next" message.

Event Hubs puts the consumer in control of the offset from which the log shall be read, 
and the consumer can repeatedly pick a different or the same offset and read the event stream from chosen offsets while 
the events are being retained.  Each partition is therefore loosely analogous to a tape drive that you can wind back to a 
particular mark and then play back to the freshest data available. 

Azure Event Hubs consumers need to be aware of the partitioning model chosen for an Event Hub as receivers explicitly 
interact with partitions. Any Event Hub's event store is split up into at least 4 partitions, each maintaining a separate event log. You can think 
of partitions like lanes on a highway. The more events the Event Hub needs to handle, the more lanes (partitions) you have 
to add. Each partition can handle at most the equivalent of 1 "throughput unit", equivalent to at most 1000 events per 
second and at most 1 Megabyte per second.

The common consumption model for Event Hubs is that multiple consumers (threads, processes, compute nodes) process events 
from a single Event Hub in parallel, and coordinate which consumer is responsible for pulling events from which partition.  

> An upcoming update for this client will also bring the popular and powerful "event processor host" from C# to Java. 
> The event processor host dramatically simplifies writing high-scale, high-throughput event consumer applications 
> that distribute the processing load over a dynamic cluster of machines. 
   
##Getting Started

This library is available for use in Maven projects from the Maven Central Repository, and can be referenced using the
following dependency declaration inside of your Maven project file:    

```XML
    <dependency> 
   		<groupId>com.microsoft.azure</groupId> 
   		<artifactId>azure-eventhubs-clients</artifactId> 
   		<version>0.6.6</version> 
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

The receiver code creates an *EventHubClient* from a given connecting string
      
```Java
    final String namespaceName = "----ServiceBusNamespaceName-----";
    final String eventHubName = "----EventHubName-----";
    final String sasKeyName = "-----SharedAccessSignatureKeyName-----";
    final String sasKey = "---SharedAccessSignatureKey----";
    ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
		
    EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
```           

The receiver code then creates (at least) one *PartitionReceiver* that will receive the data. The receiver is seeded with 
an offset, in the snippet below it's simply the start of the log.    
		
```Java
		String partitionId = "0"; // API to get PartitionIds will be released as part of V0.2
		PartitionReceiver receiver = ehClient.createReceiver(
				EventHubClient.DefaultConsumerGroupName, 
				partitionId, 
				PartitionReceiver.StartOfStream,
				false).get();
``` 

Once the receiver is initialized, getting events is just a matter of calling the *receive()* method in a loop. Each call 
to *receive()* will fetch an eneumerable batch of events to process.    		
        
```Java        
		Iterable<EventData> receivedEvents = receiver.receive().get();         
```

##Consumer Groups 

Event Hub receivers always receive via Consumer Groups. A consumer group is a named entity on an Event Hub that is
conceptually similar to a Messaging Topic subscription, even though it provides no server-side filtering capabilities.

Each Event Hub has a "default consumer group" that is created with the Event Hub, which is also the one used in 
the samples. 

The primary function of consumers groups is to provide a shared coordination context for multiple concurrent consumers 
processing the same event stream in parallel. There can be at most 5 concurrent readers on a partition per consumer group; 
it is however *recommended* that there is only one active receiver on a partition per consumer group. The [Ownership, Failover, 
and Epochs](#ownership-failover-and-epochs) section below explains how to ensure this.  

You can create up to 20 such consumer groups on an Event Hub via the Azure portal or the HTTP API.

##Using Offsets

Each Event Hub has a configurable event retention period, which defaults to one day and can be extended to seven days. 
By contacting Microsoft product support you can ask for further extend the retention period to up to 30 days.

There are three options for a consumer to pick at which point into the retained event stream it wants to 
begin receiving events:

1. **Start of stream** Receive from the start of the retained stream, as shown in the example above. This option will start 
   with the oldest available retained event in the partition and then continously deliver events until all available events 
   have been read. 
2. **Time offset**. This option will start with the oldest event in the partition that has been received into the Event Hub 
   after the given instant.
   
   ``` Java
   PartitionReceiver receiver = ehClient.createReceiver(
				EventHubClient.DefaultConsumerGroupName, 
				partitionId, 
				Instant.now()).get();
   ```  
3. **Absolute offset** This option is commonly used to resume receiving events after a previous receiver on the partition 
   has been aborted or suspended for any reason. The offset is a system-supplied string that should not be interpreted by
   the application. The next section will discuss scenarios for using this option.
   
    ``` Java
   PartitionReceiver receiver = ehClient.createReceiver(
       EventHubClient.DefaultConsumerGroupName, 
	   partitionId, 
	   savedOffset).get();
    ``` 
   

##Ownership, Failover, and Epochs

As mentioned in the overview above, the common consumption model for Event Hubs is that multiple consumers process events 
from a single Event Hub in parallel. Depending on the amount of processing work required and the data volume that has to be 
worked through, and also dependent on how resilient the system needs to be against failures, these consumers may be spread 
across multiple different compute nodes (VMs). 

A simple setup for this is to create a fixed assignment of Event Hub partitions to compute nodes. For instance, you 
could have two compute nodes handling events from 8 Event Hub partitions, assigning the first 4 partitions to the 
first node and assigning the second set of 4 to the second node. 

The downside of such a simple model with fixed assignments is that if one of the compute nodes becomes unavailable, no events 
get processed for the partitions owned by that node. 

The alternative is to make ownership dynamic and have all processing nodes reach consensus about who owns which partition,
which is referred to as "[leader election](https://en.wikipedia.org/wiki/Leader_election)" or "consensus" in literature. 
One infrastructure for negotiating leaders is [Apache Zookeeper] (https://zookeeper.apache.org/doc/trunk/recipes.html#sc_leaderElection), 
another one one is [leader election over Azure Blobs](https://msdn.microsoft.com/de-de/library/dn568104.aspx).

> The "event processor host" is a forthcoming extension to this Java client that provides an implementation of leader 
> election over Azure blobs. The event processor host for Java is very similar to the respective implementation available 
> for C# clients.          

As the number of event processor nodes grows or shrinks, a leader election model will yield a redistribution of partition
ownership. More nodes each own fewer partitions, fewer nodes each own more partitions. Since leader election occurs 
external to the Event Hub clients, there's a mechanism needed to allow a new leader for a partition to force the old leader 
to let go of the partition, meaning it must be forced to stop receiving and processing events from the partition.

That mechanism is called **epochs**. An epoch is an integer value that acts as a label for the time period during which the
"current" leader for the partition retains its ownership. The epoch value is provided as an argument to the 
*EventHubClient::createEpochReciver* method.     

  ``` Java
   epochValue = 1
   PartitionReceiver receiver1 = ehClient.createEpochReceiver(
       EventHubClient.DefaultConsumerGroupName, 
	   partitionId, 
	   savedOffset,
       epochValue).get();
  ```
  
 When a new partition owner takes over, it creates a receiver for the same partition, but with a greater epoch value. This will instantly
 cause the previous receiver to be dropped (the service initiates a shutdown of the link) and the new receiver to take over.
     
  ``` Java
   /* obtain checkpoint data */
   epochValue = 2
   PartitionReceiver receiver2 = ehClient.createEpochReceiver(
       EventHubClient.DefaultConsumerGroupName, 
	   partitionId, 
	   savedOffset,
       epochValue).get();
  ```   
  
The new leader obviously also needs to know at which offset processing shall continue. For this, the current owner of a partition should 
periodically record its progress on the event stream to a shared location, tracking the offset of the last processed message. This is 
called "checkpointing". In case of the aforementioned Azure Blob lease election model, the blob itself is a great place to keep this information. 

How often an event processor writes checkpoint information depends on the use-case. Frequent checkpointing may cause excessive writes to  
the checkpoint state location. Too infrequent checkpointing may cause too many events to be re-processed as the new onwer picks up from 
an outdated offset. 

##AMQP 1.0
Azure Event Hubs requires using the AMQP 1.0 protocol for consuming events. 

AMQP 1.0 is a TCP based protocol. For Azure Event Hubs, all traffic *must* be protected using TLS (SSL) and is using 
TCP port 5672. For the WebSocket binding of AMQP, traffic flows via port 443.  

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
| SharedAccessSignature | A previously issued [Shared Access Signature token](https://azure.microsoft.com/en-us/documentation/articles/service-bus-sas-overview/)          |
 
A connection string will therefore have the following form:

```
  Endpoint=sb://clemensveu.servicebus.windows.net&EntityPath=myeventhub&SharedAccessSignature=....
```

Consumers generally have a different relationship with the Event Hub than publishers. Usually there are relatively few consumers 
and those consumers enjoy a high level of trust within the context of a system. The relationshiop between an event consumer
and the Event Hub is commonly also much longer-lived.

It's therefore more common for a consumer to be directly configured with a SAS key rule name and key as part of the 
connection string. In order to prevent the SAS key from leaking, it is stil advisable to use a long-lived
token rather than the naked key.

A generated token will be configured into the connection string with the *SharedAccessSignature* property.   
 
More information about Shared Access Signature in Service Bus and Event Hubs about about how to generate the required tokens 
in a range of languages [can be found on the Azure site.](https://azure.microsoft.com/en-us/documentation/articles/service-bus-sas-overview/) 

The easiest way to obtain a token for development purposes is to copy the connection string from the Azure portal. These tokens
do include key name and key value outright. The portal does not issue tokens.     

