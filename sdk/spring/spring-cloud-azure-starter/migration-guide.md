# Guide for migrating to `spring-cloud-azure-starter` from `azure-spring-boot-starter`



This guide assists in the migration to `spring-cloud-azure-stater` from `com.azure.spring:azure-spring-boot-starter`. It will focus the side-by-side comparisons for similar configurations between the two packages.

Familiarity with `com.azure.spring:azure-spring-boot-starter` package is assumed. For those new to the Spring Cloud Azure Starter library, please refer to the REAME.md rather than this guide.

## Table of Contents

[TOC]

## Migration benefits

A natural question to ask when considering whether or not to adopt a new version or libary is its benefits. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learing the patterns and practices to best support developer productivity and to understand the gaps that the Spring Cloud Azure starters have.

There were several areas of consistent feedback expressed across the Spring Cloud Azure starters. The most important is that the starters for different Azure services have not enabled the complete set of configurations. Additionally, the inconsistency of artifact ids, versions, configurations made the learning curve steep. 

To improve the development experience across Spring Cloud Azure starters, a set of design guidelines was introduced to ensure that Spring Cloud Azure starters have a natural and idiomatic feel with respect to the Spring ecosystem. Further details are available in the guidelines for those interested. 

The morden `spring-cloud-azure-starter` provides the shared experience when auto-configure different Azure Service SDK clients, such as:

- A unified way to configure credential, proxy, retry, cloud environment, and transport layer settings.
- Supporting all the authenticating methods an Azure Service or Azure Service SDK supports.

## Important changes

### Group id, artifact id and package names

Group ids are the same for morden and legacy Spring Cloud Azure starters, they are all `com.azure.spring`. Artifact ids for the morden Spring Cloud Azure starters have changed. Each will have an artifact id following the pattern `spring-cloud-azure-starter-[service]`. The legacy starters each has an artifact id following the pattern `azure-spring-boot-starter-[service]`. This provides a quick and accessible means to help understand, at a glance, whether you are using morden or legacy starters.

The morden `spring-cloud-azure-starter` have packages and namespaces that begin with `com.azure.spring.cloud` and were released starting with version 4. The legacy starter have package names starting with `com.azure.spring` and a version of 3.x.x.

### Configuration properties

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









