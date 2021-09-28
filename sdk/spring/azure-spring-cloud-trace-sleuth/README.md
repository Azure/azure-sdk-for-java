# Spring Cloud for Azure Sleuth Trace plugin library for Java
This package provides the common framework for all Azure Spring libraries.

[Source code][src] | [Package (Maven)][package] | [API reference documentation][refdocs]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-trace-sleuth;current})
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-cloud-trace-sleuth</artifactId>
    <version>4.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
Azure SDK service implementation follows the `com.azure:azure-core` interfaces to trace service activities. [azure-core-tracing-opentelemetry](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-tracing-opentelemetry) is one implementation based on `Open Telemetry API`, this is not suitable for Spring developers based on `Spring Cloud Sleuth API`.

This module will depend on `Spring Cloud Sleuth API` to implement the azure core interface(`HttpPipelinePolicy`). For HTTP protocol based service, it can add the http policy `com.azure.spring.tracing.sleuth.SleuthHttpPolicy` to enable Azure SDK service tracing and delegate to Spring Sleuth tracing context, then related span can be associated with the same trace id, which propagated from Spring MVC layer. This feature will be automatically enabled if Spring Sleuth is enabled. 

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
[src]: https://github.com/Azure/azure-sdk-for-java/tree/1633e7abbcf14de6d9d504a3c88b5c66e7aab360/sdk/spring/azure-spring-cloud-context/src
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-context
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-context
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
