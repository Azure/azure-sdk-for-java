# Azure Event Hubs Kafka Spring cloud starter client library for Java
This starter for Apache Kafka with Azure Event Hubs helps developers to finish the auto-configuration of Event Hubs with Kafka protocol.  

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-cloud-dependencies].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-cloud-dependencies`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-cloud-starter-eventhubs-kafka</artifactId>
</dependency>
```

## Key concepts
Event Hubs provides a Kafka endpoint that can be used by your existing Kafka based applications as an alternative to running your own Kafka cluster. Event Hubs supports Apache Kafka protocol 1.0 and later, and works with your existing Kafka applications, including MirrorMaker.

## Examples
Please refer to this [sample project][sample] illustrating how to use this starter.

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
- [Eventhubs Kafka Sample][sample]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-starter-eventhubs-kafka
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-autoconfigure
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-kafka-azure-event-hub
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/azure-spring-cloud-starter-eventhubs-kafka/eventhubs-kafka
[spring logging document]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies

