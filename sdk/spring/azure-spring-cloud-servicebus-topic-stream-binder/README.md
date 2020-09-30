# Azure Service Bus Topic Spring Cloud Stream Binder client library for Java

The project provides **Spring Cloud Stream Binder for Azure Service Bus Topic** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Service Bus Topic][azure_service_bus].

[Source code][src_code] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven][maven] 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.microsoft.azure:spring-cloud-azure-servicebus-topic-stream-binder;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-servicebus-topic-stream-binder</artifactId>
    <version>1.2.8</version>
</dependency>
```
[//]: # ({x-version-update-end})

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

Name | Description | Required | Default 
---|---|---|---
spring.cloud.azure.credential-file-path | Location of azure credential file | Yes |
spring.cloud.azure.resource-group | Name of Azure resource group | Yes |
spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
spring.cloud.azure.servicebus.namespace | Service Bus Namespace. Auto creating if missing | Yes |

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

**_concurrency_**

Controls the max concurrent calls of service bus message handler and the size of fixed thread pool that handles user's business logic

Default: `1`

**_sessionsEnabled_**

Controls if is a session aware consumer. Set it to `true` if is a topic with sessions enabled.

Default: `false`

## Examples 

Please use this `sample` as a reference
for how to use this binder in your projects. 
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
[azure_subscription]: https://azure.microsoft.com/free
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/CONTRIBUTING.md
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-with-service-bus
[maven]: http://maven.apache.org
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-cloud-azure-servicebus-topic-stream-binder
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#spring-cloud-azure-servicebus-topic-stream-binder
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-servicebus-topic-binder-sample
[spring_boot_logging]: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging
[service_bus_queue_binder]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-servicebus-queue-stream-binder
[service_bus_topic_binder]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-servicebus-topic-stream-binder
[spring_cloud_stream_binder_service_bus_multiple_binders]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-servicebus-queue-multibinders-sample
[spring_cloud_stream_binder_service_bus_queue]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-servicebus-queue-binder-sample
[spring_cloud_stream_binder_service_bus_topic]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-servicebus-topic-binder-sample
[spring_integration]: https://spring.io/projects/spring-integration
[src_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-servicebus-topic-stream-binder
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
