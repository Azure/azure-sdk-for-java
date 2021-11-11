# Azure Spring Boot client library for Java

## Getting started
### Introduction

This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

### Support
* This repository supports Spring Boot 2.2.x, 2.3.x and 2.4.x. Please read [Spring Boot Dependency Mapping][spring-boot-dependency-mapping] for dependency mapping.

### Prerequisites
- JDK 1.8 and above
- [Maven][maven] 3.0 and above

## Key concepts
### 1.spring-cloud-azure-starter

This repository is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services. It supports Spring Boot 2.1.x, 2.2.x and 2.3.x. Please read [Spring Boot Dependency Mapping](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Boot-Dependency-Mapping) for dependency mapping.

### 2.spring-cloud-azure-starter-cosmos

[Azure Cosmos DB](https://azure.microsoft.com/services/cosmos-db/) is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Graph, and Azure Table storage.

### 3.spring-cloud-azure-starter-eventhubs

The Spring Cloud Event Hubs starter helps developers to finish the auto-configuration of Event Hubs and provides Spring Integration on Event Hubs.

For Spring Integration on Event Hubs, please refer to the [source code][source_code_eventhubs].


### 4.spring-cloud-azure-starter-integration-eventhubs

Azure Key Vault Certificates Spring Boot Starter is Spring starter for [Azure Key Vault Certificates](https://docs.microsoft.com/azure/key-vault/certificates/about-certificates), it allows you to securely manage and tightly control your certificates.

### 5.spring-cloud-azure-starter-integration-servicebus

The Spring Integration for Azure Service Bus extension project provides inbound and outbound channel adapters for Azure Service Bus.
Microsoft Azure Service Bus is a fully managed enterprise integration message broker. Service Bus can decouple applications and services.
Service Bus offers a reliable and secure platform for asynchronous transfer of data and state.

### 6.spring-cloud-azure-starter-integration-storage-queue

The *Spring Integration for Storage Queue* extension project provides inbound and outbound channel adapters and gateways for Azure Storage Queue.

### 7.spring-cloud-azure-starter-keyvault-certificates

Azure Key Vault Certificates Spring Boot Starter is Spring starter for [Azure Key Vault Certificates](https://docs.microsoft.com/azure/key-vault/certificates/about-certificates), it allows you to securely manage and tightly control your certificates.

### 8.spring-cloud-azure-starter-keyvault-secrets

Azure Key Vault Secrets Spring Boot Starter for Azure Key Vault adds Azure Key Vault as one of the
Spring PropertySource, so secrets stored in Azure Key Vault could be easily used and conveniently
accessed like other externalized configuration property, e.g. properties in files.

### 9.spring-cloud-azure-starter-servicebus

The Spring Cloud Service Bus starter helps developers to finish the auto-configuration of Service Bus and provides Spring Integration with Service Bus.

### 10.spring-cloud-azure-starter-servicebus-jms

With this starter you could easily use Spring JMS Queue and Topic with Azure Service Bus.

### 11.spring-cloud-azure-starter-storage-queue

The Spring Cloud Storage Queue starter helps developers to finish the auto-configuration of Storage Queue and provides Spring Integration with Storage Queue.

### 12.spring-cloud-azure-stream-binder-eventhubs

The project provides **Spring Cloud Stream Binder for Azure Event Hub** which allows you to build message-driven
microservice using **Spring Cloud Stream** based on [Azure Event Hub][azure_event_hub] service.

### 13.spring-cloud-azure-stream-binder-servicebus-queue

The project provides **Spring Cloud Stream Binder for Azure Service Bus Queue** which allows you to build message-driven
microservice using **Spring Cloud Stream** based on [Azure Service Bus Queue][azure_service_bus].

### 14.spring-cloud-azure-stream-binder-servicebus-topic

The project provides **Spring Cloud Stream Binder for Azure Service Bus Topic** which allows you to build message-driven
microservice using **Spring Cloud Stream** based on [Azure Service Bus Topic][azure_service_bus].


## Examples
You could check [Spring Integration Guides][spring-integration-guides] articles to learn more on usage of specific starters.

## Troubleshooting

## Next steps

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

<!--link-->
[maven]: https://maven.apache.org/
[spring-initializr]: https://start.spring.io/
[maven-central-repository]: https://search.maven.org/
[spring-cloud]: https://spring.io/projects/spring-cloud
[spring-cloud-stream]: https://cloud.spring.io/spring-cloud-stream/
[spring-boot-dependency-mapping]: https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Versions-Mapping

[spring-integration-guides]: https://docs.microsoft.com/java/azure/spring-framework
[connect-to-feed-link]: https://dev.azure.com/azure-sdk/public/_packaging?_a=connect&feed=azure-sdk-for-java

[spring-contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md
[azure-sdk-for-java-issues]: https://github.com/Azure/azure-sdk-for-java/issues
[gitter-spring-on-azure-img]: https://badges.gitter.im/Microsoft/spring-on-azure.svg
[gitter-spring-on-azure]: https://gitter.im/Microsoft/spring-on-azure
[azure-sdk-for-java-compare]: https://github.com/Azure/azure-sdk-for-java/compare

[codeofconduct]: https://opensource.microsoft.com/codeofconduct/faq/
[codeofconduct-faq]: https://opensource.microsoft.com/codeofconduct/faq/
[privacy-statement]: https://privacy.microsoft.com/privacystatement
[azure_event_hub]: https://azure.microsoft.com/services/event-hubs/
[source_code_eventhubs]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-integration-eventhubs
[azure_service_bus]: https://azure.microsoft.com/services/service-bus/
