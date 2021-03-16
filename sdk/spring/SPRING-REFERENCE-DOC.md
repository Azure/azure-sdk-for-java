# Spring Reference Yaml Documentation

## Context
sdk/spring/spring-reference.yml is aimed to build package reference of Azure Spring libraries to support an Intellij plugin for Azure SDK reference. 

The yaml follows structure of [Spring Initializr's library mapping schema](https://github.com/spring-io/start.spring.io/blob/master/start-site/src/main/resources/application.yml) and thus is compatible of keys under property of `dependencies` to describe a library.
To make things clear, we call sdk/spring/spring-reference.yml as Spring yaml, and Spring Initializr's library mapping schema as template yaml.
For those keys not listed in Spring yaml, we can add them in case of future usage.

Hierarchy of Spring yaml is organized as : Service -> Feature -> Package. Taking Cosmos DB starter as example, it is described as Cosmos DB -> Spring Data Cosmos -> azure-spring-boot-starter-cosmos. For the value of service and feature, we define two rules:
 - For Spring library of which the underlying package is covered by [java-packages.csv](https://github.com/Azure/azure-sdk/blob/master/_data/releases/latest/java-packages.csv), we use its underlying library's `ServiceName` and `DisplayName` as the service and feature.
 - For the rest like AAD starter, we create the service and feature value ourselves, and try to follow the specification of java-pacakges.csv.
 
For the link properties of repopath, msdocs, javadoc and github, we refer values in https://azure.github.io/azure-sdk/releases/latest/all/java.html.

## Property Introduction

 - name: Service name

