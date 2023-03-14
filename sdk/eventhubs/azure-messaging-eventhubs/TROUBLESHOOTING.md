# Troubleshoot Event Hubs issues

This troubleshooting guide covers failure investigation techniques, common errors for the credential types in the Azure Event Hubs Java client library, and mitigation steps to resolve these errors.

- [Handle Event Hubs exceptions](#handle-event-hubs-exceptions)
  - [Find relevant information in exception messages](#find-relevant-information-in-exception-messages)
  - [Commonly encountered exceptions](#commonly-encountered-exceptions)
- [Permission issues](#permission-issues)
- [Connectivity issues](#connectivity-issues)
  - [Timeout when connecting to service](#timeout-when-connecting-to-service)
  - [SSL handshake failures](#ssl-handshake-failures)
  - [Socket exhaustion errors](#socket-exhaustion-errors)
  - [Connect using an IoT connection string](#connect-using-an-iot-connection-string)
  - [Cannot add components to the connection string](#cannot-add-components-to-the-connection-string)
- [Enable and configure logging](#enable-and-configure-logging)
  - [Configuring Log4J 2](#configuring-log4j-2)
  - [Configuring logback](#configuring-logback)
  - [Enable AMQP transport logging](#enable-amqp-transport-logging)
  - [Reduce logging](#reduce-logging)
- [Troubleshoot EventProducerAsyncClient/EventProducerClient issues](#troubleshoot-eventproducerasyncclienteventproducerclient-issues)
  - [Cannot set multiple partition keys for events in EventDataBatch](#cannot-set-multiple-partition-keys-for-events-in-eventdatabatch)
  - [Setting partition key on EventData is not set in Kafka consumer](#setting-partition-key-on-eventdata-is-not-set-in-kafka-consumer)
- [Troubleshoot EventProcessorClient issues](#troubleshoot-eventprocessorclient-issues)
  - [412 precondition failures when using an event processor](#412-precondition-failures-when-using-an-event-processor)
  - [Partition ownership changes frequently](#partition-ownership-changes-frequently)
  - ["...current receiver '\<RECEIVER\_NAME\>' with epoch '0' is getting disconnected"](#current-receiver-receiver_name-with-epoch-0-is-getting-disconnected)
  - [High CPU usage](#high-cpu-usage)
  - [Out of memory and choosing the heap size](#out-of-memory-and-choosing-the-heap-size)
  - [Processor client stops receiving](#processor-client-stops-receiving)
  - [Duplicate EventData received when processor is restarted](#duplicate-eventdata-received-when-processor-is-restarted)
  - [Migrate from legacy to new client library](#migrate-from-legacy-to-new-client-library)
- [Performance considerations for EventProcessorClient](#performance-considerations-for-eventprocessorclient)
  - [Using `processEvent` or `processEventBatch`](#using-processevent-or-processeventbatch)
  - [Costs of checkpointing](#costs-of-checkpointing)
  - [Using `LoadBalancingStrategy.BALANCED` or `LoadBalancingStrategy.GREEDY`](#using-loadbalancingstrategybalanced-or-loadbalancingstrategygreedy)
  - [Configuring `prefetchCount`](#configuring-prefetchcount)
- [Get additional help](#get-additional-help)
  - [Filing GitHub issues](#filing-github-issues)

## Handle Event Hubs exceptions

All Event Hubs exceptions are wrapped in an [AmqpException][AmqpException].  They often have an underlying AMQP error code which specifies whether an error should be retried.  For retryable errors (ie. `amqp:connection:forced` or `amqp:link:detach-forced`), the client libraries will attempt to recover from these errors based on the [retry options][AmqpRetryOptions] specified when instantiating the client.  To configure retry options, follow the sample [Publish events to specific partition][PublishEventsToSpecificPartition].  If the error is non-retryable, there is some configuration issue that needs to be resolved.

The recommended way to solve the specific exception the AMQP exception represents is to follow the
[Event Hubs Messaging Exceptions][EventHubsMessagingExceptions] guidance.

### Find relevant information in exception messages

An [AmqpException][AmqpException] contains three fields which describe the error.

* **getErrorCondition**: The underlying AMQP error. A description of the errors can be found in the [AmqpErrorCondition][AmqpErrorCondition] javadocs or the [OASIS AMQP 1.0 spec][AmqpSpec].
* **isTransient**: Whether or not trying to perform the same operation is possible.  SDK clients apply the retry policy when the error is transient.
* **getErrorContext**: Information about where the AMQP error originated.
  * [LinkErrorContext][LinkErrorContext]: Errors that occur in either the send/receive link.
  * [SessionErrorContext][SessionErrorContext]: Errors that occur in the session.
  * [AmqpErrorContext][AmqpErrorContext]: Errors that occur in the connection or a general AMQP error.

### Commonly encountered exceptions

#### `amqp:connection:forced` and `amqp:link:detach-forced`

When the connection to Event Hubs is idle, the service will disconnect the client after some time.  This is not a problem as the clients will re-establish a connection when a service operation is requested.  More information can be found in the [AMQP troubleshooting documentation][AmqpTroubleshooting].

## Permission issues

An `AmqpException` with an [`AmqpErrorCondition`][AmqpErrorCondition] of "amqp:unauthorized-access" means that the provided credentials do not allow for them to perform the action (receiving or sending) with Event Hubs.

* [Double check you have the correct connection string][GetConnectionString]
* [Ensure your SAS token is generated correctly][AuthorizeSAS]

[Troubleshoot authentication and authorization issues with Event Hubs][troubleshoot_authentication_authorization] lists other possible solutions.

## Connectivity issues

### Timeout when connecting to service

* Verify that the connection string or fully qualified domain name specified when creating the client is correct.  [Get an Event Hubs connection string][GetConnectionString] demonstrates how to acquire a connection string.
* Check the firewall and port permissions in your hosting environment and that the AMQP ports 5671 and 5762 are open.
  * Make sure that the endpoint is allowed through the firewall.
* Try using WebSockets, which connects on port 443.  See [configure web sockets][PublishEventsWithWebSocketsAndProxy] sample.
* See if your network is blocking specific IP addresses.
  * [What IP addresses do I need to allow?][EventHubsIPAddresses]
* If applicable, check the proxy configuration.  See [configure proxy][PublishEventsWithWebSocketsAndProxy] sample.
* For more information about troubleshooting network connectivity is at [Event Hubs troubleshooting][EventHubsTroubleshooting]

### SSL handshake failures

This error can occur when an intercepting proxy is used.  We recommend testing in your hosting environment with the proxy disabled to verify.

### Socket exhaustion errors

Applications should prefer treating the Event Hubs clients as a singleton, creating and using a single instance through the lifetime of their application.  This is important as each client type manages its connection; creating a new Event Hub client results in a new AMQP connection, which uses a socket.  Additionally, it is essential to be aware that clients inherit from `java.io.Closeable`, so your application is responsible for calling `close()` when it is finished using a client.

To use the same AMQP connection when creating multiple clients, you can use the `EventHubClientBuilder.shareConnection()` flag, hold a reference to that `EventHubClientBuilder`, and create new clients from that same builder instance.

### Connect using an IoT connection string

Because translating a connection string requires querying the IoT Hub service, the Event Hubs client library cannot use it directly.  The [IoTConnectionString.java][IoTConnectionString] sample describes how to query IoT Hub to translate an IoT connection string into one that can be used with Event Hubs.

Further reading:
* [Control access to IoT Hub using Shared Access Signatures][IoTHubSAS]
* [Read device-to-cloud messages from the built-in endpoint][IoTEventHubEndpoint]

### Cannot add components to the connection string

The legacy Event Hub clients allowed customers to add components to the connection string retrieved from the portal.  The legacy clients are in packages [com.microsoft.azure:azure-eventhubs][MavenAzureEventHubs] and [com.microsoft.azure:azure-eventhubs-eph][MavenAzureEventHubsEPH].  The current generation supports connection strings only in the form published by the Azure portal.

#### Adding "TransportType=AmqpWebSockets"

To use web sockets, see the sample [PublishEventsWithSocketsAndProxy.java][PublishEventsWithWebSocketsAndProxy].

#### Adding "Authentication=Managed Identity"

To authenticate with Managed Identity, see the sample [PublishEventsWithAzureIdentity.java][PublishEventsWithAzureIdentity].

For more information about the `Azure.Identity` library, check out our [Authentication and the Azure SDK][AuthenticationAndTheAzureSDK] blog post.

## Enable and configure logging

The Azure SDK for Java offers a consistent logging story to help troubleshoot application errors and expedite their resolution.  The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue.  View the [logging][Logging] wiki for guidance about enabling logging.

In addition to enabling logging, setting the log level to `VERBOSE` or `DEBUG` provides insights into the library's state.  Below are sample log4j2 and logback configurations to reduce the excessive messages when verbose logging is enabled.

### Configuring Log4J 2

1. Add the dependencies in your pom.xml using ones from the [logging sample pom.xml][LoggingPom] under the "Dependencies required for Log4j2" section.
2. Add [log4j2.xml][log4j2] to your `src/main/resources`.

### Configuring logback

1. Add the dependencies in your pom.xml using ones from the [logging sample pom.xml][LoggingPom] under the "Dependencies required for logback" section.
2. Add [logback.xml][logback] to your `src/main/resources`.

### Enable AMQP transport logging

If enabling client logging is not enough to diagnose your issues.  You can enable logging to a file in the underlying
AMQP library, [Qpid Proton-J][qpid_proton_j_apache].  Qpid Proton-J uses `java.util.logging`. You can enable logging by
creating a configuration file with the contents below.  Or set `proton.trace.level=ALL` and whichever configuration options
you want for the `java.util.logging.Handler` implementation.  The implementation classes and their options can be found in
[Java 8 SDK javadoc][java_8_sdk_javadocs].

To trace the AMQP transport frames, set the environment variable: `PN_TRACE_FRM=1`.

#### Sample "logging.properties" file

The configuration file below logs TRACE level output from proton-j to the file "proton-trace.log".

```
handlers=java.util.logging.FileHandler
.level=OFF
proton.trace.level=ALL
java.util.logging.FileHandler.level=ALL
java.util.logging.FileHandler.pattern=proton-trace.log
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=[%1$tF %1$tr] %3$s %4$s: %5$s %n
```

### Reduce logging

One way to decrease logging is to change the verbosity.  Another is to add filters that exclude logs from logger names packages like `com.azure.messaging.eventhubs` or `com.azure.core.amqp`.  Examples of this can be found in the XML files in [Configuring Log4J 2](#configuring-log4j-2) and [Configure logback](#configuring-logback).

When submitting a bug, log messages from classes in the following packages are interesting:

* `com.azure.core.amqp.implementation`
* `com.azure.core.amqp.implementation.handler`
   * The exception is that the onDelivery message in ReceiveLinkHandler can be ignored.
* `com.azure.messaging.eventhubs.implementation`

## Troubleshoot EventProducerAsyncClient/EventProducerClient issues

### Cannot set multiple partition keys for events in EventDataBatch

When publishing messages, the Event Hubs service supports a single partition key for each EventDataBatch.  Customers can consider using the buffered producer client `EventHubBufferedProducerClient` if they want that capability.  Otherwise, they'll have to manage their batches.

### Setting partition key on EventData is not set in Kafka consumer

The partition key of the EventHubs event is available in the Kafka record headers, the protocol specific key being "x-opt-partition-key" in the header.

By design, Event Hubs does not promote the Kafka message key to be the Event Hubs partition key nor the reverse because with the same value, the Kafka client and the Event Hub client likely send the message to two different partitions.  It might cause some confusion if we set the value in the cross-protocol communication case.  Exposing the properties with a protocol specific key to the other protocol client should be good enough.

## Troubleshoot EventProcessorClient issues

### 412 precondition failures when using an event processor

412 precondition errors occur when the client tries to take or renew ownership of a partition, but the local version of the ownership record is outdated.  This occurs when another processor instance steals partition ownership.  See [Partition ownership changes a lot](#partition-ownership-changes-a-lot) for more information.

### Partition ownership changes frequently

When the number of EventProcessorClient instances changes (i.e. added or removed), the running instances try to load-balance partitions between themselves.  For a few minutes after the number of processors changes, partitions are expected to change owners.   Once balanced, partition ownership should be stable and change infrequently.  If partition ownership is changing frequently when the number of processors is constant, this likely indicates a problem.  It is recommended that a GitHub issue with logs and a repro be filed in this case.

### "...current receiver '<RECEIVER_NAME>' with epoch '0' is getting disconnected"

The entire error message looks something like this:

> New receiver 'nil' with higher epoch of '0' is created hence current receiver 'nil' with epoch '0'
> is getting disconnected. If you are recreating the receiver, make sure a higher epoch is used.
> TrackingId:<GUID>, SystemTracker:<NAMESPACE>:eventhub:<EVENT_HUB_NAME>|<CONSUMER_GROUP>,
> Timestamp:2022-01-01T12:00:00}"}

This error is expected when load balancing occurs after EventProcessorClient instances are added or removed.  Load balancing is an ongoing process.  When using the BlobCheckpointStore with your consumer, every ~30 seconds (by default), the consumer will check to see which consumers have a claim for each partition, then run some logic to determine whether it needs to 'steal' a partition from another consumer.  The service mechanism used to assert exclusive ownership over a partition is known as the [Epoch][Epoch].

However, if no instances are being added or removed, there is an underlying issue that should be addressed.  See [Partition ownership changes a lot](#partition-ownership-changes-a-lot) for additional information and [Filing GitHub issues](#filing-github-issues).

### High CPU usage

High CPU usage is usually because an instance owns too many partitions.  We recommend no more than three partitions for every 1 CPU core; better to start with 1.5 partitions for each CPU core and test increasing the number of partitions owned.

### Out of memory and choosing the heap size

The Out of memory (OOM) can happen if the current max heap for the JVM is insufficient to run the application. You may want to measure the application's heap requirement, then, based on the result, size the heap by setting the appropriate max heap memory (-Xmx JVM option).

Note that you should not specify -Xmx as a value larger than the memory available or limit set for the host (VM, container), e.g., the memory requested in the container's configuration. You should allocate enough memory for the host to support the Java heap.

A typical way to measure the value for max Java Heap is -

Run the application in an environment close to production, where the application sends, receives, and processes events under the peak load expected in production.

Wait for the application to reach a steady state. At this stage, the application and JVM would have loaded all domain objects, class types, static instances, object pools (TCP, DB connection pools), etc.

Under the steady state you will see the stable sawtooth-shaped pattern for the heap collection -

![healthy-heap-pattern][HealthyHeapPattern]

Once the application reaches the steady state, force a full GC using tools like JConsole. Observe the memory occupied after the full GC. You want to size the heap such that only 30% is occupied after the full GC. You can use this value to set the max heap size (-Xmx).

If you're on the container, then size the container to have an "additional ~1 GB" of memory for the "non-heap" need for the JVM instance.

### Processor client stops receiving

The processor client often is continually running in a host application for days on end.  Sometimes, they notice that EventProcessorClient is not processing one or more partitions.  Usually, this is not enough information to determine why the exception occurred.  The EventProcessorClient stopping is the symptom of an underlying cause (i.e. race condition) that occurred while trying to recover from a transient error.  Please see [Filing Github issues](#filing-github-issues) for the information we require.

### Duplicate EventData received when processor is restarted

The `EventProcessorClient` and Event Hub service guarantees an "at least once" delivery. Customers can add metadata to discern duplicate events. The answer to [Does Azure Event Hub guarantee an at-least once delivery?][StackOverflowAtLeastOnce] provides additional information.  If customers require only-once delivery, they may consider Service Bus, which waits for an acknowledgement from the client.  A comparison of the messaging services is documented in [Choosing between Azure messaging services][CompareMessagingServices].

### Migrate from legacy to new client library

The [migration guide][MigrationGuide] includes steps on migrating from the legacy client and migrating legacy checkpoints.

## Performance considerations for EventProcessorClient

### Using `processEvent` or `processEventBatch`

When using the `processEvent` callback, each `EventData` received calls the users' code. This works well with low or moderate traffic in the Event Hub.

If the Event Hub has high traffic and high throughput is expected, the aggregated cost of continuously calling the users' callback hinders performance of `EventProcessorClient`. In this case, users should use `processEventBatch`.

For each partition, the users' callback is invoked one at a time, so high processing time in the callback hinders performance as the `EventProcessorClient` does not continue to push more events downstream nor request more `EventData` from Event Hubs service.

### Costs of checkpointing

When using Azure Blob Storage as the checkpoint store, there is a network cost to checkpointing as it makes an HTTP request and waits for a response. This process could take up to several seconds due to network latency, the performance of Azure Blob Storage, resource location, etc.

Checkpointing after _every_ `EventData` is processed hinders performance due to the cost of making these HTTP requests. Users should not checkpoint if their callback processed no events or checkpoint after processing some number of events.

### Using `LoadBalancingStrategy.BALANCED` or `LoadBalancingStrategy.GREEDY`

When using `LoadBalancingStrategy.BALANCED`, the `EventProcessorClient` claims one partition for every load balancing cycle. If there are 32 partitions in an Event Hub, it will take 32 load-balancing iterations to claim all the partitions. If users know a set number of `EventProcessorClient` instances are running, they can use `LoadBalancingStrategy.GREEDY` to claim their share of the partitions in one load-balancing cycle.

[LoadBalancingStrategy javadocs][LoadBalancingStrategy] contains additional information about each strategy.

### Configuring `prefetchCount`

The default prefetch value is 500. When the AMQP receive link is opened, it places 500 credits on the link. Assuming that each `EventData` is one link credit, `EventProcessorClient` prefetches 500 `EventData`. When _all_ the events are consumed, the processor client adds 500 credits to the link to receive more messages. This flow repeats while the `EventProcessorClient` still has ownership of a partition.

Configuring `prefetchCount` may have performance implications if the number is _low_. Each time the AMQP receive link places credits, the remote service sends an ACK. For high throughput scenarios, the overhead of making thousands of client requests and service ACKs may hinder performance.

Configuring `prefetchCount` may have performance implications if the number is _very high_. When _x_ credits are placed on the line, the Event Hubs service knows that it can send at most _x_ messages. When each `EventData` is received, they are placed in an in-memory queue, waiting to be processed. The high number of `EventData` in the queue can result in very high memory usage.

## Get additional help

Additional information on ways to reach out for support can be found in the [SUPPORT.md][SUPPORT] at the repo's root.

### Filing GitHub issues

When filing GitHub issues, the following details are requested:

* Event Hub environment
  * How many partitions?
* EventProcessorClient environment
  * What is the machine(s) specs processing your Event Hub?
  * How many instances are running?
  * What is the max heap set (i.e., Xmx)?
* What is the average size of each EventData?
* What is the traffic pattern like in your Event Hub?  (i.e. # messages/minute and if the EventProcessorClient is always busy or has slow traffic periods.)
* Repro code and steps
  * This is important as we often cannot reproduce the issue in our environment.
* Logs.  We need DEBUG logs, but if that is not possible, INFO at least.  Error and warning level logs do not provide enough information.  The period of at least +/- 10 minutes from when the issue occurred.

<!-- repo links -->
[IoTConnectionString]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/java/com/azure/messaging/eventhubs/IoTHubConnectionSample.java
[log4j2]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs/docs/log4j2.xml
[logback]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs/docs/logback.xml
[LoggingPom]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs/docs/pom.xml
[MigrationGuide]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/migration-guide.md
[PublishEventsToSpecificPartition]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/java/com/azure/messaging/eventhubs/PublishEventsToSpecificPartition.java
[PublishEventsWithAzureIdentity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithAzureIdentity.java
[PublishEventsWithWebSocketsAndProxy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/samples/java/com/azure/messaging/eventhubs/PublishEventsWithWebSocketsAndProxy.java
[SUPPORT]: https://github.com/Azure/azure-sdk-for-java/blob/main/SUPPORT.md
[HealthyHeapPattern]: ./docs/images/healthyheappattern.png
[LoadBalancingStrategy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/LoadBalancingStrategy.java

<!-- docs.microsoft.com links -->
[AmqpErrorCondition]: https://docs.microsoft.com/java/api/com.azure.core.amqp.exception.amqperrorcondition
[AmqpErrorContext]: https://docs.microsoft.com/java/api/com.azure.core.amqp.exception.amqperrorcontext
[AmqpException]: https://docs.microsoft.com/java/api/com.azure.core.amqp.exception.amqpexception
[SessionErrorContext]: https://docs.microsoft.com/java/api/com.azure.core.amqp.exception.sessionerrorcontext
[LinkErrorContext]: https://docs.microsoft.com/java/api/com.azure.core.amqp.exception.linkerrorcontext

[AmqpTroubleshooting]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-amqp-troubleshoot
[AuthorizeSAS]: https://docs.microsoft.com/azure/event-hubs/authorize-access-shared-access-signature
[Epoch]: https://docs.microsoft.com/azure/event-hubs/event-hubs-event-processor-host#epoch
[EventHubsIPAddresses]: https://docs.microsoft.com/azure/event-hubs/troubleshooting-guide#what-ip-addresses-do-i-need-to-allow
[EventHubsMessagingExceptions]: https://docs.microsoft.com/azure/event-hubs/event-hubs-messaging-exceptions
[EventHubsTroubleshooting]: https://docs.microsoft.com/azure/event-hubs/troubleshooting-guide
[GetConnectionString]: https://docs.microsoft.com/azure/event-hubs/event-hubs-get-connection-string
[IoTEventHubEndpoint]: https://docs.microsoft.com/azure/iot-hub/iot-hub-devguide-messages-read-builtin
[IoTHubSAS]: https://docs.microsoft.com/azure/iot-hub/iot-hub-dev-guide-sas#security-tokens
[Logging]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[troubleshoot_authentication_authorization]: https://docs.microsoft.com/azure/event-hubs/troubleshoot-authentication-authorization

<!-- external links -->
[AuthenticationAndTheAzureSDK]: https://devblogs.microsoft.com/azure-sdk/authentication-and-the-azure-sdk
[MavenAzureEventHubs]: https://search.maven.org/artifact/com.microsoft.azure/azure-eventhubs/
[MavenAzureEventHubsEPH]: https://search.maven.org/artifact/com.microsoft.azure/azure-eventhubs-eph
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[AmqpSpec]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-types-v1.0-os.html
[qpid_proton_j_apache]: https://qpid.apache.org/proton/
[CompareMessagingServices]: https://learn.microsoft.com/azure/event-grid/compare-messaging-services
[StackOverflowAtLeastOnce]: https://stackoverflow.com/questions/33220685/does-azure-event-hub-guarantees-at-least-once-delivery/33577018#33577018
