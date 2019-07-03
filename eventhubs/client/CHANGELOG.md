# Release History

## 5.0.0-preview.1 (2019-07-01)
Version 5.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for
.Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For release notes and more information please visit https://aka.ms/azure-sdk-preview1-java

### Added

- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Fetch Event Hub and partition metadata using `EventHubClient`.
- Publish messages to an Azure Event Hub using `EventHubPublisher`.
- Receive messages from an Azure Event Hub using `EventHubConsumer`.

### Changed

- `EventHubRuntimeInformation` is renamed `EventHubProperties`.
- `PartitionRuntimeInformation` is renamed `PartitionProperties`.
- `PartitionReceiver` is renamed `EventHubConsumer`.
- Methods for publishing events using `PartitionSender` or `EventHubClient` are consolidated into `EventHubProducer`.

### Known issues

- AMQP protocol using web sockets is not implemented.
- Proxy support is not implemented.
- Event Host Processor is not implemented.
- Creating an `EventDataBatch` is not exposed.
- Getting `ReceiverRuntimeInformation` from `EventHubConsumer` is not implemented.
- `EventHubClient` does not clean up its `EventHubPublishers` and `EventHubConsumers`. These need to be done manually.
- Creating more than two concurrent `EventHubClients` or `EventHubConsumers` does not work.