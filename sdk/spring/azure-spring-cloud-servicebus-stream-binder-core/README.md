# Azure Service Bus Spring Cloud Stream Binder core client library for Java

The project provides core functionality of **Spring Cloud Stream Binder for Azure Service Bus** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Service Bus][azure_service_bus].

[Source code][src_code] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs]

## Getting started

### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven][maven] 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.microsoft.azure:spring-cloud-azure-servicebus-stream-binder-core;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-servicebus-stream-binder-core</artifactId>
    <version>1.2.8</version>
</dependency>
```
[//]: # ({x-version-update-end})


## Key concepts
The Spring Cloud Stream Binder for Azure Service Bus provides the binding implementation for the Spring Cloud Stream.

This implementation uses [Spring Integration][spring_integration] Service Bus Channel Adapters at its foundation. 

Please refer to [Service Bus Queue Binder][service_bus_queue_binder] and [Service Bus Topic Binder][service_bus_topic_binder] for more details.

## Examples


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
- [Service Bus Topic][spring_cloud_stream_binder_service_bus_topic]
- [Service Bus Multiple Binders][spring_cloud_stream_binder_service_bus_multiple_binders]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[azure_service_bus]: https://azure.microsoft.com/services/service-bus/
[azure_subscription]: https://azure.microsoft.com/free
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/CONTRIBUTING.md
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-with-service-bus
[maven]: http://maven.apache.org
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-cloud-azure-servicebus-stream-binder-core
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#spring-cloud-azure-servicebus-stream-binder-core
[spring_boot_logging]: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging
[service_bus_queue_binder]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-servicebus-queue-stream-binder
[service_bus_topic_binder]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-servicebus-topic-stream-binder
[spring_cloud_stream_binder_service_bus_multiple_binders]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-servicebus-queue-multibinders-sample
[spring_cloud_stream_binder_service_bus_queue]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-servicebus-queue-binder-sample
[spring_cloud_stream_binder_service_bus_topic]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-servicebus-topic-binder-sample
[spring_integration]: https://spring.io/projects/spring-integration
[src_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-servicebus-stream-binder-core
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
