# Azure Spring Boot client library for Java

## Getting started

### Introduction

This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

### Support
* This repository supports Spring Boot 2.5.x and 2.6.x. Please read [Spring Boot Dependency Mapping](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Versions-Mapping) for more information about dependency mapping.

### Prerequisites
- JDK 1.8 and above
- [Maven](https://maven.apache.org/) 3.0 and above

## Key concepts

### Azure Spring Boot

#### Artifact IDs

| Artifact ID                                     |
| ----------------------------------------------- |
| azure-spring-boot-starter-active-directory      |
| azure-spring-boot-starter-active-directory-b2c  |
| azure-spring-boot-starter-cosmos                |
| azure-spring-boot-starter-keyvault-certificates |
| azure-spring-boot-starter-keyvault-secrets      |
| azure-spring-boot-starter-servicebus-jms        |
| azure-spring-boot-starter-storage               |

#### Choose Version According to Spring Boot Version

| Spring Boot Version | Azure Spring Boot Version |
| ------------------- | ------------------------- |
| 2.5.x               | 3.12.0                    |
| 2.6.x               | 3.12.0                    |

azure-spring-boot-starter-active-directory, azure-spring-boot-starter-storage and azure-spring-boot-starter-keyvault-secrets are also available in [Spring Initializr](https://start.spring.io/).

### Spring Cloud for Azure

[Spring Cloud](https://spring.io/projects/spring-cloud) provides boilerplate patterns for developers to quickly build and orchestrate their microservice based applications. Based on that, **Spring Cloud for Azure** is designed to provide seamless Spring integration with Azure services. Developers can adopt a Spring-idiomatic way to take advantage of services on Azure, with only few lines of configuration and minimal code changes.

#### Artifact IDs

| Artifact ID                                       |
| ------------------------------------------------- |
| azure-spring-cloud-starter-cache                  |
| azure-spring-cloud-starter-eventhubs              |
| azure-spring-cloud-starter-eventhubs-kafka        |
| azure-spring-cloud-starter-servicebus             |
| azure-spring-cloud-starter-storage-queue          |
| azure-spring-cloud-stream-binder-eventhubs        |
| azure-spring-cloud-stream-binder-servicebus-queue |
| azure-spring-cloud-stream-binder-servicebus-topic |
| azure-spring-integration-eventhubs                |
| azure-spring-integration-servicebus               |
| azure-spring-integration-storage-queue            |

#### Choose Version According to Spring Boot Version

##### Version Mapping Between Spring Cloud And Spring Boot

Please refer to [Release train Spring Boot compatibility](https://spring.io/projects/spring-cloud) to get information about which version of Spring Cloud maps to which version of Spring Boot.

##### Get Spring Cloud For Azure Version By Spring Boot Version

| Spring Boot Version | Spring Cloud for Azure    |
| ------------------- | ------------------------- |
| 2.5.x               | 2.12.0                    |
| 2.6.x               | 2.12.0                    |


### Daily built dev feed  
Daily built beta feeds of all Spring packages from `master` branch are available. The packages are published to an Azure Artifacts public feed hosted at the following URL: 
> https://dev.azure.com/azure-sdk/public/_packaging?_a=feed&feed=azure-sdk-for-java

For developers wishing to use the daily packages, refer to [the connect to feed](https://dev.azure.com/azure-sdk/public/_packaging?_a=connect&feed=azure-sdk-for-java) instructions in Azure Artifacts.

Note: the daily package feed is considered volatile and taking dependencies on a daily package should be considered a temporary arrangement. We reserve the right to remove packages from this feed at any point in time.

## Examples

You could check [Spring Integration Guides](https://docs.microsoft.com/java/azure/spring-framework) articles to learn more on usage of specific starters.

## Troubleshooting

## Next steps

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

### Filing Issues

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg))](https://gitter.im/Microsoft/spring-on-azure)

### Pull Requests

Pull requests are welcome. To open your own pull request, click [here](https://github.com/Azure/azure-sdk-for-java/compare). When creating a pull request, make sure you are pointing to the fork and branch that your changes were made in.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/faq/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy](https://privacy.microsoft.com/privacystatement) statement to learn more.

