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
on Kafka API, you can try [Event Hub with Kafka API Sample][kafka_sample]
#### Consumer Group

Event Hub provides similar support of consumer group as Apache Kafka, but with slight different logic. While Kafka 
stores all committed offsets in the broker, you have to store offsets of event hub messages 
being processed manually. Event Hub SDK provide the function to store such offsets inside Azure Storage Account. So 
that's why you have to fill `spring.cloud.stream.eventhub.checkpoint-storage-account`.

#### Partitioning Support

Event Hubs provides a similar concept of physical partition as Kafka. But unlike Kafka's auto rebalancing between consumers and partitions, Event Hub provides a kind of preemptive mode. The storage account acts as a lease to determine which partition is owned by which consumer. When a new consumer starts, it will try to steal some partitions
from most heavy-loaded consumers to achieve the workload balancing.

#### Batch Consumer Support
Azure Event Hubs Spring Cloud Stream Binder supports [Spring Cloud Stream Batch Consumer feature][spring-cloud-stream-batch0-consumer]. 

When enabled, an **org.springframework.messaging.Message** of which the payload is a list of batched events will be received and passed to the consumer function. Each message header is also converted as a list, of which the content is the associated header value parsed from each event. For the communal headers of **com.azure.spring.integration.core.AzureHeaders#RAW_PARTITION_ID** and **com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER**, they are presented as a single value for the entire batch of events share the same one. Note, the checkpoint header only exists when **MANUAL** checkpoint mode is used.

Checkpointing of batch consumer supports two modes: BATCH and MANUAL. BATCH mode is an auto checkpointing mode to checkpoint the entire batch of events together once they are received by the binder. MANUAL mode is to checkpoint the events by users. When used, the 
**com.azure.spring.integration.core.api.reactor.Checkpointer** will be passes into the message header, and users could use it to do checkpointing.

The batch size can be specified by properties of `max-batch-size` and `max-wait-time` with prefix as `spring.cloud.stream.bindings.<binding-name>.consumer.`. See the below section for more information about the [configuration](#batch-consumer) and [examples](#batch-consumer-sample).

## Examples 

Please use this [sample][sample] as a reference for how to use this binder. 

### Configuration Options 

The binder provides the following configuration options in `application.properties`.

#### Spring Cloud Azure Properties ####

|Name | Description | Required | Default 
|:---|:---|:---|:---
spring.cloud.azure.auto-create-resources | If enable auto-creation for Azure resources |  | false
spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes if spring.cloud.azure.auto-create-resources is enabled. |
spring.cloud.azure.environment | Azure Cloud name for Azure resources, supported values are  `azure`, `azurechina`, `azure_germany` and `azureusgovernment` which are case insensitive | |azure | 
spring.cloud.azure.client-id | Client (application) id of a service principal or Managed Service Identity (MSI) | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.client-secret | Client secret of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.msi-enabled | If enable MSI as credential configuration | Yes if MSI is used as credential configuration. | false
spring.cloud.azure.resource-group | Name of Azure resource group | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.subscription-id | Subscription id of an MSI | Yes if MSI is used as credential configuration. |
spring.cloud.azure.tenant-id | Tenant id of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.eventhub.connection-string | Event Hubs Namespace connection string | Yes if connection string is used as Event Hubs credential configuration |
spring.cloud.azure.eventhub.checkpoint-storage-account | StorageAccount name for message checkpoint | Yes
spring.cloud.azure.eventhub.checkpoint-access-key | StorageAccount access key for message checkpoint | Yes if StorageAccount access key is used as StorageAccount credential configuration
spring.cloud.azure.eventhub.checkpoint-container | StorageAccount container name for message checkpoint | Yes
spring.cloud.azure.eventhub.namespace | Event Hub Namespace. Auto creating if missing | Yes if service principal or MSI is used as credential configuration. |

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

Default: 1

**_partition-key-extractor-name_**

The name of the bean that implements `PartitionKeyExtractorStrategy`. 
The partition handler will first use the `PartitionKeyExtractorStrategy#extractKey` method to obtain the partition key value.

Default: null

**_partition-key-expression_**

A SpEL expression that determines how to partition outbound data. 
When interface `PartitionKeyExtractorStrategy` is not implemented, it will be called in the method `PartitionHandler#extractKey`.

Default: null

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

#### Common Consumer Properties ####

You can use the below consumer configurations of **Spring Cloud Stream**, 
it uses the configuration with the format of `spring.cloud.stream.bindings.<channelName>.consumer`.

##### Batch Consumer

When `spring.cloud.stream.binding.<name>.consumer.batch-mode` is set to `true`, all of the received events will be presented as a `List<?>` to the consumer function. Otherwise, the function will be called with one event at a time. The size of the batch is controlled by Event Hubs consumer properties `max-batch-size`(required) and `max-batch-duration`(optional); refer to the [below section](#event-hub-consumer-properties) for more information.

**_batch-mode_**

Whether to enable the entire batch of messages to be passed to the consumer function in a `List`.

Default: `False`

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

**_max-batch-size_**

The maximum number of events that will be in the list of a message payload when the consumer callback is invoked.

Default: `10`

**_max-wait-time_**

The max time `Duration` to wait to receive a batch of events upto the max batch size before invoking the consumer callback.

Default: `null`

For configuration of a `Duration` property, please refer to [the offical Spring Boot documentation][spring-boot-converting-duration]. Note: when setting a `long` value, the unit of milliseconds is used.

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

### Batch Consumer Sample

#### Configuration Options
To enable the batch consumer mode, you should add below configuration
```yaml
spring:
  cloud:
    stream:
      bindings:
        consume-in-0:
          destination: {event-hub-name}
          group: [consumer-group-name]
          consumer:
            batch-mode: true 
      eventhub:
        bindings:
          consume-in-0:
            consumer:
              checkpoint-mode: BATCH # or MANUAL as needed
              max-batch-size: 2 # The default valueis 10
              max-wait-time: 1m # Optional, the default value is null
```

#### Consume messages in batches
For checkpointing mode as BATCH, you can use below code to send messages and consume in batches.
```java
    @Bean
    public Consumer<List<String>> consume() {
        return list -> list.forEach(event -> LOGGER.info("New event received: '{}'",event));
    }

    @Bean
    public Supplier<Message<String>> supply() {
        return () -> {
            LOGGER.info("Sending message, sequence " + i);
            return MessageBuilder.withPayload("\"test"+ i++ +"\"").build();
        };
    }
```

For checkpointing mode as MANUAL, you can use below code to send messages and consume/checkpoint in batches.
```java
    @Bean
    public Consumer<Message<List<String>>> consume() {
        return message -> {
            for (int i = 0; i < message.getPayload().size(); i++) {
                LOGGER.info("New message received: '{}', partition key: {}, sequence number: {}, offset: {}, enqueued time: {}",
                    message.getPayload().get(i),
                    ((List<Object>) message.getHeaders().get(EventHubHeaders.PARTITION_KEY)).get(i),
                    ((List<Object>) message.getHeaders().get(EventHubHeaders.SEQUENCE_NUMBER)).get(i),
                    ((List<Object>) message.getHeaders().get(EventHubHeaders.OFFSET)).get(i),
                    ((List<Object>) message.getHeaders().get(EventHubHeaders.ENQUEUED_TIME)).get(i));
            }
        
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            checkpointer.success()
                        .doOnSuccess(success -> LOGGER.info("Message '{}' successfully checkpointed", message.getPayload()))
                        .doOnError(error -> LOGGER.error("Exception found", error))
                        .subscribe();
        };
    }

    @Bean
    public Supplier<Message<String>> supply() {
        return () -> {
            LOGGER.info("Sending message, sequence " + i);
            return MessageBuilder.withPayload("\"test"+ i++ +"\"").build();
        };
    }
```
## Troubleshooting
### Logging setting
Please refer to [spring logging document] to get more information about logging.

#### Logging setting examples
- Example: Setting logging level of hibernate
```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

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
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/azure-spring-cloud-stream-binder-eventhubs
[spring logging document]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[eventhubs_multibinders_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/azure-spring-cloud-stream-binder-eventhubs/eventhubs-multibinders
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[azure_event_hub]: https://azure.microsoft.com/services/event-hubs/
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[spring_cloud_stream_current_producer_properties]: https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_producer_properties
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
[kafka_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/azure-spring-cloud-starter-eventhubs-kafka/eventhubs-kafka
[spring-boot-converting-duration]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.conversion.durations
[spring-cloud-stream-batch0-consumer]: https://docs.spring.io/spring-cloud-stream/docs/3.1.4/reference/html/spring-cloud-stream.html#_batch_consumers
