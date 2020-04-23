# Azure BOM for client libraries
The Azure BOM for client libraries provides a verified group of artifacts that are known to share a common dependency set. It provides a simple and elegant way to manage dependencies on multiple Azure client libraries.  

## Table of contents
- [Getting started](#getting-started)
  - [Adding the BOM to your project](#adding-the-bom-to-your-project)
  - [Adding libraries to your project](#adding-libraries-to-your-project)

## Getting started

### Adding the BOM to your project

To consume the BOM include it in the `dependencyManagement` section of your project's POM. Note that this **does not** result in all dependencies being included in your project.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-sdk-bom</artifactId>
      <version>1.0.0-beta.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### Adding libraries to your project

After adding the BOM all artifacts included in the BOM are now available to be added as a dependency without listing the artifact's version.

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
