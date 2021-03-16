# Spring Reference Yaml Documentation

## Background
sdk/spring/spring-reference.yml is aimed to build package reference of Azure Spring libraries to support an Intellij plugin for Azure SDK reference. 

The yaml follows structure of [Spring Initializr's library mapping schema](https://github.com/spring-io/start.spring.io/blob/master/start-site/src/main/resources/application.yml) and thus is compatible of keys under property of `dependencies` to describe a library.
To make things clear, we call sdk/spring/spring-reference.yml as Spring yaml, and Spring Initializr's library mapping schema as template yaml.
For those keys not listed in Spring yaml, we can add them in case of future usage.

Hierarchy of Spring yaml is organized as : Service -> Feature -> Package. Taking Cosmos DB starter as example, it is described as Cosmos DB -> Spring Data Cosmos -> azure-spring-boot-starter-cosmos.
 
## Property Introduction

- name(First-level directory): Name of Azure service which the spring library relies on.
    - For Spring library of which the underlying package is covered by [java-packages.csv](https://github.com/Azure/azure-sdk/blob/master/_data/releases/latest/java-packages.csv), we use its underlying library's `ServiceName`as the service name.
    - For the rest like AAD starter, we create the service name ourselves, and try to follow the specification of java-pacakges.csv.
- content: Features under an Azure Service.
- name(Second-level directory): Name of a feature under the associated Azure service which the spring library relies on.
    - For Spring library of which the underlying package is covered by [java-packages.csv](https://github.com/Azure/azure-sdk/blob/master/_data/releases/latest/java-packages.csv), we use its underlying library's `DisplayName`as the feature name.
    - For the rest, we create the feature name ourselves, and try to follow the specification of java-pacakges.csv.
- id: Id of feature name that is not used now and reserved in case of future usage for reference.
- description: Introduction of a feature.
- msdocs: Product documentation.
- artifacts: Package list of a service feature.
- artifactId: Artifact-id of each package.
- groupId: Group-id of each package.
- versionGA: The latest GA version if exists.
- versionPreview: The latest preview version if exists.
- type: To mark if the package is Spring library, Azure service client library or management library, supported values are `spring`, `client` and `mgmt`.
- links: Link list of each package. For below link properties, we refer values in https://azure.github.io/azure-sdk/releases/latest/all/java.html.
- repopath: Maven repository url.
- msdocs: Documentation url of repository azure-docs-for-java.
- javadoc: Javadoc API url.
- github: Github repository url.
- springProperties: All spring specific properties copied from the template yaml.
- starter: Boolean value to mark if the library is a starter or not.
- bom: To map the bom package of each Spring library, we use this property to index the `id` property of a library's bom. 
- compatibilityRange: The compatible range for Spring Boot version.
- mappings: If a library shows different compatibilites towards different Spring Boot/Cloud version, we use this property for their version mapping.
- mappings.compatibilityRange: Spring Boot version range.
- mappings.groupId: The associated group-id of the target Azure Spring library version.
- mappings.artifactId: The associated artifact-id of the target Azure Spring library version.
- mappings.version: The target version of Azure Spring library.

