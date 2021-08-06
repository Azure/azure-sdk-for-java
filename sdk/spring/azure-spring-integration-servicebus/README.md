# Azure Service Bus Spring Integration client library for Java

The Spring Integration for Azure Service Bus extension project provides inbound and outbound channel adapters for Azure Service Bus. 
Microsoft Azure Service Bus is a fully managed enterprise integration message broker. Service Bus can decouple applications and services. 
Service Bus offers a reliable and secure platform for asynchronous transfer of data and state. 

[Source code][src_code] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-cloud-dependencies].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-cloud-dependencies`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-integration-servicebus</artifactId>
</dependency>
```

## Key concepts
[Spring Integration][spring_integration] enables lightweight messaging within Spring-based applications and supports integration with external systems via declarative adapters.

This project provides inbound and outbound channel adapters for Azure Service Bus.

### Configure ServiceBusMessageConverter to customize ObjectMapper
`ServiceBusMessageConverter` is made as a configurable bean to allow users to customized `ObjectMapper`.

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
- [Spring Integration with Service Bus Sample][spring_integration_sample_with_service_bus]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Links -->
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-integration-servicebus
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#spring-integration-servicebus
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-integration-sample-servicebus
[spring_boot_logging]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[spring_integration]: https://spring.io/projects/spring-integration
[spring_integration_sample_with_service_bus]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-integration-sample-servicebus
[src_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-integration-servicebus
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
