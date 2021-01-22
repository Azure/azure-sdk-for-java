# Azure Spring cloud azure context client library for Java
This package helps developers to finish the auto-configuration of Azure Context.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs]

## Getting started
### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven][maven] 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-context;current})
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-cloud-context</artifactId>
    <version>2.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
Azure contexts represent your active subscription to run commands against, and the authentication information needed to connect to an Azure cloud. With Azure contexts, Azure Spring Cloud services doesn't need to reauthenticate your account each time you switch subscriptions.
## Examples

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


## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-cloud-context/src
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-cloud-azure-context
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-context
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/CONTRIBUTING.md
[maven]: https://maven.apache.org/
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
