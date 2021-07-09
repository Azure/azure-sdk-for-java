# Architecture Docs

## Qpid Proton-J Integration

Qpid Proton-J publishes events and messages via its event-driven process called [`Reactor`][Reactor]. `azure-core-amqp`
hooks into Qpid Proton-J Reactor via `Handlers`.  [`BaseHandler`][BaseHandler] contains all of the events that can be
listened to.  `Handlers` can be associated with classes implementing the `Extendable` interface via
`BaseHandler.setHandler(Extendable, Handler)`.  Proton-J `Connection`, `Session`, `Link`, `Sender` and `Receiver` are
all `Extendable`.

The UML diagram below shows this relationship; the interfaces shown in green are Proton-J classes. Each Proton-J
instance (ie. `Connection`, `Session`, `Sender`, `Receiver`) is associated with one corresponding `*Handler`.  Each
azure-core-amqp `AmqpConnection` is associated with one [`Reactor`][Reactor].  When that instance closes, the AMQP
connection is also closed.

Each [ReactorConnection][ReactorConnection] has one Proton-J [`Reactor`][Reactor] instance.  Each [`Reactor`][Reactor]
has one [`ReactorDispatcher`][ReactorDispatcher] and one [`ReactorExecutor`][ReactorExecutor].

![azure-core-amqp integration with Proton-J][AzureCoreAmpqArchitecture]

## Prefetch and AMQP Link Credits

In Project Reactor, prefetch is the initial number of items to request upstream. Afterwards, 75% of the initial prefetch
is used for subsequent `request(long)`.

In Event Hubs, prefetch is the number of AMQP link credits to put on the link when it is first created.  After those
initial link credits have been consumed, we have different ways of calculating how many credits are added to the link.

The diagram below illustrates how it happens. Things to note:

* Large `EventData` use multiple AMQP link credits.
* There is no backpressure for [`EventHubConsumerAsyncClient.receiveFromPartition()`][EventHubConsumerAsyncClient].
* [`EventHubConsumerAsyncClient.receiveFromPartition()`] returns `EventData` on `Scheduler.single("<name>")`.
  * Since events are not published on another Scheduler, they flow downstream using the Scheduler that
    [`ReceiveLinkHandler.onDelivery`][ReceiveLinkHandler] executed on.
  * All Proton-J events run on the single scheduler because it is not thread-safe.
* [`EventProcessorClient`][EventProcessorClient] uses back-pressure due to `concatMap` and `publishOn` within its
  [PartitionPumpManager.startPartitionPump][PartitionPumpManager].

![Flow of credits when receiving deliveries][ReceiveFlowDiagram]

<!-- Links -->
[BaseHandler]: https://github.com/apache/qpid-proton-j/blob/main/proton-j/src/main/java/org/apache/qpid/proton/engine/BaseHandler.java
[EventHubConsumerAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventHubConsumerAsyncClient.java#L334
[EventProcessorClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventProcessorClient.java
[AzureCoreAmpqArchitecture]: ./architecture-uml.jpeg
[ReceiveFlowDiagram]: ./receive-flow.jpeg
[PartitionPumpManager]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/PartitionPumpManager.java#L228
[Reactor]: https://github.com/apache/qpid-proton-j/blob/main/proton-j/src/main/java/org/apache/qpid/proton/reactor/Reactor.java
[ReceiveLinkHandler]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/handler/ReceiveLinkHandler.java#L97
[ReactorConnection]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ReactorConnection.java
[ReactorDispatcher]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ReactorDispatcher.java
[ReactorExecutor]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ReactorExecutor.java
