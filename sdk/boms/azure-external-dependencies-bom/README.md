# Azure external dependencies BOM for libraries and services
The Azure external dependencies BOM for libraries provides all shared artifacts that are used in Java development for
Azure. It provides a simple and elegant way to manage dependencies for multiple Azure libraries and services.

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
      <artifactId>azure-external-dependencies-bom</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### Adding libraries to your project

After adding the BOM all artifacts included in the BOM are now available to be added as a dependency without listing the
artifact's version.

```xml
<dependencies>
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
  </dependency>
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
  </dependency>
  <dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
  </dependency>
</dependencies>
```
