# Azure Event Hubs Track 2 Performance test client library for Java

Represents performance tests for newer Event Hubs client.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- Microsoft Azure subscription
    - You can create a free account at: [https://azure.microsoft.com](https://azure.microsoft.com)
- Azure Event Hubs instance
    - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]

### Create the jar package

Execute: `mvn package -pl com.azure:azure-messaging-eventhubs-track2-perf`

## Key concepts

## Examples

Executing the jar without any command-line arguments will display a help menu with all the available options and their
required parameters.

### Sending EventData

The command below runs the scenario for sending messages.

- (REQUIRED) `-cs` is the connection string for the Event Hubs namespace.
- (REQUIRED) `-n` is the name of the Event Hub.
- `-c` is the number of EventData to send, 500 events.
- `-d` is the test duration, 60 seconds.
- `--warmup` is the warm-up time, 0 seconds.

```bash
java -jar azure-messaging-eventhubs-track2-perf-1.0.0-beta.1-jar-with-dependencies.jar \
sendeventdata -cs "<<event-hubs-namespace-connection-string>>" -n "<<event-hub-name>>" \
-c 500 -d 60 --warmup 0
```

### Receive using EventHubConsumerAsyncClient

The command below runs the scenario for receiving messages from a single partition. This uses the low level receiver.

- (REQUIRED) `-cs` is the connection string for the Event Hubs namespace.
- (REQUIRED) `-n` is the name of the Event Hub.
- (REQUIRED) `--partitionId` is the partition to receive events from.
- `--consumerGroup` is the consumer group to use when receiving messages. If none is specified the default, "$DEFAULT"
  is used.
- `--count` is the number of EventData to receive, 100.
- `--duration` is the test duration, 60 seconds.
- `--warmup` is the warm-up time, 0 seconds.

```bash
java -jar azure-messaging-eventhubs-track2-perf-1.0.0-beta.1-jar-with-dependencies.jar \
receiveevents -cs "<<event-hubs-namespace-connection-string>>" -n "<<event-hub-name>>" \
--partitionId "0" --consumerGroup "<<consumer-group-name>>"  --count 100 --duration 60 --warmup 0
```

### Receive using EventProcessorClient

The command below runs the scenario for receiving messages via EventProcessorClient.

**NOTE: Assumes that there are many events in the Event Hub already.**

This test runs for `--duration` period of time, and aggregates how many events were received from each partition during
that time.  The results print out differently because we added some custom counting that is not available in the base
performance library.

- (REQUIRED) `-cs` is the connection string for the Event Hubs namespace.
- (REQUIRED) `-n` is the name of the Event Hub.
- (REQUIRED) `--storageEndpoint` is the endpoint for the storage account.
- (REQUIRED) `--storageConnectionString` is the connection string for the storage account.
- `--prefetch` is the number of credits to initially add to each partition receiver when they begin receiving, 750
  credits.
- `--duration` is the test duration, 600 seconds.
- `--warmup` is the warm-up time, 0 seconds.
- The consumer group was not specified, so the default, "$DEFAULT" is used.

```bash
java -jar azure-messaging-eventhubs-track2-perf-1.0.0-beta.1-jar-with-dependencies.jar \
eventprocessor -cs "<<event-hubs-namespace-connection-string>>" -n "<<event-hub-name>>" \
--storageEndpoint "https://my-storage.blob.core.windows.net/" \
--storageConnectionString "<<storage-account-connection-string>>" \
--prefetch 750 --duration 600 --warmup 0
```

#### Publish events before running EventProcessorClient

Events can be published to the Event Hub prior to running the EventProcessorClient test. Adding `--publish` and
`--eventsToSend <# of Events>` will publish that number of events to each partition before starting the run.

`--publish` is off by default because an Event Hub can store events for many days and receiving events starts from the
beginning of a stream.

### Receive using ReactorReceiver

This removes the cost of filtering events through EventHubConsumerAsyncClient and EventProcessorClient.

- (REQUIRED) `-cs` is the connection string for the Event Hubs namespace.
- (REQUIRED) `-n` is the name of the Event Hub.
- (REQUIRED) `--partitionId` is the partition to receive events from.
- `--consumerGroup` is the consumer group to use when receiving messages. If none is specified the default, "$DEFAULT"
  is used.
- `--count` is the number of EventData to receive, 1500.
- `--prefetch` is the number of credits to initially add to each partition receiver when they begin receiving, 250
  credits.
- `--credits` is the number of credits to add **after** the "prefetch" credits have been consumed. In this case,
  continue to add 500 credits after the initial 250 are consumed.
- `--duration` is the test duration, 60 seconds.
- `--warmup` is the warm-up time, 0 seconds.

```bash
java -jar azure-messaging-eventhubs-track2-perf-1.0.0-beta.1-jar-with-dependencies.jar \
reactorreceiveevents -cs "<<event-hubs-namespace-connection-string>>" -n "<<event-hub-name>>" \
--consumerGroup "my-consumer-group" --partitionId "1" --count 1500 --prefetch 250 --credits 500 \
--duration 60 --warmup 0
```

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- links -->
[event_hubs_create]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
