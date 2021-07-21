# How to use Azure Spring BOMs

To add Azure Spring BOMs, please add the following fragment in your pom.xml.
You can refer to the [maven doc] about more details about dependency management.

## Add azure-spring-boot-bom

[//]: # ({x-version-update-start;com.azure.spring:azure-spring-boot-bom;current})
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-boot-bom</artifactId>
      <version>3.7.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})

## Add azure-spring-cloud-dependencies

[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-dependencies;current})
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-cloud-dependencies</artifactId>
      <version>2.7.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})

[maven doc]: https://maven.apache.org/