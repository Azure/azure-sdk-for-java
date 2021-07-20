# Azure Service Bus Spring Cloud Stream Binder core client library for Java

The project provides core functionality of **Spring Cloud Stream Binder for Azure Service Bus** which allows you to build message-driven 
microservice using **Spring Cloud Stream** based on [Azure Service Bus][azure_service_bus].

[Source code][src_code] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-cloud-dependencies].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-cloud-dependencies`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-cloud-stream-binder-servicebus-core</artifactId>
</dependency>
```

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
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-with-service-bus
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-cloud-azure-servicebus-stream-binder-core
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-stream-binder-servicebus-core
[spring_boot_logging]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[service_bus_queue_binder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-queue
[service_bus_topic_binder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-topic
[spring_cloud_stream_binder_service_bus_multiple_binders]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-cloud-sample-servicebus-queue-multibinders
[spring_cloud_stream_binder_service_bus_queue]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-cloud-sample-servicebus-queue-binder
[spring_cloud_stream_binder_service_bus_topic]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-cloud-sample-servicebus-topic-binder
[spring_integration]: https://spring.io/projects/spring-integration
[src_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-stream-binder-servicebus-core
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
