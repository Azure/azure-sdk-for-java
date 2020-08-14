# Spring Cloud Azure Service Bus Topic Stream Binder client library for Java

The project provides **Spring Cloud Stream Binder for Azure Service Bus Topic** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Service Bus Topic](https://azure.microsoft.com/en-us/services/service-bus/) service.

## Key concepts

### Service Bus Topic Binder Overview

The Spring Cloud Stream Binder for Azure Service Bus Topic provides the binding implementation for the Spring Cloud Stream.
This implementation uses Spring Integration Service Bus Topic Channel Adapters at its foundation. 

#### Scheduled Message

This binder supports submitting messages to a topic for delayed processing. Users can send scheduled messages with header `x-delay` 
expressing in milliseconds a delay time for the message. The message will be delivered to the respective topics after `x-delay` milliseconds. 

#### Consumer Group

Service Bus Topic provides similar support of consumer group as Apache Kafka, but with slight different logic.
This binder rely on `Subscription` of a topic to act as a consumer group.

#### Partitioning Support

## Getting started

This binder implementation has no partition support even service bus topic supports partition.

## Examples 

Please use this `sample` as a reference
for how to use this binder in your projects. 

### Feature List 

- [Dependency Management](#dependency-management)
- [Configuration Options](#configuration-options)

#### Dependency Management

**Maven Coordinates** 
```
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-servicebus-topic-stream-binder</artifactId>
</dependency>

```
**Gradle Coordinates** 
```
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-servicebus-topic-stream-binder'
}
```

#### Configuration Options 

The binder provides the following configuration options in `application.properties`.

##### Spring Cloud Azure Properties #####

Name | Description | Required | Default 
---|---|---|---
spring.cloud.azure.credential-file-path | Location of azure credential file | Yes |
spring.cloud.azure.resource-group | Name of Azure resource group | Yes |
spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
spring.cloud.azure.servicebus.namespace | Service Bus Namespace. Auto creating if missing | Yes |

##### Serivce Bus Topic Producer Properties #####

It supports the following configurations with the format of `spring.cloud.stream.servicebus.topic.bindings.<channelName>.producer`.

**_sync_**

Whether the producer should act in a synchronous manner with respect to writing messages into a stream. If true, the 
producer will wait for a response after a send operation.

Default: `false`

**_send-timeout_**

Effective only if `sync` is set to true. The amount of time to wait for a response after a send operation, in milliseconds.

Default: `10000`
 
##### Service Bus Topic Consumer Properties #####

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

## Troubleshooting
## Next steps
## Contributing
