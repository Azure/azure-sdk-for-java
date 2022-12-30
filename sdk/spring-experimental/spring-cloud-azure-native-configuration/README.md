# Spring Cloud Azure Native Configuration

The Spring Cloud Azure Native Support library provides support for building Spring Boot applications using Spring Cloud Azure Starters to [GraalVM native
images](https://www.graalvm.org/22.0/reference-manual/native-image/). 
This library builds on top of [spring-native-configuration](https://mvnrepository.com/artifact/org.springframework.experimental/spring-native-configuration) and [Azure AOT GraalVM Support](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/aot), and provides Spring Cloud Azure support through [Native hints](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#native-hints).

**NOTE:** This library is a preview version and is intended to enable applications using Spring Cloud Azure Starter libraries to quickly
build and validate native images. However, this is not a stable, GA version and is not officially supported to use in production
environments.

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 11 or later.
- [Azure Subscription][azure_subscription]
- [Docker](https://docs.docker.com/installation/#installation) for [Buildpacks](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#getting-started-buildpacks-system-requirements) usage
- [GraalVM 22.0.0 - Java 11](https://www.graalvm.org/downloads/) and [Native Image](https://www.graalvm.org/22.0/reference-manual/native-image/) for [Native Build Tools](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#getting-started-native-image-system-requirements) usage

For more details, please refer to [Getting started with Spring Native](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#getting-started)
documentation.

### Include the package

[//]: # ({x-version-update-start;com.azure.spring:spring-cloud-azure-native-configuration;current})
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>spring-cloud-azure-native-configuration</artifactId>
  <version>4.0.0-beta.2</version>
</dependency>
```

This package must work with [Spring Native AOT plugin](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#aot).

## Key concepts

### AOT Generation

Spring Native provides the AOT (Ahead Of Time) generation plugins, which will perform a deep analysis of your Spring application at build-time 
to transform and optimize your application and generate the required GraalVM native configuration. The Spring AOT plugin supports some opinions 
about the source generation process, and also debugging the source generation. For more details about Spring AOT please refer to [AOT generation](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#aot) documentation.

### Native hints

Spring Native provides the native hints feature to make Spring developers use well-known Spring idioms and APIs, which can be provided 
statically using an annotated model, or programmatically by implementing one of the callback interfaces. Of course, it is also possible 
to manually configure the GraalVM configuration files. For more details about Spring AOT please refer to [Native Hints](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#native-hints) documentation.

This library provides all the necessary `NativeConfiguration` implementations for using Spring Cloud Azure Starter libraries.

## Examples

| Library Artifact ID                                     | Supported Example Projects                                                                                      |
|---------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| spring-cloud-azure-starter-appconfiguration             | [appconfiguration-client][appconfiguration-client]                                                              |
| spring-cloud-azure-starter-eventhubs                    | [eventhubs-client][eventhubs-client]                                                                            |
| spring-cloud-azure-starter-integration-eventhubs        | [storage-queue-integration][storage-queue-integration], [storage-queue-operation][storage-queue-operation]      |
| spring-cloud-azure-starter-integration-storage-queue    | [appconfiguration-client][appconfiguration-client]                                                              |
| spring-cloud-azure-starter-keyvault-secrets             | [property-source][property-source], [secret-client][secret-client]                                              |
| spring-cloud-azure-starter-storage-blob                 | [storage-blob-sample][storage-blob-sample]                                                                      |
| spring-cloud-azure-starter-storage-file-share           | [storage-file-sample][storage-file-sample]                                                                      |
| spring-cloud-azure-starter-storage-queue                | [storage-queue-client][storage-queue-client]                                                                    |

## Troubleshooting

## Next steps

The [Spring Cloud Azure Samples][azure-spring-samples] repository consists of all the above samples, you can follow [here][azure-spring-samples-on-spring-native] to build the native image.

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][spring-contributing] to build from source or contribute.

### Filing Issues

If you encounter any bug, please file an issue [here][azure-sdk-for-java-issues].

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter][gitter-spring-on-azure-img]][gitter-spring-on-azure]

### Pull Requests

Pull requests are welcome. To open your own pull request, click [here][azure-sdk-for-java-compare]. When creating a pull request, make sure you are pointing to the fork and branch that your changes were made in.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct][codeofconduct]. For more information see the [Code of Conduct FAQ][codeofconduct-faq] or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy][privacy-statement] statement to learn more.

<!-- LINKS -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/free
[spring-contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md
[azure-sdk-for-java-issues]: https://github.com/Azure/azure-sdk-for-java/issues
[gitter-spring-on-azure-img]: https://badges.gitter.im/Microsoft/spring-on-azure.svg
[gitter-spring-on-azure]: https://gitter.im/Microsoft/spring-on-azure
[azure-sdk-for-java-compare]: https://github.com/Azure/azure-sdk-for-java/compare
[codeofconduct]: https://opensource.microsoft.com/codeofconduct/faq/
[codeofconduct-faq]: https://opensource.microsoft.com/codeofconduct/faq/
[privacy-statement]: https://privacy.microsoft.com/privacystatement
[azure-spring-samples]: https://github.com/Azure-Samples/azure-spring-boot-samples
[azure-spring-samples-on-spring-native]: https://github.com/Azure-Samples/azure-spring-boot-samples#run-samples-based-on-spring-native
[azure-spring-sample-storage-blob-native]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/spring-native/storage-blob-native
[appconfiguration-client]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/appconfiguration/spring-cloud-azure-starter-appconfiguration/appconfiguration-client
[eventhubs-client]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/spring-cloud-azure-starter-eventhubs/eventhubs-client
[storage-queue-integration]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/storage/spring-cloud-azure-starter-integration-storage-queue/storage-queue-integration
[storage-queue-operation]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/storage/spring-cloud-azure-starter-integration-storage-queue/storage-queue-operation
[appconfiguration-client]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/appconfiguration/spring-cloud-azure-starter-appconfiguration/appconfiguration-client
[property-source]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/keyvault/spring-cloud-azure-starter-keyvault-secrets/property-source
[secret-client]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/keyvault/spring-cloud-azure-starter-keyvault-secrets/secret-client
[storage-blob-sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/storage/spring-cloud-azure-starter-storage-blob/storage-blob-sample
[storage-file-sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/storage/spring-cloud-azure-starter-storage-file-share/storage-file-sample
[storage-queue-client]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/storage/spring-cloud-azure-starter-storage-queue/storage-queue-client
