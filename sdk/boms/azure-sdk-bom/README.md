# Azure BOM for client libraries
The Azure BOM for client libraries provides a verified group of Azure client libraries that are known to share common 
dependencies. It provides a simple and elegant way to orchestrate using multiple Azure client libraries while ensuring 
minimal dependency conflicts.

## Table of contents
- [Getting started](#getting-started)
  - [Adding the BOM to your project](#adding-the-bom-to-your-project)
  - [Adding libraries to your project](#adding-libraries-to-your-project)

## Getting started

### Adding the BOM to your project

To consume the BOM include it in the `dependencyManagement` section of your project's POM. Note that this **does not**
result in all dependencies being included in your project.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-sdk-bom</artifactId>
      <version>1.2.25</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

**NOTE**: In **Spring Boot** application, you can choose **azure-sdk-bom**'s version according to [Spring-Versions-Mapping](https://aka.ms/spring/versions).

### Adding libraries to your project

After adding the BOM, Azure client libraries included in the BOM are now available to be added as a dependency without 
listing the artifact's version.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
  </dependency>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
  </dependency>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
  </dependency>
</dependencies>
```

### Overview

#### Release cadence

Currently, the `azure-sdk-bom` releases monthly using the latest generally available(GA) versions of managed libraries.

#### Included library requirements

Only GA'd Azure SDKs in the `com.azure` group are permitted to be managed dependencies in the `azure-sdk-bom`. SDKs 
that have yet to be GA'd won't be considered at this time as the `azure-sdk-bom` is meant to act as a production ready 
dependency management system for applications built using Azure SDKs.
