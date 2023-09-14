# Azure Monitor OpenTelemetry Distro / Application Insights in Spring native Java application

This Spring Boot starter provides telemetry data to Azure Monitor for Spring Boot applications and GraalVM native images.

_For a Spring Boot application running on a JVM (not with a GraalVM native image), we recommend using the [Application Insights Java agent][application_insights_java_agent_spring_boot].

[Source code][source_code] | (Package yet to release) | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation]

## Getting started

### Prerequisites

- [Azure Subscription][azure_subscription]
- [Application Insights resource][application_insights_resource]

For more information, please read [introduction to Application Insights][application_insights_intro].

### Include the dependency

[//]: # ({x-version-update-start;com.azure:azure-monitor-azure-monitor-spring-native;current})
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>spring-cloud-azure-starter-monitor</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

You may have to align the OpenTelemetry versions of Spring Boot 3 and `spring-cloud-azure-starter-monitor`. If this is the case, you will notice a WARN message during the application start-up:
```
WARN  c.a.m.a.s.OpenTelemetryVersionCheckRunner - The OpenTelemetry version is not compatible with the spring-cloud-azure-starter-monitor dependency. The OpenTelemetry version should be
```
To fix this with Maven, you can set the `opentelemetry.version` property:

```xml
<properties>
   <opentelemetry.version>{otel-version-given-in-the-warn-log}</opentelemetry.version>
</properties>
```

Another way is to declare the `opentelemetry-bom` BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-bom</artifactId>
            <version>{otel-version-given-in-the-warn-log}</version>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

With Gradle, you can fix the issue in this way:

```
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

dependencyManagement {
    imports {
        mavenBom 'io.opentelemetry:opentelemetry-bom:{otel-version-given-in-the-warn-log}'
    }
}
```

### Authentication

#### Get the instrumentation key from the portal

In order to export telemetry data to Azure Monitor, you will need the connection string to your [Application
 Insights resource][application_insights_resource]. Go to [Azure Portal][azure_portal], 
search for your resource. On the overview page of your resource, you will find the connection string in the top
right corner.

#### Configure the connection string
You can then configure the connection string in two different ways:
* With the `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable
* With the `applicationinsights.connection.string` system property. You can use `-Dapplicationinsights.connection.string` or add the property to your `application.properties` file.

### Additional instrumentations
You can configure additional instrumentations with [OpenTelemetry instrumentations libraries](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md#libraries--frameworks).

### Build your Spring native application
At this step, you can build your application as a native image and start the native image:

```
mvn -Pnative spring-boot:build-image
docker run -e APPLICATIONINSIGHTS_CONNECTION_STRING="{CONNECTION_STRING}" {native-image-name} 
```
where you have to replace `{CONNECTION_STRING}` and `{native-image-name}` by your connection string and the native image name.

## Debug your Spring native application

If something does not work as expected, you can enable self-diagnostics features at DEBUG level to get some insights.

You can configure the self-diagnostics level by using the APPLICATIONINSIGHTS_SELF_DIAGNOSTICS_LEVEL environment variable. You can configure the level with ERROR, WARN, INFO, DEBUG, or TRACE.

_The APPLICATIONINSIGHTS_SELF_DIAGNOSTICS_LEVEL environment variable only works for Logback today._

The following line shows you how to add self-diagnostics at the DEBUG level to a Spring native image:
```
docker run -e -e APPLICATIONINSIGHTS_SELF_DIAGNOSTICS_LEVEL=DEBUG {native-image-name}
```

You have to replace `{native-image-name}` by your native image name.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the
[Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/spring-cloud-azure-starter-monitor/src
<!-- [package_mvn]: https://central.sonatype.com/artifact/com.azure.spring/spring-cloud-azure-starter-monitor -->
[api_reference_doc]: https://docs.microsoft.com/azure/azure-monitor/overview
[product_documentation]: https://docs.microsoft.com/azure/azure-monitor/overview
[azure_subscription]: https://azure.microsoft.com/free/
[application_insights_resource]: https://docs.microsoft.com/azure/azure-monitor/app/create-new-resource
[application_insights_intro]: https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview
[application_insights_java_agent_spring_boot]: https://learn.microsoft.com/azure/azure-monitor/app/java-spring-boot
[azure_portal]: https://portal.azure.com
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%monitor%2Fazure-monitor-spring-native%2FREADME.png)
