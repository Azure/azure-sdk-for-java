
# General Overview of Microsoft Azure Event Hubs client for Java

This Java client library for Azure Event Hubs allows for both sending events to and receiving events from an Azure Event Hub. 

An **event publisher** is a source of telemetry data, diagnostics information, usage logs, or other log data, as 
part of an embedded device solution, a mobile device application, a game title running on a console or other device, 
some client or server based business solution, or a web site.  

An **event consumer** picks up such information from the Event Hub and processes it. Processing may involve aggregation, complex 
computation and filtering. Processing may also involve distribution or storage of the information in a raw or transformed fashion.
Event Hub consumers are often robust and high-scale platform infrastructure parts with built-in analytics capabilities, like Azure 
Stream Analytics, Apache Spark, or Apache Storm.   
   
Most applications will act either as an event publisher or an event consumer, but rarely both. The exception are event 
consumers that filter and/or transform event streams and then forward them on to another Event Hub; an example for such a consumer
and (re-)publisher is Azure Stream Analytics. 

We'll therefore only give a glimpse at publishing and receiving here in this overview and provide further detail in 
the [Publishing Events](PublishingEvents.md) and [Consuming Events](ConsumingEvents.md) guides. 

### Publishing Events

The vast majority of Event Hub applications using this and other client libraries are and will be event publishers. 
And for most of these publishers, publishing events is extremely simple. 

With your Java application referencing this client library,	
which is quite simple in a Maven build [as we explain in the guide](PublishingEvents.md), you'll need to import the 
*com.microsoft.azure.eventhubs* package with the *EventData* and *EventHubClient* classes.  
 
 
```Java
    import com.microsoft.azure.eventhubs.*;
```

Event Hubs client library uses qpid proton reactor framework which exposes AMQP connection and message delivery related 
state transitions as reactive events. In the process,
the library will need to run many asynchronous tasks while sending and receiving messages to Event Hubs.
So, `EventHubClient` requires an instance of `ScheduledExecutorService`, where all these tasks are run.


```Java
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(8)
```

Using an Event Hub connection string, which holds all required connection information, including an authorization key or token, 
you then create an *EventHubClient* instance, which manages a secure AMQP 1.0 connection to the Event Hub.   
   
```Java
    ConnectionStringBuilder connStr = new ConnectionStringBuilder()
                .setNamespaceName("----ServiceBusNamespaceName-----")
                .setEventHubName("----EventHubName-----")
                .setSasKeyName("-----SharedAccessSignatureKeyName-----")
                .setSasKey("---SharedAccessSignatureKey----");	
	
    EventHubClient ehClient = EventHubClient.createSync(connStr.toString(), executor);
```

Once you have the client in hands, you can package any arbitrary payload as a plain array of bytes and send it. 

```Java
    EventData sendEvent = EventData.create(payloadBytes);
    ehClient.sendSync(sendEvent);
```
         
The entire client API is built for Java 8's concurrent task model, generally returning 
[*CompletableFuture<T>*](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html), so the library has these methods suffixed with *Sync* as their Synchronous counterparts/varaints.

Learn more about publishing events, including advanced options, and when you should and shouldn't use those options, 
[in the event publisher guide](PublishingEvents.md).

### Consuming Events

Consuming events from Azure Event Hubs is a bit more complex than sending events, because the receivers need to be
aware of Event Hub's partitioning model, while senders can most often ignore it. 

Any Event Hub's event store is split up into at least 4 partitions, each maintaining a separate event log. You can think 
of partitions like lanes on a highway. The more events the Event Hub needs to handle, the more lanes (partitions) you have 
to add. Each partition can handle at most the equivalent of 1 "throughput unit", equivalent to at most 1000 events per 
second and at most 1 Megabyte per second.

Consuming messages is also quite different compared to typical messaging infrastucture like queues or topic 
subscriptions, where the consumer simply fetches the "next" message. Azure Event Hubs puts the consumer in control of 
the offset from which the log shall be read, and the consumer can repeatedly pick a different or the same offset and read 
the event stream from chosen offsets while the events are being retained. Each partition is therefore loosely analogous 
to a tape drive that you can wind back to a particular mark and then play back to the freshest data available.         
   
Just like the sender, the receiver code imports the package and creates an *EventHubClient* from a given connecting string.
The receiver code then creates (at least) one *PartitionReceiver* that will receive the data. The receiver is seeded with 
an offset, in the snippet below it's simply the start of the log.    
		
```Java
		String partitionId = "0";
		PartitionReceiver receiver = ehClient.createReceiverSync(
                	EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                	partitionId,
                	EventPosition.fromStartOfStream());

		receiver.setReceiveTimeout(Duration.ofSeconds(20));
``` 

Once the receiver is initialized, getting events is just a matter of calling the *receive()* method in a loop. Each call 
to *receive()* will fetch an enumerable batch of events to process. 
Simply put, create a receiver from a specific offset and from then on, the log can be read only in one direction (oldest to latest event).	
        
```Java        
		Iterable<EventData> receivedEvents = receiver.receiveSync(maxEventsCount);         
```

As you might imagine, there's quite a bit more to know about partitions, about distributing the workload of processing huge and 
fast data streams across several receiver machines, and about managing offsets in such a multi-machine scenario such that 
data is not repeatedly read or, worse, skipped. You can find this and other details discussed in 
the [Consuming Events](ConsumingEvents.md) guide.    
