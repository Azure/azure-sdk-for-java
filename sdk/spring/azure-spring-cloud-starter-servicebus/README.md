# Azure Service Bus Spring Cloud starter client library for Java

The Spring Cloud Service Bus starter helps developers to finish the auto-configuration of Service Bus and provides Spring Integration with Service Bus.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-cloud-dependencies].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-cloud-dependencies`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-cloud-starter-servicebus</artifactId>
</dependency>
```

## Key concepts
[Spring Integration][spring_integration] enables lightweight messaging within Spring-based applications and supports integration with external systems via declarative adapters.

This project provides Spring Integration adaption with Azure Service Bus and the ability to auto-configure connection to Azure Service Bus.

### Configure ServiceBusMessageConverter to customize ObjectMapper
`ServiceBusMessageConverter` is made as a configurable bean to allow users to customized `ObjectMapper`.

### Support for Service Bus Message Headers and Properties
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
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME | OffsetDateTime | 1
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.core.AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE | Integer | 2
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
- [Spring Integration with Service Bus Sample][spring_cloud_starter_sample_with_service_bus]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Links -->
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-starter-servicebus
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-autoconfigure
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-integration-sample-servicebus
[spring_boot_logging]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[spring_integration]: https://spring.io/projects/spring-integration
[spring_cloud_starter_sample_with_service_bus]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/servicebus/azure-spring-integration-sample-servicebus
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
