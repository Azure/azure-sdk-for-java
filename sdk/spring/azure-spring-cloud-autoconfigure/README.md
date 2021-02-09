# Azure Spring cloud AutoConfigure client library for Java
This package is for Spring Cloud Starters of Azure services. It helps Spring Cloud developers to adopt Azure services.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs]

## Getting started

### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven][maven] 3.0 and above

### Include the Package
To start a new project using Azure, go on [start.spring.io][spring_io] and select "Azure
Support": this will configure the project to make sure you can integrate easily with Azure service.

For instance, let's assume that you want to use Event Hubs starter, you can add the usual `spring-cloud-starter-azure-eventhubs`
dependency to your project and the Spring Cloud auto-configuration will kick-in: 

[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-starter-eventhubs;current})
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-cloud-starter-eventhubs</artifactId>
  <version>2.2.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

Note that there is no need to add a `version` as those are managed already by the project.

## Key concepts
This project provides auto-configuration for the following Azure services:

- [Azure Cache][cache]
- [Event Hubs][event_hubs]
- [Event Hubs Kafka][event_hubs_kafka]
- [Service Bus][service_bus]
- [Storage][storage]
- [Storage Queue][storage_queue]

## Examples

The following section provides sample projects illustrating how to use the Azure Spring Cloud starters.
### More sample code
- [Azure Cache][cache_sample]
- [Event Hubs][event_hubs_sample]
- [Event Hubs Kafka][event_hubs_kafka_sample]
- [Service Bus][service_bus_sample]
- [Storage Queue][storage_queue_sample]

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc][logging_doc].
 

## Next steps

The following section provides sample projects illustrating how to use the Azure Spring Cloud starters.
### More sample code
- [Azure Cache][cache_sample]
- [Event Hubs][event_hubs_sample]
- [Event Hubs Kafka][event_hubs_kafka_sample]
- [Service Bus][service_bus_sample]
- [Storage Queue][storage_queue_sample]

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-autoconfigure/src/
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-cloud-azure-autoconfigure
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-autoconfigure
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[spring_io]: https://start.spring.io
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/CONTRIBUTING.md
[maven]: https://maven.apache.org/
[cache]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-starter-cache
[event_hubs]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-starter-eventhubs
[event_hubs_kafka]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-starter-eventhubs-kafka
[service_bus]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-starter-servicebus
[storage]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-starter-storage
[storage_queue]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-starter-storage-queue
[cache_sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-cache
[event_hubs_sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-eventhubs
[event_hubs_kafka_sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-eventhubs-kafka
[service_bus_sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-servicebus
[storage_queue_sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-storage-queue
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
