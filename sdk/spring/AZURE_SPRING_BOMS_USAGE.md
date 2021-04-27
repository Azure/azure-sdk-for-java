# How to use azure spring BOMs

### Adding the azure-spring-boot-bom BOM

To consume the azure spring boot BOM include it in the `dependencyManagement` section of your project's POM. 
Note that this **does not** result in all dependencies being included in your project.

[//]: # ({x-version-update-start;com.azure.spring:azure-spring-boot-bom;dependency})
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-boot-bom</artifactId>
      <version>3.4.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})

### Adding the azure-spring-cloud-dependencies BOM

To consume the azure spring cloud dependencies BOM include it in the `dependencyManagement` section of your project's POM.
Note that this **does not** result in all dependencies being included in your project.

[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-dependencies;dependency})
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>azure-spring-cloud-dependencies</artifactId>
      <version>2.4.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})
