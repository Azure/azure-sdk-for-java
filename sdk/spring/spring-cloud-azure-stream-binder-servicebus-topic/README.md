# Azure Service Bus Topic Spring Cloud Stream Binder client library for Java

The project provides **Spring Cloud Stream Binder for Azure Service Bus Topic** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Service Bus Topic][azure_service_bus].

[Source code][src_code] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-cloud-dependencies].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-cloud-dependencies`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>spring-cloud-azure-stream-binder-servicebus-topic</artifactId>
</dependency>
```

## Key concepts

The Spring Cloud Stream Binder for Azure Service Bus Topic provides the binding implementation for the Spring Cloud Stream.

This implementation uses [Spring Integration][spring_integration] Service Bus Topic Channel Adapters at its foundation. 

### Scheduled Message
This binder supports submitting messages to a topic for delayed processing. Users can send scheduled messages with header `x-delay` 
expressing in milliseconds a delay time for the message. The message will be delivered to the respective topics after `x-delay` milliseconds. 

### Consumer Group

Service Bus Topic provides similar support of consumer group as Apache Kafka, but with slight different logic.
This binder rely on `Subscription` of a topic to act as a consumer group.

### Partitioning Support

This binder implementation has no partition support even service bus topic supports partition.

### Configuration Options 

The binder provides the following configuration options:

##### Spring Cloud Azure Properties

|Name | Description | Required | Default
|:---|:---|:---|:---
spring.cloud.azure.auto-create-resources | If enable auto-creation for Azure resources |  | false
spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes if spring.cloud.azure.auto-create-resources is enabled. |
spring.cloud.azure.environment | Azure Cloud name for Azure resources, supported values are `azure`, `azurechina`, `azure_germany` and `azureusgovernment` which are case insensitive | |azure | 
spring.cloud.azure.client-id | Client (application) id of a service principal or Managed Service Identity (MSI) | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.client-secret | Client secret of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.msi-enabled | If enable MSI as credential configuration | Yes if MSI is used as credential configuration. | false
spring.cloud.azure.resource-group | Name of Azure resource group | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.subscription-id | Subscription id of an MSI | Yes if MSI is used as credential configuration. |
spring.cloud.azure.tenant-id | Tenant id of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.servicebus.connection-string | Service Bus Namespace connection string | Yes if connection string is used as credential configuration |
spring.cloud.azure.servicebus.namespace | Service Bus Namespace. Auto creating if missing | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.servicebus.transportType | Service Bus transportType, supported value of `AMQP` and `AMQP_WEB_SOCKETS` | No | `AMQP`
spring.cloud.azure.servicebus.retry-Options | Service Bus retry options | No | Default value of AmqpRetryOptions

##### Serivce Bus Topic Producer Properties

It supports the following configurations with the format of `spring.cloud.stream.servicebus.topic.bindings.<channelName>.producer`.

**_sync_**

Whether the producer should act in a synchronous manner with respect to writing messages into a stream. If true, the 
producer will wait for a response after a send operation.

Default: `false`

**_send-timeout_**

Effective only if `sync` is set to true. The amount of time to wait for a response after a send operation, in milliseconds.

Default: `10000`
 
##### Service Bus Topic Consumer Properties

It supports the following configurations with the format of `spring.cloud.stream.servicebus.topic.bindings.<channelName>.consumer`.

**_checkpoint-mode_**

The mode in which checkpoints are updated.

`RECORD`, checkpoints occur after each record successfully processed by user-defined message handler without any exception.

`MANUAL`, checkpoints occur on demand by the user via the `Checkpointer`. You can get `Checkpointer` by `Message.getHeaders.get(AzureHeaders.CHECKPOINTER)`callback.

Default: `RECORD`

**_prefetch-count_**

Prefetch count of underlying service bus client.

Default: `1`

**_maxConcurrentCalls_**

Controls the max concurrent calls of service bus message handler and the size of fixed thread pool that handles user's business logic

Default: `1`

**_maxConcurrentSessions_**

Controls the maximum number of concurrent sessions to process at any given time.

Default: `1`

**_concurrency_**

When `sessionsEnabled` is true, controls the maximum number of concurrent sessions to process at any given time.
When `sessionsEnabled` is false, controls the max concurrent calls of service bus message handler and the size of fixed thread pool that handles user's business logic

Deprecated, replaced with `maxConcurrentSessions` when `sessionsEnabled` is true and `maxConcurrentCalls` when `sessionsEnabled` is false

Default: `1`

**_sessionsEnabled_**

Controls if is a session aware consumer. Set it to `true` if is a topic with sessions enabled.

Default: `false`

**_receiveMode_**

The modes for receiving messages.

`PEEK_LOCK`, received message is not deleted from the queue or subscription, instead it is temporarily locked to the receiver, making it invisible to other receivers.

`RECEIVE_AND_DELETE`, received message is removed from the queue or subscription and immediately deleted.

Default: `PEEK_LOCK`

**_enableAutoComplete_**

Enable auto-complete and auto-abandon of received messages.
'enableAutoComplete' is not needed in for RECEIVE_AND_DELETE mode.

Default: `true`
##### Support for Service Bus Message Headers and Properties
The following table illustrates how Spring message headers are mapped to Service Bus message headers and properties.
When creat a message, developers can specify the header or property of a Service Bus message by below constants.

For some Service Bus headers that can be mapped to multiple Spring header constants, the priority of different Spring headers is listed.

Service Bus Message Headers and Properties | Spring Message Header Constants | Type | Priority Number (Descending priority)
---|---|---|---
**MessageId** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.MESSAGE_ID | String | 1
**MessageId** | com.azure.spring.integration.core.AzureHeaders.RAW_ID | String | 2
**MessageId** | org.springframework.messaging.MessageHeaders.ID | UUID | 3
ContentType | org.springframework.messaging.MessageHeaders.CONTENT_TYPE | String | N/A
ReplyTo | org.springframework.messaging.MessageHeaders.REPLY_CHANNEL | String | N/A
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.core.AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE | Integer | 1
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME | Instant | 2
TimeToLive | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TIME_TO_LIVE | Duration | N/A
SessionID | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SESSION_ID | String | N/A
CorrelationId | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.CORRELATION_ID | String | N/A
To | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TO | String | N/A
ReplyToSessionId | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.REPLY_TO_SESSION_ID | String | N/A
**PartitionKey** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.PARTITION_KEY | String | 1
**PartitionKey** | com.azure.spring.integration.core.AzureHeaders.PARTITION_KEY | String | 2

## Examples 
## Usage examples
**Example: Manually set the partition key for the message**

This example demonstrates how to manually set the partition key for the message in the application.

**Way 1:**
This example requires that `spring.cloud.stream.default.producer.partitionKeyExpression` be set `"'partitionKey-' + headers[<message-header-key>]"`.
```yaml
spring:
  cloud:
    azure:
      servicebus:
        connection-string: [servicebus-namespace-connection-string]
    stream:
      default:
        producer:
          partitionKeyExpression:  "'partitionKey-' + headers[<message-header-key>]"
```
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader("<message-header-key>", "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When using `application.yml` to configure the partition key, its priority will be the lowest.
> It will take effect only when the `ServiceBusMessageHeaders.SESSION_ID`, `ServiceBusMessageHeaders.PARTITION_KEY`, `AzureHeaders.PARTITION_KEY` are not configured.

**Way 2:**
Manually add the partition Key in the message header by code.

*Recommended:* Use `ServiceBusMessageHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

*Not recommended but currently supported:* `AzureHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(AzureHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```
> **NOTE:** When both `ServiceBusMessageHeaders.PARTITION_KEY` and `AzureHeaders.PARTITION_KEY` are set in the message headers,
> `ServiceBusMessageHeaders.PARTITION_KEY` is preferred.

**Example: Set the session id for the message**

This example demonstrates how to manually set the session id of a message in the application.

```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.SESSION_ID, "Customize session id")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When the `ServiceBusMessageHeaders.SESSION_ID` is set in the message headers, and a different `ServiceBusMessageHeaders.PARTITION_KEY` (or `AzureHeaders.PARTITION_KEY`) header is also set,
> the value of the session id will eventually be used to overwrite the value of the partition key.

Please use this `sample` as a reference to learn more about how to use this binder in your project.
- [Service Bus Topic][spring_cloud_stream_binder_service_bus_topic]

## Troubleshooting

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using 
`logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. 
The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc][spring_boot_logging].

## Next steps
The following section provides sample projects illustrating how to use the starter in different cases.

### More sample code
- [Service Bus Queue][spring_cloud_stream_binder_service_bus_queue]
- [Service Bus Multiple Binders][spring_cloud_stream_binder_service_bus_multiple_binders]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Links -->
[azure_service_bus]: https://azure.microsoft.com/services/service-bus/
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-with-service-bus
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-stream-binder-servicebus-topic
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-stream-binder-servicebus-topic
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-topic/servicebus-topic-binder
[spring_boot_logging]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[service_bus_queue_binder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-queue
[service_bus_topic_binder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-topic
[spring_cloud_stream_binder_service_bus_multiple_binders]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue/servicebus-queue-multibinders
[spring_cloud_stream_binder_service_bus_queue]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue
[spring_cloud_stream_binder_service_bus_topic]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-topic/servicebus-topic-binder
[spring_integration]: https://spring.io/projects/spring-integration
[src_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-topic
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
