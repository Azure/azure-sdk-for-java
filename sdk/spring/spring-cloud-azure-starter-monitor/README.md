# Azure Monitor OpenTelemetry Distro / Application Insights in Spring Boot native image Java application

This project is an Azure distribution of the [OpenTelemetry Spring Boot starter][otel_spring_starter].

It allows you to get telemetry data on Azure with a [Spring Boot native image application][spring_boot_native].

For a Spring Boot application running on a JVM runtime (not with a GraalVM native image), we recommend using the [Application Insights Java agent][application_insights_java_agent_spring_boot].

[Source code][source_code] | [Package (Maven)][package_mvn] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation]

## Getting started

### Prerequisites

- [Azure Subscription][azure_subscription]
- [Application Insights resource][application_insights_resource]

For more information, please read [introduction to Application Insights][application_insights_intro].

### Build update

#### OpenTelemetry configuration

Import the OpenTelemetry Bills of Materials (BOM)
by [following the OpenTelemetry documentation](https://opentelemetry.io/docs/instrumentation/java/automatic/spring-boot/#dependency-management).

#### Add monitoring dependency

Add the [Spring Cloud Azure Starter Monitor](https://central.sonatype.com/artifact/com.azure.spring/spring-cloud-azure-starter-monitor) dependency.

#### Required native image build configuration

[Instruction][azure_native] for Spring Boot native image applications.

### Enable OpenTelemetry features

All the features described in the [OpenTelemetry Spring Boot starter][otel_spring_starter] documentation work with Azure Monitor OpenTelemetry Distro / Application Insights in Spring Boot native image Java application.

You have just to replace the `opentelemetry-spring-boot-starter` dependency by the `spring-cloud-azure-starter-monitor` one.

### Azure Monitor configuration

#### Connection String

In order to export telemetry data to Azure Monitor, you will need the connection string to your [Application
 Insights resource][application_insights_resource]. Go to [Azure Portal][azure_portal], 
search for your resource. On the overview page of your resource, you will find the connection string in the top
right corner.

You can then configure the connection string in two different ways:
* With the `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable
* With the `applicationinsights.connection.string` system property. You can use `-Dapplicationinsights.connection.string` or add the property to your `application.properties` file.

#### Cloud role name

The [Application Map](https://learn.microsoft.com/azure/azure-monitor/app/app-map?tabs=net#set-or-override-cloud-role-name) uses the cloud role name to identify components on the map.

You can set the cloud role name with the `spring.application.name` property. You have other options to set the cloud role role: see how to set the service name in the [OpenTelemetry documentation](https://opentelemetry.io/docs/languages/java/automatic/spring-boot/).

### Build your Spring native application
At this step, you can build your Spring Boot native image application and start it:

```
mvn -Pnative spring-boot:build-image
docker run -e APPLICATIONINSIGHTS_CONNECTION_STRING="{CONNECTION_STRING}" {image-name} 
```
where you have to replace `{CONNECTION_STRING}` and `{image-name}` by your connection string and the native image name.

### Disable the monitoring

You can disable the monitoring by setting the `otel.sdk.disabled` property or the `OTEL_SDK_DISABLED` environment variable to true.

### Troubleshooting

#### Debug

If something does not work as expected, you can enable self-diagnostics features at DEBUG level to get some insights.

You can configure the self-diagnostics level by using the APPLICATIONINSIGHTS_SELF_DIAGNOSTICS_LEVEL environment variable. You can configure the level with ERROR, WARN, INFO, DEBUG, or TRACE.

_The APPLICATIONINSIGHTS_SELF_DIAGNOSTICS_LEVEL environment variable only works for Logback today._

The following line shows you how to add self-diagnostics at the DEBUG level when running a docker container:
```
docker run -e APPLICATIONINSIGHTS_SELF_DIAGNOSTICS_LEVEL=DEBUG {image-name}
```

You have to replace `{image-name}` by your docker image name.

#### OpenTelemetry version issue

You may notice the following message during the application start-up:
```
WARN  c.a.m.a.s.OpenTelemetryVersionCheckRunner - The OpenTelemetry version is not compatible with the spring-cloud-azure-starter-monitor dependency. The OpenTelemetry version should be
```

In this case, you have to import the OpenTelemetry Bills of Materials
by [following the OpenTelemetry documentation](https://opentelemetry.io/docs/instrumentation/java/automatic/spring-boot/#dependency-management).


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
[otel_spring_starter]: https://opentelemetry.io/docs/instrumentation/java/automatic/spring-boot/
[otel_spring_starter_instrumentation]: https://opentelemetry.io/docs/instrumentation/java/automatic/spring-boot/#additional-instrumentations
[spring_boot_native]: https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html
[azure_native]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/README.md#spring-aot-and-spring-native-images
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/spring-cloud-azure-starter-monitor/src
[package_mvn]: https://central.sonatype.com/artifact/com.azure.spring/spring-cloud-azure-starter-monitor
[api_reference_doc]: https://opentelemetry.io/docs/instrumentation/java/automatic/spring-boot/
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
