- [1. Goal](#1-goal)
- [2. Modules](#2-modules)
    * [2.1. Brief introduction of modules](#21-brief-introduction-of-modules)
    * [2.2. Modules' relationship](#22-modules-relationship)
        + [2.2.1 Spring Boot modules' relationship](#221-spring-boot-modules-relationship)
        + [2.2.2 Spring Cloud Azure modules' relationship](#222-spring-cloud-azure-modules-relationship)
- [3. Developing guide](#3-developing-guide)
    + [3.1. Naming conventions](#31-naming-conventions)
        - [3.1.1. Group id](#311-group-id)
        - [3.1.2. Artifact id](#312-artifact-id)
    + [3.1.3. Package name](#313-package-name)
        - [3.1.3.1. Example package names](#3131-example-package-names)
        - [3.1.3.2. About **implementation** package](#3132-about-implementation-package)
    * [3.2. Developing guide of spring-cloud-azure-autoconfigure](#32-developing-guide-of-spring-cloud-azure-autoconfigure)
        + [3.2.1. Properties](#321-properties)
            - [3.2.1.1. Unified configuration](#3211-unified-configuration)
            - [3.2.1.2. Service configuration](#3212-service-configuration)
            - [3.2.1.3. Legacy property support](#3213-legacy-property-support)
        + [3.2.2. Configure beans](#322-configure-beans)
    * [3.3. Developing guide of spring-cloud-azure-starter(-xxx)](#33-developing-guide-of-spring-cloud-azure-starter-xxx)
    * [3.4. Developing guide of spring-cloud-azure-dependencies](#34-developing-guide-of-spring-cloud-azure-dependencies)




# 1. Goal

[Spring Boot] helps you to create Spring-powered, production-grade applications and services with
absolute minimum fuss. When you use [Azure Resources] in your spring boot project, you may
need to use **Spring Cloud Azure**, which help you to use Azure Resources much easier.

Our primary goals are:
 - Provide uniform configuration of Azure SDKs. Auto-configure kinds of Azure SDK client.
 - Provide Health Indicator / Metrics for each Azure Resource.
 - Simplify dependency management.

# 2. Modules

## 2.1. Brief introduction of modules
Here is a quick overview of modules in Spring Cloud Azure:

 - **spring-cloud-azure-autoconfigure**

  spring-cloud-azure-autoconfigure attempts to deduce which beans a user might need. 
  For example, if azure-security-keyvault-secrets is on the classpath, then they probably want an SecretClient to be defined.
  Auto-configuration will always back away as the user starts to define their own beans.

 - **spring-cloud-azure-starters**

  Starters are a set of convenient dependency descriptors that you can include in your application. 
  You get a one-stop-shop for all the Azure Spring and related technology you need without having to
  hunt through sample code and copy(paste) loads of dependency descriptors. 
  For example, if you want to get started using Spring and [Azure Key Vault secrets], include the spring-cloud-azure-starter-keyvault-secrets dependency in your project, and you are good to go.

 - **spring-cloud-azure-actuator**

  Actuator endpoints let you monitor and interact with your application. Spring Cloud Azure Actuator
  provides HealthEndpoint and MetricsEndpoint of all kinds of Azure Resources,

 - **spring-cloud-azure-actuator-autoconfigure**

  This provides auto-configuration for actuator endpoints based on the content of classpath and a 
  set of properties. Just like Spring Cloud Azure AutoConfigure, this will back away as the user
  starts to define their own beans

## 2.2. Modules' relationship

### 2.2.1 Spring Boot modules' relationship

  <img src="https://user-images.githubusercontent.com/13167207/131450981-d55474bb-acc6-4c20-b6b7-28e09fdbce98.png" alt="Spring Boot modules' relationship">

  [Click this link to edit Spring Boot modules' relationship]

### 2.2.2 Spring Cloud Azure modules' relationship

  Spring Cloud Azure modules' relationship is just like Spring Boot, but we do not have spring-cloud-azure.

  <img src="https://user-images.githubusercontent.com/13167207/132191255-184dd1e2-731a-440e-926a-0bc687c0c0d4.png" alt="Spring Cloud Azure modules' relationship">

[Click this link to edit Spring Cloud Azure modules' relationship]


# 3. Developing guide

### 3.1. Naming conventions

#### 3.1.1. Group id
  All group-id should be **com.azure.spring**.

#### 3.1.2. Artifact id
  We should contain these artifacts:
  + **spring-cloud-azure-autoconfigure**
  + **spring-cloud-azure-starter**
  + **spring-cloud-azure-starter-[service]-[module]**, Example: spring-cloud-azure-starter-keyvault-secrets
  + **spring-cloud-azure-actuator**
  + **spring-cloud-azure-actuator-autoconfigure**
  + **spring-cloud-azure-starter-actuator**

### 3.1.3. Package name

#### 3.1.3.1. Example package names
 - **com.azure.spring.cloud.autoconfigure.[service].[module].implementation**.
   Example: com.azure.spring.cloud.autoconfigure.keyvault.secrets.implementation
 - **com.azure.spring.cloud.actuator.implementation**.
 - **com.azure.spring.cloud.actuator.autoconfigure.[service].[module].implementation**.
   Example: com.azure.spring.cloud.actuator.autoconfigure.keyvault.secrets.implementation

####  3.1.3.2. About **implementation** package
  **xxx.implementation.xxx** should not export in **module-info.java**. Classes in this package allow breaking change when upgrade minor / patch version


## 3.2. Developing guide of spring-cloud-azure-autoconfigure

### 3.2.1. Properties
Configuration options of Spring Cloud Azure for Azures should contain two sources of configuration:
One is the unified configuration which apply for all Spring Cloud Azure for Azures, that is, 
each starter can use the unified properties for its own features. The other is each starter's
specific configuration which only work on one starter. For the auto-configuration of each starter,
two configuration properties should be enabled.

#### 3.2.1.1. Unified configuration
Unified configuration aims to provide all fundamental and common configuration of all Azure SDK
clients for each starter, which includes credential, environment, http client, retry options and
so on.

The template of unified configuration is: **spring.cloud.azure.<configuration-type>.<configuration-name>**
- Configuration-type is to classify the configuration according to its function, the value should 
  be like **credential**, **environment**, **httpclient** and **retry**.
- Configuration-name is to describe each configuration option, like **client-id** and
  **authority-host**.

Unified configuration property class is com.azure.spring.autoconfigure.unity.AzureProperties.

#### 3.2.1.2. Service configuration
For service configuration, it should contain all configurable options of that service's client.
We divide service configuration into two parts: one is the fundamental configuration inherited from
unified configuration, which allows users to configure them using the service configuration options
instead of the unified. The other is specific configurations for that service's own features.
The template of unified configuration is:

**spring.cloud.azure.<service-name>.<configuration-type>.<configuration-name>**

- **<service-name>** is the name of an Azure service, e.g., **servicebus**, **storage**...
- **<configuration-type>** is to classify the unified configuration according to its function if needed, the value should be like **credential**, **environment**, **httpclient** and **retry**. For some services' properties, this is unnecessary, e.g., spring.cloud.azure.servicebus.connection-string
- **<configuration-name>** is to describe each configuration option, like **client-id** and **authority-host**.

Service configuration property class should be under each service's configuration package: **com.azure.spring.cloud.autoconfigure.[service].[module]**

#### 3.2.1.3. Legacy property support
For legacy properties like **azure.<service-name>.<configuration-name>**, we use
**EnvironmentPostProcessor** to convert them to the active ones, and then put them into the
application environment. When Key Vault secret starter is used, the preceding processor will be
executed again after KeyVaultEnvironmentPostProcessor to convert legacy properties load from
Key Vault if it exists.

### 3.2.2. Configure beans

We will provide necessary beans for each Azure Service.

 - SyncClient
 - AsyncClient

## 3.3. Developing guide of spring-cloud-azure-starter(-xxx)
These artifacts should only contain a pom file,
should not contain other files change readme or changelog.
Just like [spring-cloud-aws] and [spring-cloud-gcp].

## 3.4. Developing guide of spring-cloud-azure-dependencies
These artifacts should only contain a pom file,
should not contain other files change readme or changelog.
Just like [spring-cloud-aws] and [spring-cloud-gcp].


[Spring Boot]: https://github.com/spring-projects/spring-boot
[Azure Resources]: https://aka.ms/azref
[Azure Active Directory]: https://azure.microsoft.com/services/active-directory/
[Azure Key Vault secrets]: https://docs.microsoft.com/azure/key-vault/secrets/about-secrets
[keyvault]: https://docs.microsoft.com/azure/key-vault/general/basic-concepts
[certificates]: https://docs.microsoft.com/azure/key-vault/certificates/about-certificates
[Click this link to edit Spring Boot modules' relationship]: https://mermaid.live/edit#pako:#eyJjb2RlIjoiZmxvd2NoYXJ0IFREIFxuXG5zcHJpbmctYm9vdC1hY3R1YXRvciAtLT4gc3ByaW5nLWJvb3Rcblxuc3ByaW5nLWJvb3QtYXV0b2NvbmZpZ3VyZSAtLT4gc3ByaW5nLWJvb3Rcblxuc3ByaW5nLWJvb3Qtc3RhcnRlciAtLT4gc3ByaW5nLWJvb3RcbnNwcmluZy1ib290LXN0YXJ0ZXIgLS0-IHNwcmluZy1ib290LWF1dG9jb25maWd1cmVcblxuc3ByaW5nLWJvb3Qtc3RhcnRlci14eHggLS0-IHNwcmluZy1ib290LXN0YXJ0ZXJcblxuc3ByaW5nLWJvb3QtYWN0dWF0b3ItYXV0b2NvbmZpZ3VyZSAtLT4gc3ByaW5nLWJvb3QtYXV0b2NvbmZpZ3VyZVxuc3ByaW5nLWJvb3QtYWN0dWF0b3ItYXV0b2NvbmZpZ3VyZSAtLT4gc3ByaW5nLWJvb3RcbnNwcmluZy1ib290LWFjdHVhdG9yLWF1dG9jb25maWd1cmUgLS0-IHNwcmluZy1ib290LWFjdHVhdG9yXG5cbnNwcmluZy1ib290LXN0YXJ0ZXItYWN0dWF0b3IgLS0-IHNwcmluZy1ib290LXN0YXJ0ZXJcbnNwcmluZy1ib290LXN0YXJ0ZXItYWN0dWF0b3IgLS0-IHNwcmluZy1ib290LWFjdHVhdG9yLWF1dG9jb25maWd1cmVcblxuJSU9PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PVxuY2xhc3NEZWYgZ3JlZW4gIGZpbGw6IzBGMFxuY2xhc3NEZWYgYmx1ZSAgIGZpbGw6IzBGRlxuY2xhc3NEZWYgeWVsbG93IGZpbGw6I0ZGMFxuXG5jbGFzcyBzcHJpbmctYm9vdCxzcHJpbmctYm9vdC1hY3R1YXRvcixzcHJpbmctYm9vdC1hY3R1YXRvci1hdXRvY29uZmlndXJlLHNwcmluZy1ib290LWF1dG9jb25maWd1cmUsc3ByaW5nLWJvb3Qtc3RhcnRlcixzcHJpbmctYm9vdC1zdGFydGVyLWFjdHVhdG9yLHNwcmluZy1ib290LXN0YXJ0ZXIteHh4IGdyZWVuXG5cblxuIiwibWVybWFpZCI6IntcbiAgXCJ0aGVtZVwiOiBcImRlZmF1bHRcIlxufSIsInVwZGF0ZUVkaXRvciI6ZmFsc2UsImF1dG9TeW5jIjp0cnVlLCJ1cGRhdGVEaWFncmFtIjpmYWxzZX0
[Click this link to edit Spring Cloud Azure modules' relationship]: https://mermaid.live/edit#pako:#eyJjb2RlIjoiZmxvd2NoYXJ0IFREIFxuXG5cbnNwcmluZy1jbG91ZC1henVyZS1zdGFydGVyIC0tPiBzcHJpbmctY2xvdWQtYXp1cmUtYXV0b2NvbmZpZ3VyZVxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0YXJ0ZXIteHh4IC0tPiBzcHJpbmctY2xvdWQtYXp1cmUtc3RhcnRlclxuXG5zcHJpbmctY2xvdWQtYXp1cmUtYWN0dWF0b3ItYXV0b2NvbmZpZ3VyZSAtLT4gc3ByaW5nLWNsb3VkLWF6dXJlLWFjdHVhdG9yXG5zcHJpbmctY2xvdWQtYXp1cmUtYWN0dWF0b3ItYXV0b2NvbmZpZ3VyZSAtLT4gc3ByaW5nLWNsb3VkLWF6dXJlLWF1dG9jb25maWd1cmVcblxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0YXJ0ZXItYWN0dWF0b3IgLS0-IHNwcmluZy1jbG91ZC1henVyZS1zdGFydGVyXG5zcHJpbmctY2xvdWQtYXp1cmUtc3RhcnRlci1hY3R1YXRvciAtLT4gc3ByaW5nLWNsb3VkLWF6dXJlLWFjdHVhdG9yLWF1dG9jb25maWd1cmVcblxuXG5cbiUlPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT1cbmNsYXNzRGVmIGdyZWVuICBmaWxsOiMwRjBcbmNsYXNzRGVmIGJsdWUgICBmaWxsOiMwRkZcbmNsYXNzRGVmIHllbGxvdyBmaWxsOiNGRjBcblxuY2xhc3Mgc3ByaW5nLWNsb3VkLWF6dXJlLWFjdHVhdG9yLHNwcmluZy1jbG91ZC1henVyZS1hY3R1YXRvci1hdXRvY29uZmlndXJlLHNwcmluZy1jbG91ZC1henVyZS1hdXRvY29uZmlndXJlLHNwcmluZy1jbG91ZC1henVyZS1zdGFydGVyLHNwcmluZy1jbG91ZC1henVyZS1zdGFydGVyLWFjdHVhdG9yLHNwcmluZy1jbG91ZC1henVyZS1zdGFydGVyLXh4eCBibHVlXG5cblxuIiwibWVybWFpZCI6IntcbiAgXCJ0aGVtZVwiOiBcImRlZmF1bHRcIlxufSIsInVwZGF0ZUVkaXRvciI6ZmFsc2UsImF1dG9TeW5jIjp0cnVlLCJ1cGRhdGVEaWFncmFtIjpmYWxzZX0
[spring-projects]: https://spring.io/projects
[transitive-dependencies]: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#transitive-dependencies
[optional-dependencies]: https://maven.apache.org/guides/introduction/introduction-to-optional-and-excludes-dependencies.html#optional-dependencies
[spring-boot-starter-data-redis]: https://github.com/spring-projects/spring-boot/blob/v2.5.3/spring-boot-project/spring-boot-starters/spring-boot-starter-data-redis/build.gradle
[spring-cloud-aws]: https://github.com/awspring/spring-cloud-aws
[spring-cloud-gcp]: https://github.com/spring-cloud/spring-cloud-gcp