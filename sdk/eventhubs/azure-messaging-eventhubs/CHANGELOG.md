# Release History
## 5.0.0-beta.6 (2019-12-02)
- Artifact name changed from `preview` to `beta`.
- Producer clients (both sync and async) support sending events only using `EventDataBatch`. All other send overloads are removed.
- Async consumer now supports receiving events from all partitions to help getting started scenarios.
- Sync consumer will only support receiving from a single partition.
- `BatchOptions` is renamed to `CreateBatchOptions`.
- `receive()` methods now return `PartitionEvent` which includes `PartitionContext` and `EventData`.
- Producer and consumer clients now support sharing same amqp connection.
- Removed support for user-provided schedulers.
- Configuration for owner level and ability to track last enqueued event properties are now in `ReceiveOptions` and will
be declared at the time of receiving events and not when the client is created.
- `EventProcessorStore` renamed to `CheckpointStore` and method signatures are updated.
- `EventProcessor` renamed to `EventProcessorClient` and `EventProcessorBuilder` renamed to `EventProcessorClientBuilder`.
- New types introduced to simplify functional callbacks used in `EventProcessorClient`.
- `EventProcessorClient` now supports tracking last enqueued event properties.

## 5.0.0-preview.5 (2019-11-01)
- Separate clients for sending and receiving events.
   - `EventHubProducerAsyncClient` and `EventHubProduderClient` for sending events.
   - `EventHubConsumerAsyncClient` and `EventHubConsumerClient` for receiving events.
- Moved `InMemoryPartitionManager` from main package to samples and renamed to `InMemoryEventProcessorStore`
- The `EventProcessorStore`, previously `PartitionManager`, has updated APIs to include `fullyQualifiedNamespace` of 
the Event Hub.
- Updates to `EventProcessor` to allow functional callbacks for processing events, errors etc.
   
## 5.0.0-preview.4 (2019-10-08)
- Proxy support for Event Hubs sync and async clients.
- `EventHubConsumer` and `EventHubAsyncConsumer` now provides last enqueued event information.
- Refactored `azure-messaging-eventhubs` to extract AMQP implementation details to `azure-core-amqp` module.
- Added modules support for JDK 9+.
- Renamed model classes to support Java bean naming convention.
- `EventHubClient` and `EventHubAsyncClient` now provides method to get the name of the Event Hub associated with the client.

## 5.0.0-preview.3 (2019-09-09)

- Added synchronous `EventHubConsumer` and `EventHubProducer`.
- Added support for balancing partitions across multiple instances of `EventProcessor`.
- Added `EventProcessorBuilder` to create `EventProcessor` and removed that functionality from `EventHubClientBuilder`.
- Removed `CheckpointManager`. Checkpointing is done using the `PartitionContext` exposed in `PartitionProcessor` methods.
- Changed `PartitionProcessor` from an interface to an abstract base class.
- Changed `EventData.systemProperties` to exclude already exposed properties (ie. sequence number, enqueued time) from
  the map.

## 5.0.0-preview.2 (2019-08-06)

- Added support for AMQP protocol using web sockets to connect to Azure Event Hubs.
- Added support for publishing events using `EventDataBatch`.
- Added support for processing events from all Event Hub partitions through `EventProcessor`. This early preview is
  intended to allow consumers to test the new design using a single instance that does not persist checkpoints to any
  durable store.
- Added a fixed retry policy implementation.
- Removed operation timeouts from `EventHubClientBuilder`, `EventHubProducerOptions`, and `EventHubConsumerOptions` and
  moved to `RetryOptions` as `tryTimeout(Duration)`.
- Removed exposed retry policies in favor of setting `RetryOptions`.
- Renamed all instances of `EventHubPath` to `EventHubName` to align with the usage context and unify on the chosen
  semantics across the client library for different languages.
- Fixed various bugs for notifying users of link shutdown and errors.

### Known issues

- Proxy support is not implemented.
- `EventHubClient` does not clean up its `EventHubPublishers` and `EventHubConsumers`. These need to be closed manually
by calling `EventHubPublisher.close()` or `EventHubConsumer.close()`.
- On transient AMQP connection/session/link failures, the corresponding transports are not recreated.

## 5.0.0-preview.1 (2019-07-01)

Version 5.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for
.Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For release notes and more information please visit https://aka.ms/azure-sdk-preview1-java

### Features

- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Fetch Event Hub and partition metadata using `EventHubClient`.
- Publish messages to an Azure Event Hub using `EventHubPublisher`.
- Receive messages from an Azure Event Hub using `EventHubConsumer`.

### Known issues

- AMQP protocol using web sockets is not implemented.
- Proxy support is not implemented.
- Event Host Processor is not implemented.
- Creating an `EventDataBatch` is not exposed.
- Getting `ReceiverRuntimeInformation` from `EventHubConsumer` is not implemented.
- `EventHubClient` does not clean up its `EventHubPublishers` and `EventHubConsumers`. These need to be closed manually
  by calling `EventHubPublisher.close()` or `EventHubConsumer.close()`.
- Creating more than two concurrent `EventHubClients` or `EventHubConsumers` does not work. Limit usage of concurrent
  clients and consumers to two to avoid failures.
