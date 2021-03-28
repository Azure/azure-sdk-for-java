# Azure BOM for client libraries
The Azure BOM for client libraries provides a verified group of Azure client libraries that are known to share a common 
dependency set and the dependencies they use. It provides a simple and elegant way to orchestrate using multiple Azure 
client libraries and ensuring minimal dependency conflicts.

## Table of contents
- [Getting started](#getting-started)
  - [Adding the BOM to your project](#adding-the-bom-to-your-project)
  - [Adding libraries to your project](#adding-libraries-to-your-project)
  - [Adding library dependencies to your project](#adding-library-dependencies-to-your-project)

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
      <version>1.0.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

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

### Adding library dependencies to your project

In addition to containing Azure client libraries the BOM also list dependencies that the Azure client libraries use.
These are added to allow the BOM to configure them to reduce dependency conflict on commonly used libraries such as
Jackson, Netty, OkHttp, SLF4J, and more.

```xml
<dependencies>
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
  </dependency>
  <dependency>
    <groupId>io.projectreactor.netty</groupId>
    <artifactId>reactor-netty</artifactId>
  </dependency>
  <dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
  </dependency>
</dependencies>
```
