# Azure Event Hubs Spring Cloud Stream Binder client library for Java

The project provides **Spring Cloud Stream Binder for Azure Event Hub** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Event Hub][azure_event_hub] service.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-cloud-dependencies].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-cloud-dependencies`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-cloud-stream-binder-eventhubs</artifactId>
</dependency>
```

## Key concepts

### EventHub Binder Overview

The Spring Cloud Stream Binder for Azure Event Hub provides the binding implementation for the Spring Cloud Stream.
This implementation uses Spring Integration Event Hub Channel Adapters at its foundation. From design's perspective, 
Event Hub is similar as Kafka. Also, Event Hub could be accessed via Kafka API. If your project has tight dependency 
on Kafka API, you can try `Event Hub with Kafka API Sample`
#### Consumer Group

Event Hub provides similar support of consumer group as Apache Kafka, but with slight different logic. While Kafka 
stores all committed offsets in the broker, you have to store offsets of event hub messages 
being processed manually. Event Hub SDK provide the function to store such offsets inside Azure Storage Account. So 
that's why you have to fill `spring.cloud.stream.eventhub.checkpoint-storage-account`.

#### Partitioning Support

Event Hub provides a similar concept of physical partition as Kafka. But unlike Kafka's auto rebalancing between consumers and partitions, Event Hub provides a kind of preemptive mode. The storage account acts as a lease to determine which partition is owned by which consumer. When a new consumer starts, it will try to steal some partitions
from most heavy-loaded consumers to achieve the workload balancing.

## Examples 

Please use this `sample` as a reference for how to use this binder. 

### Configuration Options 

The binder provides the following configuration options in `application.properties`.

#### Spring Cloud Azure Properties ####

Name | Description | Required | Default 
---|---|---|---
 spring.cloud.azure.resource-group | Name of Azure resource group | Yes |
 spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
 spring.cloud.azure.eventhub.namespace | Event Hub Namespace. Auto creating if missing | Yes |
 spring.cloud.azure.eventhub.checkpoint-storage-account | StorageAccount name for checkpoint message successfully consumed | Yes

#### Common Producer Properties ####

You can use the producer configurations of **Spring Cloud Stream**, 
it uses the configuration with the format of `spring.cloud.stream.bindings.<channelName>.producer`.

##### Partition configuration

The system will obtain the parameter `PartitionSupply` to send the message, 
the following is the process of obtaining the priority of the partition ID and key:

![Create PartitionSupply parameter process](resource/create-partition-supply-process.png)

The following are configuration items related to the producer:

**_partition-count_**

The number of target partitions for the data, if partitioning is enabled.

Default: `1`

**_partition-key-extractor-name_**

The name of the bean that implements `PartitionKeyExtractorStrategy`. 
The partition handler will first use the `PartitionKeyExtractorStrategy#extractKey` method to obtain the partition key value.

Default: `null`

**_partition-key-expression_**

A SpEL expression that determines how to partition outbound data. 
When interface `PartitionKeyExtractorStrategy` is not implemented, it will be called in the method `PartitionHandler#extractKey`.

Default: `null`

For more information about setting partition for the producer properties, please refer to the [Producer Properties of Spring Cloud Stream][spring_cloud_stream_current_producer_properties].

#### Event Hub Producer Properties ####

It supports the following configurations with the format of `spring.cloud.stream.eventhub.bindings.<channelName>.producer`.
 
**_sync_**

Whether the producer should act in a synchronous manner with respect to writing messages into a stream. If true, the 
producer will wait for a response from Event Hub after a send operation.

Default: `false`

**_send-timeout_**

Effective only if `sync` is set to true. The amount of time to wait for a response from Event Hub after a send operation, in milliseconds.

Default: `10000`

**_custom-endpoint-address_**

Sets a custom endpoint address when connecting to the Event Hubs service. This can be useful when your network does not allow connecting to the standard Azure Event Hubs endpoint address, but does allow connecting through an intermediary.

Default: `null`

**_prefetch-count_**

Sets the count used by the receiver to control the number of events the Event Hub consumer will actively receive and queue locally without regard to whether a receive operation is currently active.

Default: `1`

**_share-connection_**

Toggles the builder to use the same connection for producers or consumers that are built from this instance. By default, a new connection is constructed and used created for each Event Hub consumer or producer created.

Default: `false`

**_retry-options.max-retries_**

Sets the maximum number of retry attempts before considering the associated operation to have failed.

Default: `3`

**_retry-options.delay_**

Gets the delay between retry attempts for a fixed approach or the delay on which to base calculations for a backoff-approach.

Default: `0.8s`

**_retry-options.max-delay_**

Sets the maximum permissible delay between retry attempts.

Default: `1m`

**_retry-options.try-timeout_**

Sets the maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.

Default: `1m`

**_retry-options.try-timeout_**

Sets the approach to use for calculating retry delays.

`FIXED`, Retry attempts happen at fixed intervals; each delay is a consistent duration.

`EXPONENTIAL`, Retry attempts will delay based on a backoff strategy, where each attempt will increase the duration that it waits before retrying.

Default: `AmqpRetryMode.EXPONENTIAL`

**_transport_**

Sets the transport type by which all the communication with Azure Event Hubs occurs.

`AMQP`, AMQP over TCP. Uses port 5671 - assigned by IANA for secure AMQP (AMQPS).

`AMQP_WEB_SOCKETS`, AMQP over Web Sockets. Uses port 443.

Default: `AmqpTransportType.AMQP`

#### Event Hub Consumer Properties ####

It supports the following configurations with the format of `spring.cloud.stream.eventhub.bindings.<channelName>.consumer`.

**_start-position_**

Whether the consumer receives messages from the beginning or end of event hub. if `EARLIEST`, from beginning. If 
`LATEST`, from end.

Default: `LATEST`

**_checkpoint-mode_**

The mode in which checkpoints are updated.

`RECORD`, checkpoints occur after each record is successfully processed by user-defined message handler without any exception. If you use `StorageAccount` as checkpoint store, this might become botterneck. 

`BATCH`, checkpoints occur after each batch of messages successfully processed by user-defined message handler without any exception. `default` mode. You may experience reprocessing at most one batch of messages when message processing fails. Be aware that batch size could be any value.

`MANUAL`, checkpoints occur on demand by the user via the `Checkpointer`. You can do checkpoints after the message has been successfully processed. `Message.getHeaders.get(AzureHeaders.CHECKPOINTER)`callback can get you the `Checkpointer` you need. Please be aware all messages in the corresponding Event Hub partition before this message will be considered as successfully processed.

`PARTITION_COUNT`, checkpoints occur after the count of messages defined by `checkpoint_count` successfully processed for each partition. You may experience reprocessing at most `checkpoint_count` of  when message processing fails.

`Time`, checkpoints occur at fixed time inerval specified by `checkpoint_interval`. You may experience reprocessing of messages during this time interval when message processing fails.

Default: `BATCH`

**_checkpoint-count_**

Effectively only when `checkpoint-mode` is `PARTITION_COUNT`. Decides the amount of message for each partition to do one checkpoint.

Default: `10`

**_checkpoint-interval_**

Effectively only when `checkpoint-mode` is `Time`. Decides The time interval to do one checkpoint.

Default: `5s`

**_custom-endpoint-address_**

Sets a custom endpoint address when connecting to the Event Hubs service. This can be useful when your network does not allow connecting to the standard Azure Event Hubs endpoint address, but does allow connecting through an intermediary.

Default: `null`

**_prefetch-count_**

Sets the count used by the receiver to control the number of events the Event Hub consumer will actively receive and queue locally without regard to whether a receive operation is currently active.

Default: `1`

**_share-connection_**

Toggles the builder to use the same connection for producers or consumers that are built from this instance. By default, a new connection is constructed and used created for each Event Hub consumer or producer created.

Default: `false`

**_retry-options.max-retries_**

Sets the maximum number of retry attempts before considering the associated operation to have failed.

Default: `3`

**_retry-options.delay_**

Gets the delay between retry attempts for a fixed approach or the delay on which to base calculations for a backoff-approach.

Default: `0.8s`

**_retry-options.max-delay_**

Sets the maximum permissible delay between retry attempts.

Default: `1m`

**_retry-options.try-timeout_**

Sets the maximum duration to wait for completion of a single attempt, whether the initial attempt or a retry.

Default: `1m`

**_retry-options.try-timeout_**

Sets the approach to use for calculating retry delays.

`FIXED`, Retry attempts happen at fixed intervals; each delay is a consistent duration.

`EXPONENTIAL`, Retry attempts will delay based on a backoff strategy, where each attempt will increase the duration that it waits before retrying.

Default: `AmqpRetryMode.EXPONENTIAL`

**_transport_**

Sets the transport type by which all the communication with Azure Event Hubs occurs.

`AMQP`, AMQP over TCP. Uses port 5671 - assigned by IANA for secure AMQP (AMQPS).

`AMQP_WEB_SOCKETS`, AMQP over Web Sockets. Uses port 443.

Default: `AmqpTransportType.AMQP`

**_loadBalancingStrategy_**

The LoadBalancingStrategy the event processor will use for claiming partition ownership. By default, a Balanced approach will be used.

`BALANCED`, The event processor will use a steady approach to claim ownership of partitions and slowly trend towards a stable state where all active processors will have an even distribution of Event Hub partitions. 
This strategy may take longer to settle into a balanced partition distribution among active processor instances. This strategy is geared towards minimizing ownership contention and reducing the need to transfer ownership frequently, especially when multiple instances are initialized together, until a stable state is reached.

`GREEDY`, The event processor will attempt to claim its fair share of partition ownership greedily. This enables event processing of all partitions to start/resume quickly when there is an imbalance detected by the processor. 
This may result in ownership of partitions frequently changing when multiple instances are starting up but will eventually converge to a stable state.

Default: `LoadBalancingStrategy.BALANCED`

**_loadBalancingUpdateInterval_**

The time interval between load balancing update cycles. This is also generally the interval at which ownership of partitions are renewed. By default, this interval is set to 10 seconds.

Default: `10s`

**_partitionOwnershipExpirationInterval_**

The time duration after which the ownership of partition expires if it's not renewed by the owning processor instance. This is the duration that this processor instance will wait before taking over the ownership of partitions previously owned by an inactive processor. By default, this duration is set to a minute.

Default: `1m`

**_trackLastEnqueuedEventProperties_**

Sets whether or not the event processor should request information on the last enqueued event on its associated partition, and track that information as events are received.

Default: `false`

### Error Channels
**_consumer error channel_**

this channel is open by default, you can handle the error message in this way:
```
    // Replace destination with spring.cloud.stream.bindings.input.destination
    // Replace group with spring.cloud.stream.bindings.input.group
    @ServiceActivator(inputChannel = "{destination}.{group}.errors")
    public void consumerError(Message<?> message) {
        LOGGER.error("Handling customer ERROR: " + message);
    }
```

**_producer error channel_**

this channel is not open by default, if you want to open it. You need to add a configuration in your application.properties, like this:
```
spring.cloud.stream.default.producer.errorChannelEnabled=true
```

you can handle the error message in this way:
```
    // Replace destination with spring.cloud.stream.bindings.output.destination
    @ServiceActivator(inputChannel = "{destination}.errors")
    public void producerError(Message<?> message) {
        LOGGER.error("Handling Producer ERROR: " + message);
    }
```

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc][logging_doc].
 

## Next steps

The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Eventhubs Binder Sample][sample]
- [Eventhubs Multibinders Sample][eventhubs_multibinders_sample]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-stream-binder-eventhubs/src
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-stream-binder-eventhubs
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-stream-binder-eventhubs
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-azure-event-hub
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/eventhubs/azure-spring-cloud-sample-eventhubs-binder
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[eventhubs_multibinders_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/eventhubs/azure-spring-cloud-sample-eventhubs-multibinders
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[azure_event_hub]: https://azure.microsoft.com/services/event-hubs/
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[spring_cloud_stream_current_producer_properties]: https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_producer_properties
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
