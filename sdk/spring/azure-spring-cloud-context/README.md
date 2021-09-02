# Spring Cloud for Azure context client library for Java
This package helps developers to finish the auto-configuration of Azure Context.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-context;current})
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-cloud-context</artifactId>
    <version>2.8.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
Azure contexts represent your active subscription to run commands against, and the authentication information needed to connect to an Azure cloud. With Azure contexts, Spring Cloud for Azure services doesn't need to reauthenticate your account each time you switch subscriptions.
## Examples

## Troubleshooting
### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc][logging_doc].

### Enable client logging
Please refer to [this page about configure logging in the Azure SDK for Java][logging] to get more information.

## Next steps


## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-cloud-context/src
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-context
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-context
[logging]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
