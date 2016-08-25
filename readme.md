# Microsoft Azure Event Hubs Client for Java

Azure Event Hubs is a highly scalable publish-subscribe service that can ingest millions of events per second and stream them into multiple applications. 
This lets you process and analyze the massive amounts of data produced by your connected devices and applications. Once Event Hubs has collected the data, 
you can retrieve, transform and store it by using any real-time analytics provider or with batching/storage adapters. 

Refer to the [online documentation](https://azure.microsoft.com/services/event-hubs/) to learn more about Event Hubs in general.

##Overview

This Java client library for Azure Event Hubs allows for both sending events to and receiving events from an Azure Event Hub. 

An **event publisher** is a source of telemetry data, diagnostics information, usage logs, or other log data, as 
part of an emvbedded device solution, a mobile device application, a game title running on a console or other device, 
some client or server based business solution, or a web site.  

An **event consumer** picks up such information from the Event Hub and processes it. Processing may involve aggregation, complex 
computation and filtering. Processing may also involve distribution or storage of the information in a raw or transformed fashion.
Event Hub consumers are often robust and high-scale platform infrastructure parts with built-in analytics capabilites, like Azure 
Stream Analytics, Apache Spark, or Apache Storm.   
   
Most applications will act either as an event publisher or an event consumer, but rarely both. The exception are event 
consumers that filter and/or transform event streams and then forward them on to another Event Hub; an example for such a consumer
and (re-)publisher is Azure Stream Analytics. 

We'll therefore only give a glimpse at publishing and receiving here in this overview and provide further detail in 
the [Publishing Events](PublishingEvents.md) and [Consuming Events](ConsumingEvents.md) guides. 

###Publishing Events

The vast majority of Event Hub applications using this and other client libraries are and will be event publishers. 
And for most of these publishers, publishing events is extremely simple. 

With your Java application referencing this client library,
which is quite simple in a Maven build [as we explain in the guide](PublishingEvents.md), you'll need to import the 
*com.microsoft.azure.eventhubs* package with the *EventData* and *EventHubClient* classes.  
 
 
```Java
    import com.microsoft.azure.eventhubs.*;
```        

Using an Event Hub connection string, which holds all required connection information, including an authorization key or token, 
you then create an *EventHubClient* instance, which manages a secure AMQP 1.0 connection to the Event Hub.   
   
```Java
    final String namespaceName = "----ServiceBusNamespaceName-----";
    final String eventHubName = "----EventHubName-----";
    final String sasKeyName = "-----SharedAccessSignatureKeyName-----";
    final String sasKey = "---SharedAccessSignatureKey----";
    ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
		
    EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
```

Once you have the client in hands, you can package any arbitrary payload as a plain array of bytes and send it. 

```Java
    EventData sendEvent = new EventData(payloadBytes);
    ehClient.send(sendEvent).get();
```
         
The entire client API is built for Java 8's concurrent task model, generally returning 
[*CompleteableFuture<T>*](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html), so the 
*.get()* suffixing the operations in the snippets above just wait until the respective operation is complete.

Learn more about publishing events, including advanced options, and when you should and shouldn't use those options, 
[in the event publisher guide](PublishingEvents.md).

###Consuming Events

Consuming events from Azure Event Hubs is a bit more complex than sending events, because the receivers need to be
aware of Event Hub's partitioning model, while senders can most often ignore it. 

Any Event Hub's event store is split up into at least 4 partitions, each maintaining a separate event log. You can think 
of partitions like lanes on a highway. The more events the Event Hub needs to handle, the more lanes (partitions) you have 
to add. Each partition can handle at most the equivalent of 1 "throughput unit", equivalent to at most 1000 events per 
second and at most 1 Megabyte per second.

Consuming messages is also quite different compared to typical messaging infrastuctures like queues or topic 
subscriptions, where the consumer simply fetches the "next" message. Azure Event Hubs puts the consumer in control of 
the offset from which the log shall be read, and the consumer can repeatedly pick a different or the same offset and read 
the event stream from chosen offsets while the events are being retained. Each partition is therefore loosely analogous 
to a tape drive that you can wind back to a particular mark and then play back to the freshest data available.         
   
Just like the sender, the receiver code imports the package and creates an *EventHubClient* from a given connecting string
      
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

		receiver.setReceiveTimeout(Duration.ofSeconds(5));
``` 

Once the receiver is initialized, getting events is just a matter of calling the *receive()* method in a loop. Each call 
to *receive()* will fetch an enumerable batch of events to process.    		
        
```Java        
		Iterable<EventData> receivedEvents = receiver.receive(maxEventsCount).get();         
```

As you might imagine, there's quite a bit more to know about partitions, about distributing the workload of processing huge and 
fast data streams across several receiver machines, and about managing offsets in such a multi-machine scenario such that 
data is not repeatedly read or, worse, skipped. You can find this and other details discussed in 
the [Consuming Events](ConsumingEvents.md) guide.           

##Using the library 

You will generally not have to build this client library yourself. The build model and options are documented in the 
[Contributor's Guide](developer.md), which also explains how to create and submit proposed patches and extensions, and how to 
build a private version of the client library using a snapshot version of the foundational Apache Qpid Proton-J library, which 
this library uses as its AMQP 1.0 protocol core. 

This library is available for use in Maven projects from the Maven Central Repository, and can be referenced using the
following dependency declaration. The dependency declaration will in turn pull futrher required dependencies, specifically 
the required version of Apache Qpid Proton-J, and the crytography library BCPKIX by the Legion of Bouncy Castle.   

```XML
   	<dependency> 
   		<groupId>com.microsoft.azure</groupId> 
   		<artifactId>azure-eventhubs</artifactId> 
   		<version>0.8.0</version> 
   	</dependency>   
 ```
 
 For different types of build environments, the latest released JAR files can also be [explicitly obtained from the 
 Maven Central Repository]() or from [the Release distribution point on GitHub]().  

###Explore the client library with the Eclipse IDE 

1. Maven is expected to be installed and configured - version > 3.3.9
2. After git-clone'ing to the project, open the shell and navigate to the location where the 'pom.xml' is present
3. Run these commands to prepare this maven project to be opened in Eclipse:
  - mvn -Declipse.workspace=<path_to_workspace> eclipse:configure-workspace
  - mvn eclipse:eclipse
4. Open Eclipse and use "Import Existing Maven projects" to open the project.
5. If you see any Build Errors - make sure the Execution Environment is set to java sdk version 1.8 or higher
  * [go to Project > Properties > 'Java Build Path' > Libraries tab. Click on 'JRE System Library (V x.xx)' and Edit this to be 1.8 or higher]
6. Set these Environment variables to be able to run unit tests:
  * EVENT_HUB_CONNECTION_STRING
  * PARTITION_COUNT
  * EPHTESTSTORAGE

##How to provide feedback

First, if you experience any issues with the runtime behavior of the Azure Event Hubs service, please consider filing a support request
right away. Your options for [getting support are enumerated here](https://azure.microsoft.com/support/options/). In the Azure portal, 
you can file a support request from the "Help and support" menu in the upper right hand corner of the page.   

If you find issues in this library or have suggestions for improvement of code or documentation, [you can file an issue in the project's 
GitHub repository.](https://github.com/Azure/azure-event-hubs/issues). Issues related to runtime behavior of the service, such as 
sporadic exceptions or apparent service-side performance or reliability issues can not be handled here.

Generally, if you want to discuss Azure Event Hubs or this client library with the community and the maintainers, you can turn to 
[stackoverflow.com under the #azure-eventhub tag](http://stackoverflow.com/questions/tagged/azure-eventhub) or the 
[MSDN Service Bus Forum](https://social.msdn.microsoft.com/Forums/en-US/home?forum=servbus). 
