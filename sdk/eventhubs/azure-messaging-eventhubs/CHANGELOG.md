# Release History

## 5.13.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

- Introducing ReactorShim to proxy certain reactive operations to appropriate Reactor operators, these are the operations for which recent Reactor versions have more optimized operators compared to an older version, or same operators with breaking change across Reactor versions
- When available, using the backpressure aware windowTimeout operator through ReactorShim. ([23950](https://github.com/Azure/azure-sdk-for-java/issues/23950))

### Other Changes

## 5.12.1 (2022-06-10)

### Features Added

- Updated processor client process error when load balance occurs errors.

### Bugs Fixed

- Fixes trace context propagation issue: links to *message* spans were not populated on *send* span. ([#28951](https://github.com/Azure/azure-sdk-for-java/pull/28951))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.29.1`.
- Upgraded `azure-core-amqp` to `2.5.2`.

## 5.12.0 (2022-05-16)

### Features Added

- `EventData.setBodyAsBinaryData` is exposed.

### Breaking Changes

- `EventData` extends from `MessageContent`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.28.0`.
- Upgraded `azure-core-amqp` to `2.5.0`.

## 5.11.2 (2022-04-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.27.0`.
- Upgraded `azure-core-amqp` to `2.4.2`.

## 5.11.1 (2022-03-17)

### Features Added
- Updated the receiver to retry to obtain a new connection if the RequestResponseChannel in the current connection is disposed.

### Bugs Fixed

- Removed the incorrect lock from `EventDataBatch.tryAdd()` implementation and documented that this API is not thread-safe. ([#25910](https://github.com/Azure/azure-sdk-for-java/issues/25910))
- Fixed a bug where users get a NullPointerException when getting `LastEnqueuedEventProperties` for an empty window. ([#27121](https://github.com/Azure/azure-sdk-for-java/issues/27121))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to `1.26.0`.
- Upgraded `azure-core-amqp` from `2.4.0` to `2.4.1`.

## 5.11.0 (2022-02-11)

### Bugs Fixed

- Fixed a bug that when received message does not have trace context, span is not created. ([#25182](https://github.com/Azure/azure-sdk-for-java/issues/25182))

### Other Changes

- Updated load balancing strategy, ownership interval, and load balancing intervals. ([#25039](https://github.com/Azure/azure-sdk-for-java/issues/25039))

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to `1.25.0`.
- Upgraded `azure-core-amqp` from `2.3.7` to `2.4.0`.

## 5.10.4 (2022-01-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to `1.24.1`.
- Upgraded `azure-core-amqp` from `2.3.3` to `2.3.7`.

## 5.10.3 (2021-11-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to `1.22.0`.
- Upgraded `azure-core-amqp` from `2.3.3` to `2.3.4`.

## 5.10.2 (2021-10-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.20.0` to `1.21.0`.
- Upgraded `azure-core-amqp` from `2.3.2` to `2.3.3`.

## 5.10.1 (2021-09-20)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.20.0`.
- Update `azure-core-amqp` dependency to `2.3.2`.

## 5.10.0 (2021-08-19)

### Features Added

- Add `EventProcessorClientBuilder.prefetchCount(int)` overload.

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.19.0`.
- Update `azure-core-amqp` dependency to `2.3.1`.

## 5.9.0 (2021-07-09)

### Features Added

- Add additional fields to `EventData`. `getContentType()`, `getCorrelationId()`, `getMessageId()` and the corresponding setters were added.
- Add `EventData.getRawAmqpMessage()`. Data in `EventData.getSystemProperties()` is backed by `AmqpAnnotatedMessage` but are read-only.

### Dependency Updates

- Update `azure-core` dependency to `1.18.0`.
- Update `azure-core-amqp` dependency to `2.3.0`.

## 5.8.0 (2021-06-14)

### Features Added

- Add `EventHubClientBuilder.credential(String, String, AzureNamedKeyCredential)` overload.
- Add `EventHubClientBuilder.credential(String, String, AzureSasCredential)` overload.
- Add `EventProcessorClientBuilder.credential(String, String, AzureNamedKeyCredential)` overload.
- Add `EventProcessorClientBuilder.credential(String, String, AzureSasCredential)` overload.
- Add `EventHubConnectionStringProperties` to get connection string properties.

### Dependency Updates

- Update `azure-core` dependency to `1.17.0`.
- Update `azure-core-amqp` dependency to `2.2.0`.

## 5.7.1 (2021-05-10)

### Dependency Updates

- Update `azure-core` dependency to `1.16.0`.
- Update `azure-core-amqp` dependency to `2.0.5`.

## 5.7.0 (2021-04-12)

## Bug Fixes

- Update AMQP receive link to add credits on the link based on backpressure
  request from downstream.
- Update logging to be less verbose.

### Dependency Updates

- Update `azure-core` dependency to `1.15.0`.
- Update `azure-core-amqp` dependency to `2.0.4`.

## 5.6.0 (2021-03-10)
### Bug Fixes
- Update to end the trace span regardless of the scope instance type for process operation tracing spans.

### Dependency Updates
- Update `azure-core` dependency to `1.14.0`.
- Update `azure-core-amqp` dependency to `2.0.3`.

## 5.4.0 (2021-01-14)
### New features
- Add `clientOptions` to `EventProcessorClientBuilder` to support setting user's application id used in user-agent
 property of the amqp connection.

### Dependency Updates
- Update `azure-core` dependency to `1.12.0`.
- Update `azure-core-amqp` dependency to `2.0.1`.
- Update `azure-identity` dependency to `1.2.2`.

## 5.4.0-beta.1 (2020-11-12)
### Breaking changes
- Removed `ObjectBatch` and related `createBatch()` and `send()` operations in favor of supporting `BinaryData` in
  `EventData`.

## 5.3.1 (2020-10-30)
### Bug fixes
- Eagerly close top-level client in `EventProcessorClient` after fetching the list of partitions instead of waiting until
 the connection times out.
- Added checks for matching lost link name with the current link name before propagating the error in
 `AmqpReceiveLinkProcessor`.

## 5.3.0 (2020-10-12)
### New Features
- Add `clientOptions` to `EventHubClientBuilder` to support for setting user's application id in the user-agent property
of the amqp connection.

### Other Changes
- `EventHubProcessorClient` checks connection status of each partition consumer periodically and closes
the partition consumer to rebuild the connection later.

### Dependency Updates
- Update `azure-core` dependency to `1.9.0`.
- Update `azure-core-amqp` dependency to `1.6.0`.
- Update `azure-identity` dependency to `1.1.3`.

## 5.2.0 (2020-09-11)
- Default scheme to 'sb://' if no scheme is set in 'Endpoint'.
- Update dependency version of `azure-core-amp` to `1.5.1`
- Add support for connection strings containing Shared Access Signature
- Add option to control the load balancing cycle interval.
- Add option to control the partition ownership expiration duration.
- Add option to configure the load balancing strategy to either use balanced or greedy approach.

## 5.2.0-beta.2 (2020-08-14)
- Support for object serializer to send and receive strongly-typed objects.

## 5.2.0-beta.1 (2020-07-08)
- Add option to control the load balancing cycle interval.
- Add option to control the partition ownership expiration duration.
- Add option to configure the load balancing strategy to either use balanced or greedy approach.

## 5.1.2 (2020-07-08)
- Updated dependency version of `azure-core-amqp` which has a bug fix for updating User Agent string format.
- Fix bug where batch receive handler runs on non-blocking thread and fails on blocking calls.

## 5.1.1 (2020-06-12)
- Fix bug where receiver link fails to add credits to new links created after an existing link is closed.
- Add a check to load balancer task to not run if the previous load balancer task is still in progress.
- Updated dependency version of `azure-core-amqp` to `1.2.0`

## 5.1.0 (2020-05-07)
- Add support for sending a collection of events as a single batch from `EventHubProducerClient` and `EventHubProducerAsyncClient`.
- Updated dependency version of `azure-core-amqp` to `1.1.2`.

## 5.1.0-beta.1 (2020-04-08)
- Add support for heartbeat for single process event function in Event Processor Client.
- Add support for receiving events in batches in Event Processor Client.

## 5.0.3 (2020-04-08)
- Fix bug where producers and consumers would be unable to get partition information after a reconnect.

## 5.5.0 (2020-02-15)
### New features
- Use `BinaryData` in `EventData`.
- Expose `customEndpointAddress` to support connecting to an intermediary before Azure Event
  Hubs in both `EventHubsClientBuilder` and `EventProcessorClientBuilder`

### Dependency Updates
- Update `azure-core` dependency to `1.13.0`.
- Update `azure-core-amqp` dependency to `2.0.2`.

## 5.0.2 (2020-02-13)
- Fix bug where producers and consumers would not be able to block in their consuming code.

## 5.0.1 (2020-02-11)
- Add support for different error handling cases in EventProcessor.
- Recreate connection in client on transient errors.
- Add tracing links when sending a batch of events.
- Tracing link names are shortened when sending events.
- EventPosition.fromOffset(long) is no longer inclusive.

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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Feventhubs%2Fazure-messaging-eventhubs%2FCHANGELOG.png)
