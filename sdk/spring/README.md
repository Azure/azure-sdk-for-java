# Spring Cloud Azure

Spring Cloud Azure offers a convenient way to interact with **Azure** provided services using well-known Spring idioms and APIs for Spring developers. 

 - [Reference doc](https://aka.ms/spring/docs).
 - [Migration guide for 4.0](https://aka.ms/spring/docs#migration-guide-for-4-0).
 - [Spring Boot Support Status](https://aka.ms/spring/versions)

## Build from Source

To check out the project and build it from source, do the following:

```shell
git clone git@github.com:Azure/azure-sdk-for-java.git
cd azure-sdk-for-java
mvn clean package -f sdk/spring/pom.xml -P dev
```

You can use the following command to install jars into your local repository quickly:

```shell
mvn clean install \
  -Dcheckstyle.skip=true \
  -Dcodesnippet.skip \
  -Denforcer.skip \
  -Djacoco.skip=true \
  -Dmaven.javadoc.skip=true \
  -Drevapi.skip=true \
  -DskipTests \
  -Dspotbugs.skip=true \
  -Pdev \
  -T 4 \
  -ntp \
  -f sdk/spring/pom.xml
```

If you are using PowerShell, please use the following command instead:
```powershell
mvn clean install `
 "-Dcheckstyle.skip" `
 "-Dcodesnippet.skip" `
 "-Denforcer.skip" `
 "-Djacoco.skip" `
 "-Dmaven.javadoc.skip" `
 "-Drevapi.skip" `
 "-DskipTests" `
 "-Dspotbugs.skip" `
 -Pdev `
 -T 4 `
 -ntp `
 -f sdk/spring/pom.xml
```



## Modules

There are several modules in Spring Cloud Azure. Here is a quick review:

### spring-cloud-azure-autoconfigure

Auto-configuration attempts to deduce which beans a user might need. For example, if `Cosmos DB` is on the classpath, and the user has not configured any Cosmos DB clients, then they probably want an Cosmos DB client to be defined. Auto-configuration will always back away as the user starts to define their own beans. 

This module contains the auto-configuration code for Azure services. 

### spring-cloud-azure-starters

Spring Cloud Azure Starters are a set of convenient dependency descriptors to include in your application. It boosts your Spring Boot application developement with Azure services. For example, if you want to get started using Spring and Azure Cosmos DB for data persistence, include the `spring-cloud-azure-starter-cosmos` dependency in your project. 

The following application starters are provided by Spring Cloud Azure under the `com.azure.spring` group:

| Name                                                 | Description                                                        |
|------------------------------------------------------|--------------------------------------------------------------------|
| spring-cloud-azure-starter                           | Core starter, including auto-configuration support                 |
| spring-cloud-azure-starter-active-directory          | Starter for using Microsoft Entra ID with Spring Security          |
| spring-cloud-azure-starter-active-directory-b2c      | Starter for using Azure Active Directory B2C with Spring Security  |
| spring-cloud-azure-starter-appconfiguration          | Starter for using Azure App Configuration                          |
| spring-cloud-azure-starter-cosmos                    | Starter for using Azure Cosmos DB                                  |
| spring-cloud-azure-starter-data-cosmos               | Starter for using Azure Cosmos DB and Spring Data Cosmos DB        |
| spring-cloud-azure-starter-eventhubs                 | Starter for using Azure Event Hubs                                 |
| spring-cloud-azure-starter-eventgrid                 | Starter for using Azure Event Grid                                 |
| spring-cloud-azure-starter-integration-eventhubs     | Starter for using Azure Event Hubs and Spring Integration          |
| spring-cloud-azure-starter-integration-servicebus    | Starter for using Azure Service Bus and Spring Integration         |
| spring-cloud-azure-starter-integration-storage-queue | Starter for using Azure Storage Queue and Spring Integration       |
| spring-cloud-azure-starter-keyvault-secrets          | Starter for using Azure Key Vault Secrets                          |
| spring-cloud-azure-starter-keyvault-certificates     | Starter for using Azure Key Vault Certificates                     |
| spring-cloud-azure-starter-servicebus                | Starter for using Azure Service Bus                                |
| spring-cloud-azure-starter-servicebus-jms            | Starter for using Azure Service Bus and JMS                        |
| spring-cloud-azure-starter-storage-blob              | Starter for using Azure Storage Blob                               |
| spring-cloud-azure-starter-storage-file-share        | Starter for using Azure Storage File Share                         |
| spring-cloud-azure-starter-storage-queue             | Starter for using Azure Storage Queue                              |
| spring-cloud-azure-starter-stream-eventhubs          | Starter for using Azure Event Hubs and Spring Cloud Stream Binder  |
| spring-cloud-azure-starter-stream-servicebus         | Starter for using Azure Service Bus and Spring Cloud Stream Binder |


In addition to the application starters, the following starters can be used to add `production ready` features:

| Name                                | Description                                                                                                                       |
|-------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| spring-cloud-azure-starter-actuator | Starter for using Spring Boot’s Actuator which provides production ready features to help you monitor and manage your application |

### spring-cloud-azure-actuator

Actuator endpoints let you monitor and interact with your application. `Spring Boot Actuator` provides the infrastructure required for actuator endpoints. 

This module extends the `Spring Boot Actuator` module and provides the actuating support for Azure services.

### spring-cloud-azure-actuator-autoconfigure

This provides auto-configuration for actuator endpoints based on the content of the classpath and a set of properties. 

### spring-integration-azure

Spring Integration Extension for Azure provides Spring Integration adapters for the various services provided by the [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java/). Below is a list of supported adapters:

- spring-integration-azure-eventhbus
- spring-integration-azure-servicebus
- spring-integration-azure-storage-queue

### spring-cloud-azure-stream-binder

Spring Cloud Stream is a framework for building highly scalable event-driven microservices connected with shared messaging systems.

The framework provides a flexible programming model built on already established and familiar Spring idioms and best practices, including support for persistent pub/sub semantics, consumer groups, and stateful partitions.

Current binder implementations include:

- spring-cloud-azure-stream-binder-eventhubs
- spring-cloud-azure-stream-binder-servicebus

## Spring Cloud Azure Bill of Materials (BOM)

If you’re a Maven user, add our BOM to your pom.xml `<dependencyManagement>` section. This will allow you to not specify versions for any of the Maven dependencies and instead delegate versioning to the BOM.

[//]: # ({x-version-update-start;com.azure.spring:spring-cloud-azure-dependencies;current})
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-dependencies</artifactId>
            <version>5.13.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})

With Gradle, you can import the `spring-cloud-azure-dependencies` BOM in [two ways](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/).

You can use the Gradle’s native BOM support by adding dependencies:

```kotlin
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
  id("java")
  id("org.springframework.boot") version "3.2.O"
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(platform("com.azure.spring:spring-cloud-azure-dependencies:{version}"))
}
```

You can also use the `io.spring.dependency-management` plugin and import the BOM in `dependencyManagement`:

```kotlin
plugins {
    id("io.spring.dependency-management") version "1.1.0"
}

dependencyManagement {
    imports { 
        mavenBom("com.azure.spring:spring-cloud-azure-dependencies:{version}")
    }
}
```

## Spring Boot 3 Support

The source code of Spring Cloud Azure for Spring Boot 3.x can be found on the [feature/spring-boot-3](https://github.com/Azure/azure-sdk-for-java/tree/feature/spring-boot-3) branch.

#### Spring AOT and Spring native images

Azure SDK JARs are signed. [Spring Boot 3 does not support today signed JARs](https://github.com/Azure/azure-sdk-for-java/issues/30320) when you run your application with [AOT mode on a JVM](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment.efficient.aot) or you [build a native image](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html).

You can disable the JAR signature verification in the following way for GraalVM Native Build Tools:

* Maven
```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <configuration>
        <buildArgs>
            <arg>-Djava.security.properties=src/main/resources/custom.security</arg>
        </buildArgs>
    </configuration>
</plugin>
```

* Gradle:
```groovy
graalvmNative {
  binaries {
    main {
      buildArgs('-Djava.security.properties=' + file("$rootDir/custom.security").absolutePath)
    }
  }
}
```

You have to create a `custom.security file` in `src/main/resources` with the following content:
```
jdk.jar.disabledAlgorithms=MD2, MD5, RSA, DSA
```

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][spring-contributing] to build from source or contribute.

### Filing Issues

If you encounter any bug, please file an issue [here][azure-sdk-for-java-issues].

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

You can participate community driven [![Gitter][gitter-spring-on-azure-img]][gitter-spring-on-azure]

### Pull Requests

Pull requests are welcome. To open your own pull request, click [here][azure-sdk-for-java-compare]. When creating a pull request, make sure you are pointing to the fork and branch that your changes were made in.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct][codeofconduct]. For more information see the [Code of Conduct FAQ][codeofconduct-faq] or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy][privacy-statement] statement to learn more.



[spring-contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md
[azure-sdk-for-java-issues]: https://github.com/Azure/azure-sdk-for-java/issues
[gitter-spring-on-azure-img]: https://badges.gitter.im/Microsoft/spring-on-azure.svg
[gitter-spring-on-azure]: https://gitter.im/Microsoft/spring-on-azure
[azure-sdk-for-java-compare]: https://github.com/Azure/azure-sdk-for-java/compare
[codeofconduct]: https://opensource.microsoft.com/codeofconduct/faq/
[codeofconduct-faq]: https://opensource.microsoft.com/codeofconduct/faq/
[privacy-statement]: https://privacy.microsoft.com/privacystatement

