# Azure Spring Boot client library for Java

## Getting started
### Introduction

This repo is for Spring Boot Starters of Azure services. It helps Spring Boot developers to adopt Azure services.

### Support Spring Boot
This repository supports Spring Boot 2.1.x, 2.2.x and 2.3.x. Please read [Spring Boot Dependency Mapping](https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Boot-Dependency-Mapping) for dependency mapping.

### Prerequisites
- JDK 1.8 and above
- [Maven](http://maven.apache.org/) 3.0 and above

## Key concepts
### Usage

Below starters are available with latest release version. We recommend users to leverage latest version for bug fix and new features.
You can find them in [Maven Central Repository](https://search.maven.org/).
The first three starters are also available in [Spring Initializr](http://start.spring.io/). 

Starter Name | Version for Spring Boot 2.3.x | Version for Spring Boot 2.2.x | Version for Spring Boot 2.1.x | Version for Spring Boot 2.0.x
---|:---:|:---:|:---:|:---:
[azure-active-directory-spring-boot-starter](azure-spring-boot-starter-active-directory/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-spring-boot-starter%20AND%20v:2.0.*)
[azure-storage-spring-boot-starter](azure-spring-boot-starter-storage/README.md) | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-storage-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-storage-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-storage-spring-boot-starter%20AND%20v:2.0.*)
[azure-keyvault-secrets-spring-boot-starter](azure-spring-boot-starter-keyvault-secrets/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-keyvault-secrets-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-keyvault-secrets-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-keyvault-secrets-spring-boot-starter%20AND%20v:2.0.*)
[azure-active-directory-b2c-spring-boot-starter](azure-spring-boot-starter-active-directory-b2c/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-active-directory-b2c-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.2.*) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-active-directory-b2c-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-active-directory-b2c-spring-boot-starter%20AND%20v:2.1.*) | N/A
azure-cosmosdb-spring-boot-starter | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-cosmosdb-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-cosmosdb-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-cosmosdb-spring-boot-starter%20AND%20v:2.0.*)
[azure-cosmosdb-spring-boot-2-2-starter](azure-spring-boot-2-2-starter-cosmosdb/README.md) | N/A | N/A | N/A | N/A
[azure-cosmosdb-spring-boot-2-3-starter](azure-spring-boot-2-3-starter-cosmosdb/README.md) | N/A | N/A | N/A | N/A
[azure-mediaservices-spring-boot-starter](azure-spring-boot-starter-mediaservices/README.md) | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-mediaservices-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-mediaservices-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-mediaservices-spring-boot-starter%20AND%20v:2.0.*)
[azure-servicebus-spring-boot-starter](azure-spring-boot-starter-servicebus/README.md) | N/A | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-spring-boot-starter%22) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-spring-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-spring-boot-starter%20AND%20v:2.0.*)
[spring-data-gremlin-boot-starter](azure-spring-boot-starter-data-gremlin/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22spring-data-gremlin-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-gremlin-boot-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-gremlin-boot-starter%20AND%20v:2.0.*)
[azure-spring-boot-metrics-starter](azure-spring-boot-starter-metrics) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-spring-boot-metrics-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.2.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.1.*) | [![](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-spring-boot-metrics-starter/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-spring-boot-metrics-starter%20AND%20v:2.0.*)
[azure-servicebus-jms-spring-boot-starter](azure-spring-boot-starter-servicebus-jms/README.md) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-servicebus-jms-spring-boot-starter%22) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.2.*) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:azure-servicebus-jms-spring-boot-starter%20AND%20v:2.1.*) | N/A

### Snapshots  
[![Nexus OSS](https://img.shields.io/nexus/snapshots/https/oss.sonatype.org/com.microsoft.azure/azure-spring-boot.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/microsoft/azure/azure-spring-boot/)

Snapshots built from `master` branch are available, add [maven repositories](https://maven.apache.org/settings.html#Repositories) configuration to your pom file as below. 
```xml
<repositories>
  <repository>
    <id>nexus-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>
```
## Examples
You could check below articles to learn more on usage of specific starters.

[How to use the Spring Boot Starter with Azure Cosmos DB API](https://docs.microsoft.com/java/azure/spring-framework/configure-spring-boot-starter-java-app-with-cosmos-db).

## Troubleshooting
## Next steps

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](./CONTRIBUTING.md) to build from source or contribute.

### Filing Issues

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter](https://badges.gitter.im/Microsoft/spring-on-azure.svg)](https://gitter.im/Microsoft/spring-on-azure)

### Pull Requests

Pull requests are welcome. To open your own pull request, click [here](https://github.com/Microsoft/azure-spring-boot/compare). When creating a pull request, make sure you are pointing to the fork and branch that your changes were made in.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy](https://privacy.microsoft.com/privacystatement) statement to learn more.

