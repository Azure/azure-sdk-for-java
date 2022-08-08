# Azure Application Insights Query client library for Java

> A newer package [com.azure:azure-monitor-query](https://search.maven.org/artifact/com.azure/azure-monitor-query)
> for [Azure Application Insights Query](https://azure.microsoft.com/services/monitor/) is available as of October 2021. While this
> package will continue to receive critical bug fixes and security fixes, we strongly encourage you to upgrade to the new package.
> Read the [migration guide](https://aka.ms/azsdk/java/migrate/monitorquery) for more details.


This project provides client tools or utilities in Java to query data in [Azure Application Insights](https://azure.microsoft.com/services/application-insights/). For reference documentation on classes and models, see the [Azure SDK for Java reference](https://docs.microsoft.com/java/api/overview/azure/?view=azure-java-stable). 

Azure Application Insights provides SDKs for telemetry collection and enables deep analytics via a [rich query language](https://docs.microsoft.com/azure/azure-monitor/logs/log-analytics-overview). This library provides query access to data already stored in Application Insights. To start monitoring a Java application, see the [quickstart](https://docs.microsoft.com/azure/application-insights/app-insights-java-quick-start). 

## Examples

Please see [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/applicationinsights/microsoft-azure-applicationinsights-query/samples/src/main/java/com/microsoft/azure/applicationinsights/query/samples) for code examples using this library. 

## Download

### Latest release

To get the binaries of the official Microsoft Azure Log Analytics SDK as distributed by Microsoft, ready for use within your project, you can use Maven.

[//]: # ({x-version-update-start;com.microsoft.azure:azure-applicationinsights-query;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-applicationinsights-query</artifactId>
    <version>1.0.0-Preview-2</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Prerequisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven

## Help and Issues

If you encounter any bugs with this library, open an issue via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or see [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](https://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## More information
- [Azure Java SDKs](https://docs.microsoft.com/java/azure/)
- If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](https://go.microsoft.com/fwlink/?LinkId=330212)

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fapplicationinsights%2Fmicrosoft-azure-applicationinsights-query%2FREADME.png)
