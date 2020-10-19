# Azure Spring Integration storage queue client library for Java
The *Spring Integration for Storage Queue* extension project provides inbound and outbound channel adapters and gateways for Azure Storage Queue.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs] | [Samples][sample]

## Getting started
### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven][maven] 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.microsoft.azure:spring-integration-storage-queue;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-integration-storage-queue</artifactId>
    <version>1.2.8</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
Azure Queue Storage is a service for storing large numbers of messages. You access messages from anywhere in the world via authenticated calls using HTTP or HTTPS. A queue message can be up to 64 KB in size. A queue may contain millions of messages, up to the total capacity limit of a storage account. Queues are commonly used to create a backlog of work to process asynchronously.
## Examples
Please refer to this [sample project][sample] illustrating how to use Storage Queue integration.

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc][logging_doc].
 

## Next steps

The following section provide a sample project illustrating how to use this package.
### More sample code
- [Storage Queue Integration Sample][sample]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-integration-storage-queue
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-integration-storage-queue
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#spring-integration-storage-queue
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-storage-queue-integration-sample
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/CONTRIBUTING.md
[maven]: http://maven.apache.org/
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
