# Guide for migrating to Spring Cloud Azure 4.0

This guide assists in the migration to **Spring Cloud Azure 4.0** from legacy Azure Spring libraries. We will call
libraries whose group id and artifact id following pattern `com.azure.spring:spring-cloud-azure-*` the modern libraries,
and those with pattern `com.azure.spring:azure-spring-boot-*`, `com.azure.spring:azure-spring-cloud-*` ,
or `com.azure.spring:azure-spring-integration-*` the legacy ones. This guide will focus the side-by-side comparisons for
similar configurations between the modern and legacy libraries. o

Familiarity with `com.azure.spring:azure-spring-boot-*`, `com.azure.spring:azure-spring-cloud-*`
or `com.azure.spring:azure-spring-integration-*` package is assumed. For those new to the Spring Cloud Azure 4.0
libraries, please refer to the README.md**[placeholder]** rather than this guide.

[TOC]

## Migration benefits

A natural question to ask when considering whether to adopt a new version or library is its benefits. As 
Azure has
matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and
practices to best support developer productivity and to understand the gaps that the Spring Cloud Azure libraries have.

There were several areas of consistent feedback expressed across the Spring Cloud Azure libraries. The most important is
that the libraries for different Azure services have not enabled the complete set of configurations. Additionally, the
inconsistency of project naming, artifact ids, versions, configurations made the learning curve steep.

To improve the development experience across Spring Cloud Azure libraries, a set of design guidelines was introduced to
ensure that Spring Cloud Azure libraries have a natural and idiomatic feel with respect to the Spring ecosystem. Further
details are available in the guidelines**[placeholder]** for those interested.

The **Spring Cloud Azure 4.0** provides the shared experience across libraries integrating with different Spring
projects, for example Spring Boot, Spring Integration, Spring Cloud Stream, etc. The shared experience includes:

- **[placeholder]** An official name for the project?

- A unified BOM to include all Spring Cloud Azure 4.0 libraries.
- A consistent naming convention for artifacts.
- A unified way to configure credential, proxy, retry, cloud environment, and transport layer settings.

- Supporting all the authenticating methods an Azure Service or Azure Service SDK supports.

## Overview

This migration guide will be consisted of following sections:

- Naming changes for Spring Cloud Azure 4.0
- Artifact changes: renamed / added / deleted

- Configuration properties
- Authentication

## Naming changes

There has never been a consistent or official name to call all the Spring Cloud Azure libraries, some of them were
called `Azure Spring Boot` and some of them ` Spring on Azure` , and all these names will make developer confused. Since
4.0, we began to use the project name `Spring Cloud Azure` to represent all the Azure Spring libraries.

## BOM

We used to ship two BOMs for our libraries, the `azure-spring-boot-bom` and `azure-spring-cloud-dependencies`, but we
combined these two BOMs into one BOM since 4.0, the `spring-cloud-azure-dependencies`. Please add an entry in the
dependencyManagement of your project to benefit from the dependency management.

```xml

<properties>
    <spring.cloud.azure.version>4.0.0</spring.cloud.azure.version>
</properties>
<dependencyManagement>
<dependencies>
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>spring-cloud-azure-dependencies</artifactId>
        <version>${spring.cloud.azure.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencies>
</dependencyManagement>
```

## Artifact changes: renamed / added / deleted

Group ids are the same for modern and legacy Spring Cloud Azure libraries, they are all `com.azure.spring`. Artifact ids
for the modern Spring Cloud Azure libraries have changed. And according to which Spring project it belongs, Spring Boot,
Spring Integration or Spring Cloud Stream, the artifact ids pattern could be `spring-cloud-azure-starter-[service]`
, `spring-integration-azure-[service]` and `spring-cloud-azure-stream-binder-[service]`. The legacy starters each has an
artifact id following the pattern `azure-spring-*`. This provides a quick and accessible means to help understand, at a
glance, whether you are using modern or legacy starters.

In the process of developing Spring Cloud Azure 4.0, we renamed some artifacts to make them follow the new naming
conventions, deleted some artifacts for its functionality could be put in a more appropriate artifact, and added 
some new artifacts to better serve some scenarios.

| Legacy Artifact ID                                | Modern Artifact ID                                           | Description                                                  |
| :------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| azure-spring-boot-starter                         | spring-cloud-azure-starter                                   | This artifact has been deleted with all functionality be merged into the new `spring-cloud-azure-starter` artifact. |
| azure-spring-boot-starter-active-directory        | // TODO                                                      |                                                              |
| azure-spring-boot-starter-active-directory-b2c    | // TODO                                                      |                                                              |
| azure-spring-boot-starter-cosmos                  | spring-cloud-azure-starter-cosmos                            |                                                              |
| azure-spring-boot-starter-keyvault-certificates   | // TODO                                                      |                                                              |
| azure-spring-boot-starter-keyvault-secrets        | spring-cloud-azure-starter-keyvault-secrets                  |                                                              |
| azure-spring-boot-starter-servicebus-jms          | spring-cloud-azure-starter-servicebus-jms                    |                                                              |
| azure-spring-boot-starter-storage                 | spring-cloud-azure-starter-storage-blob <br/>spring-cloud-azure-starter-storage-file-share | The legacy artifact contains the functionality of both Storage Blob and File Share, it's been splited into two separate artifacts in 4.0, spring-cloud-azure-starter-storage-blob and spring-cloud-azure-starter-storage-file-share. |
| azure-spring-boot                                 | N/A                                                          | This artifact has been deleted with all functionality be merged into the new `spring-cloud-azure-autoconfigure` artifact. |
| azure-spring-cloud-autoconfigure                  | N/A                                                          | This artifact has been deleted with all functionality be merged into the new `spring-cloud-azure-autoconfigure` artifact. |
| azure-spring-cloud-context                        | N/A                                                          | This artifact has been deleted with all functionality be merged into the new `spring-cloud-azure-autoconfigure`  and `spring-cloud-azure-resourcemanager` artifacts. |
| azure-spring-cloud-messaging                      | // TODO                                                      |                                                              |
| azure-spring-cloud-starter-cache                  | // TODO                                                      |                                                              |
| azure-spring-cloud-starter-eventhubs-kafka        | // TODO                                                      |                                                              |
| azure-spring-cloud-starter-eventhubs              | spring-cloud-azure-starter-integration-eventhubs             |                                                              |
| azure-spring-cloud-starter-servicebus             | spring-cloud-azure-starter-integration-servicebus            |                                                              |
| azure-spring-cloud-starter-storage-queue          | spring-cloud-azure-starter-integration-storage-queue         |                                                              |
| azure-spring-cloud-storage                        | N/A                                                          | This artifact has been deleted with all functionalities merged into the new `spring-cloud-azure-autoconfigure` artifact. |
| azure-spring-cloud-stream-binder-eventhubs        | spring-cloud-azure-stream-binder-eventhubs                   |                                                              |
| azure-spring-cloud-stream-binder-service-core  | spring-cloud-azure-stream-binder-servicebus-core             |                                                              |
| azure-spring-cloud-stream-binder-servicebus-queue | // TODO                                                      |                                                              |
| azure-spring-cloud-stream-binder-servicebus-topic | // TODO                                                      |                                                              |
| azure-spring-integration-core                     | spring-integration-azure-core                                |                                                              |
| azure-spring-integration-eventhubs                | spring-integration-azure-eventhubs                           |                                                              |
| azure-spring-integration-servicebus               | spring-integration-azure-servicebus                          |                                                              |
| azure-spring-integration-storage-queue            | spring-integration-azure-storage-queue                       |                                                              |
| N/A                                               | spring-cloud-azure-actuator                                  | Spring Cloud Azure Actuator.                                 |
| N/A                                               | spring-cloud-azure-actuator-autoconfigure                    | Spring Cloud Azure Actuator AutoConfigure.                   |
| N/A                                               | spring-cloud-azure-autoconfigure                             | Spring Cloud Azure AutoConfigure.                            |
| N/A                                               | spring-cloud-azure-core                                      |                                                              |
| N/A                                               | spring-cloud-azure-resourcemanager                           | The Core library using Azure Resource Manager to read metadata and create resources. |
| N/A                                               | spring-cloud-azure-service                                   |                                                              |
| N/A                                               | spring-cloud-azure-starter                                   | The Core Spring Cloud Azure starter, including auto-configuration support. |
| N/A                                               | spring-cloud-azure-starter-appconfiguration                  | Starter for using Azure App Configuration.                   |
| N/A                                               | spring-cloud-azure-starter-eventhubs                         | Starter for using Azure Event Hubs.                          |
| N/A                                               | spring-cloud-azure-starter-servicebus                        | Starter for using Azure Service Bus.                         |
| N/A                                               | spring-cloud-azure-starter-storage-blob                      | Starter for using Azure Storage Blob.                        |
| N/A                                               | spring-cloud-azure-starter-storage-file-share                | Starter for using Azure Storage File Share.                  |
| N/A                                               | spring-cloud-azure-starter-storage-queue                     | Starter for using Azure Storage Queue.                       |
| N/A                                               | spring-cloud-azure-starter-stream-eventhubs                  |                                                              |
| N/A                                               | spring-cloud-azure-starter-stream-servicebus                 |                                                              |

## Dependencies changes

Some unnecessary dependencies were included in the legacy artifacts, which we have removed in the modern Spring Cloud
Azure 4.0 libraries. Please make sure add the removed dependencies manually to your project to prevent unintentionally crash.

### spring-cloud-azure-starter

| Removed dependencies                                    | Description                                                  |
| ------------------------------------------------------- | ------------------------------------------------------------ |
| org.springframework.boot:spring-boot-starter-validation | Please include the validation starter if you want to use the Hibernate Validator. |

## Configuration properties

### Global configurations

The modern `spring-cloud-azure-starter` allows developers to define properties that apply to all Azure SDKs in the
namespace `spring.cloud.azure`. It was not supported in the legacy `azure-spring-boot-starter`. The global
configurations can be divided into five categories:

| Prefix                        | Description                                                  |
| ----------------------------- | ------------------------------------------------------------ |
| spring.cloud.azure.client     | To configure the transport clients underneath each Azure SDK. |
| spring.cloud.azure.credential | To configure how to authenticate with Azure Active Directory. |
| spring.cloud.azure.profile    | To configure the Azure cloud environment.                    |
| spring.cloud.azure.proxy      | To configure the proxy options apply to all Azure SDK clients. |
| spring.cloud.azure.retry      | To configure the retry options apply to all Azure SDK clients. |

For a full list of common configurations, check this list **[placeholder]**.

### Each SDK configurations
####
## API breaking changes

## Authentication

Spring Cloud Azure 4.0 supports all the authentication methods each Azure Service SDK supports. It allows to configure a
global token credential as well as providing the token credential at each service level. But credential is not required
to configure in Spring Cloud Azure 4.0, it can leverage the credential stored in local developing environment, or
managed identity in Azure Services, just make sure the principal has been granted sufficient permission to access the
target Azure resources. 





