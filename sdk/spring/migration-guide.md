# Guide for migrating to Spring Cloud Azure 4.0



This guide assists in the migration to **Spring Cloud Azure 4.0** from legacy Azure Spring libraries. We will call libraries whose group id and id following pattern `com.azure.spring:spring-cloud-azure-*` the morden libraries, and those with pattern `com.azure.spring:azure-spring-boot-*` or `com.azure.spring:azure-spring-cloud-*` the legacy ones. This guide will focus the side-by-side comparisons for similar configurations between the morden and legacy libraries. 

Familiarity with `com.azure.spring:azure-spring-boot-*` or `com.azure.spring:azure-spring-cloud-*` package is assumed. For those new to the Spring Cloud Azure 4.0 libraries, please refer to the REAME.md**[placeholder]** rather than this guide.

[TOC]



## Migration benefits

A natural question to ask when considering whether or not to adopt a new version or libary is its benefits. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learing the patterns and practices to best support developer productivity and to understand the gaps that the Spring Cloud Azure libraries have.

There were several areas of consistent feedback expressed across the Spring Cloud Azure libraries. The most important is that the libraries for different Azure services have not enabled the complete set of configurations. Additionally, the inconsistency of artifact ids, versions, configurations made the learning curve steep. 

To improve the development experience across Spring Cloud Azure libraries, a set of design guidelines was introduced to ensure that Spring Cloud Azure starters have a natural and idiomatic feel with respect to the Spring ecosystem. Further details are available in the guidelines for those interested. 

The **Spring Cloud Azure 4.0** provides the shared experience across libraries integrating with different Spring projects, for example Spring Boot, Spring Integration, Spring Cloud Stream, and etc. The shared experience includes:

- **[placeholder]** An official name for the project?

- A unified BOM to include all Spring Cloud Azure 4.0 libraries.
- A consitent naming convention for artifacts.
- A unified way to configure credential, proxy, retry, cloud environment, and transport layer settings.

- Supporting all the authenticating methods an Azure Service or Azure Service SDK supports.

## Overview

This migration guide will be consisted of below sections:

- Naming changes for Spring Cloud Azure 4.0
- Artifcats changes: renamed / added / deleted 

- Configuration properties
- Authentication 

## Naming changes

There has never been a consistent or official name to call all the Spring Cloud Azure libraries, some of them were called `Azure Spring Boot` and some of them ` Spring on Azure` , and all these names will make developer confused. Since 4.0, we began to use the project name `Spring Cloud Azure` to represent all the Azure Spring libraries. 

## BOM

We used to ship two BOMs for our libaries, the `azure-spring-boot-bom` and `azure-spring-cloud-dependencies`, but we combined these two BOMs into one BOM since 4.0, the `spring-cloud-azure-dependencies`. Please add an entry in the dependencyManagement of your project to benefit from the dependency management.

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



## Artifacts changes: renamed / added / deleted

Group ids are the same for morden and legacy Spring Cloud Azure libraries, they are all `com.azure.spring`. Artifact ids for the morden Spring Cloud Azure starters have changed. And according to the Spring projects it belongs, such as Spring Boot, Spring Integration or Spring Cloud Stream, the artifact ids pattern could be `spring-cloud-azure-starter-[service]`, `spring-integration-azure-[service]` and `spring-cloud-azure-stream-binder-[service]`. The legacy starters each has an artifact id following the pattern `azure-spring-*`. This provides a quick and accessible means to help understand, at a glance, whether you are using morden or legacy starters.

### azure-spring-boot-starter

This artifact has been renamed to **spring-cloud-azure**-starter.

### azure-spring-boot-starter-active-directory

// TODO

### azure-spring-boot-starter-active-directory-b2c

// TODO

### azure-spring-boot-starter-cosmos

This artifact has been renamed to **spring-cloud-azure-starter**-cosmos.

### azure-spring-boot-starter-keyvault-certificates

// TODO

### azure-spring-boot-starter-keyvault-secrets

This artifact has been renamed to **spring-cloud-azure-starter**-keyvault-secrets.

### azure-spring-boot-starter-servicebus-jms

This artifact has been renamed to **spring-cloud-azure-starter**-servicebus-jms.

### azure-spring-boot-starter-storage

This artifact has been deleted, and two new artifacts were created to substitute it, the `spring-cloud-azure-starter-storage-blob` and `spring-cloud-azure-starter-storage-file-share`.

### azure-spring-boot

This artifact has been deleted with all functionality be merged into the new `spring-cloud-azure-autoconfigure` artifact.

### azure-spring-cloud-autoconfigure

This artifact has been deleted with all functionality be merged into the new `spring-cloud-azure-autoconfigure` artifact.

### azure-spring-cloud-context

This artifact has been deleted without substitution.

### azure-spring-cloud-messaging

// TODO

### azure-spring-cloud-starter-cache

// TODO

### azure-spring-cloud-starter-eventhubs-kafka

// TODO

### azure-spring-cloud-starter-eventhubs

This artifact has been renamed to `spring-cloud-azure-starter-integration-eventhubs`.

### azure-spring-cloud-starter-servicebus

This artifact has been renamed to `spring-cloud-azure-starter-integration-servicebus`.

### azure-spring-cloud-starter-storage-queue

This artifact has been renamed to `spring-cloud-azure-starter-integration-storage-queue`.

### azure-spring-cloud-storage

This artifact has been deleted with all functionality be merged into the new `spring-cloud-azure-autoconfigure` artifact.

### azure-spring-cloud-stream-binder-eventhubs

This artifact has been renamed to `spring-cloud-azure-stream-binder-eventhubs`.

### azure-spring-cloud-stream-binder-servicebus-core

This artifact has been renamed to `spring-cloud-azure-stream-binder-servicebus-core`.

### azure-spring-cloud-stream-binder-servicebus-queue

// TODO

### azure-spring-cloud-stream-binder-servicebus-topic

// TODO

### azure-spring-inetgration-core

This artifact has been renamed to `spring-integration-azure-core`.

### azure-spring-inetgration-eventhubs

This artifact has been renamed to `spring-integration-azure-eventhubs`.

### azure-spring-inetgration-servicebus

This artifact has been renamed to `spring-integration-azure-servicebus`.

### azure-spring-inetgration-storage-queue

This artifact has been renamed to `spring-integration-azure-storage-queue`.



### Newly added artifacts:

There're also a bunch of starters have been newly added, here the full list:

#### spring-cloud-azure-actuator

// TODO

#### spring-cloud-azure-actuator-autoconfigure

// TODO

#### spring-cloud-azure-core

// TODO

#### spring-cloud-azure-resourcemanager

// TODO

#### spring-cloud-azure-service

// TODO

#### spring-cloud-azure-starter-appconfiguration

// TODO

#### spring-cloud-azure-starter-eventhubs

// TODO

#### spring-cloud-azure-starter-servicebus

// TODO

#### spring-cloud-azure-starter-storage-blob

// TODO

#### spring-cloud-azure-starter-storage-file-share

// TODO

#### spring-cloud-azure-starter-storage-queue

// TODO

#### spring-cloud-azure-starter-stream-eventhubs

// TODO

#### spring-cloud-azure-starter-stream-servicebus

// TODO @yiliu

## Configuration properties

#### Configurations apply to all service sdks

The morden `spring-cloud-azure-starter` allows developers to define properties that apply to all Azure SDKs in the namespace `spring.cloud.azure`. It was not supported in the legacy `azure-spring-boot-starter`. The common configurations can be divided into five categories:

| Prefix                        | Description                                                  |
| ----------------------------- | ------------------------------------------------------------ |
| spring.cloud.azure.client     | To configure the transport clients underneath each Azure SDK. |
| spring.cloud.azure.credential | To configure how to authenticate with Azure Active Directory. |
| spring.cloud.azure.profile    | To configure the Azure cloud environment.                    |
| spring.cloud.azure.proxy      | To configure the proxy options apply to all Azure SDK clients. |
| spring.cloud.azure.retry      | To configure the retry options apply to all Azure SDK clients. |

For a full list of common configurations, check this list **[placeholder]**.



## Authentication









