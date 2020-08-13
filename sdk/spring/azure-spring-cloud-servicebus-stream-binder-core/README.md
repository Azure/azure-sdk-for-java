# Azure Spring Cloud Service Bus Queue Stream Binder client library for Java

The project provides **Spring Cloud Stream Binder for Azure Service Bus Queue** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Service Bus Queue](https://azure.microsoft.com/en-us/services/service-bus/) service.

## Key concepts

### Service Bus Queue Binder Overview

The Spring Cloud Stream Binder for Azure Service Bus Queue provides the binding implementation for the Spring Cloud Stream.
This implementation uses Spring Integration Service Bus Queue Channel Adapters at its foundation. 

#### Consumer Group

This binder has no consumer group support since all consumers share one queue.

#### Partitioning Support

This binder has no partition support even service bus queue supports partition.

## Getting started

## Examples

Please use this `sample` as a reference for how to use this binder in your projects. 

### Feature List 

- [Dependency Management](#dependency-management)
- [Configuration Options](#configuration-options)

#### Dependency Management

**Maven Coordinates** 
```
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-servicebus-queue-stream-binder</artifactId>
</dependency>

```
**Gradle Coordinates** 
```
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-servicebus-queue-stream-binder'
}
```

#### Configuration Options 

The binder provides the following configuration options in `application.properties`.

##### Spring Cloud Azure Properties ####

Name | Description | Required | Default 
---|---|---|---
spring.cloud.azure.credential-file-path | Location of azure credential file | Yes |
spring.cloud.azure.resource-group | Name of Azure resource group | Yes |
spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
spring.cloud.azure.servicebus.namespace | Service Bus Namespace. Auto creating if missing | Yes |

##### Serivce Bus Queue Producer Properties #####

It supports the following configurations with the format of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.producer`.

**_sync_**

Whether the producer should act in a synchronous manner with respect to writing records into a stream. If true, the 
producer will wait for a response after a send operation.

Default: `false`

**_sendTimeout_**

Effective only if `sync` is set to true. The amount of time to wait for a response after a send operation, in milliseconds.

Default: `10000`
 
##### Service Bus Queue Consumer Properties #####

It supports the following configurations with the format of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.consumer`.

**_checkpointMode_**

The mode in which checkpoints are updated.
If `RECORD`, checkpoints occur after each record is received by Spring Channel.
If `MANUAL`, checkpoints occur on demand by the user via the `Checkpointer`. You can get `Checkpointer` by `Message.getHeaders.get(AzureHeaders.CHECKPOINTER)`callback.

Default: `RECORD`

## Troubleshooting
## Next steps
## Contributing
