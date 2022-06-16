# Azure Monitor Ingestion client library for Java

The Azure Monitor Ingestion client library is used to send custom logs to [Azure Monitor][azure_monitor_overview].

This library allows you to send data from virtually any source to supported built-in tables or to custom tables 
that you create in Log Analytics workspace. You can even extend the schema of built-in tables with custom columns.

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-monitor-ingestion;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-ingestion</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
## Key concepts

### Data Collection Endpoint

Data Collection Endpoints (DCEs) allow you to uniquely configure ingestion settings for Azure Monitor. This 
article provides an overview of data collection endpoints including their contents and structure and how you can create
and work with them.

### Tables
Custom logs can send data to any custom table that you create and to certain built-in tables in your Log Analytics 
workspace. The target table must exist before you can send data to it. The following built-in tables are currently supported:

- [CommonSecurityLog](https://docs.microsoft.com/azure/azure-monitor/reference/tables/commonsecuritylog)
- [SecurityEvents](https://docs.microsoft.com/azure/azure-monitor/reference/tables/securityevent)
- [Syslog](https://docs.microsoft.com/azure/azure-monitor/reference/tables/syslog)
- [WindowsEvents](https://docs.microsoft.com/azure/azure-monitor/reference/tables/windowsevent)

### Data Collection Rule

Data collection rules define data collected by Azure Monitor and specify how and where that data should be sent or 
stored. The REST API call must specify a DCR to use. A single DCE can support multiple DCRs, so you can specify a
different DCR for different sources and target tables.

The DCR must understand the structure of the input data and the structure of the target table. If the two don't match, 
it can use a transformation to convert the source data to match the target table. You may also use the transform to 
filter source data and perform any other calculations or conversions.

For more details, refer to [Data collection rules in Azure Monitor](https://docs.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-overview).

## Examples

## Troubleshooting

## Next steps

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution.
For details, visit [https://cla.microsoft.com](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the
PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this
once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/free

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmonitor%2Fazure-monitor-ingestion%2FREADME.png)
