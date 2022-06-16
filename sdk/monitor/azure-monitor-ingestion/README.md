# Azure Monitor Ingestion client library for Java

The Azure Monitor Ingestion client library is used to send custom logs to [Azure Monitor][azure_monitor_overview].

This library allows you to send data from virtually any source to supported built-in tables or to custom tables 
that you create in Log Analytics workspace. You can even extend the schema of built-in tables with custom columns.

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- A [Data Collection Endpoint][data_collection_endpoint]
- A [Data Collection Rule][data_collection_rule]
- A [Log Analytics workspace][log_analytics_workspace]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-monitor-ingestion;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-ingestion</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

### Create the client

An authenticated client is required to upload logs to Azure Monitor. The library includes both synchronous and asynchronous forms of the clients. To authenticate, 
the following examples use `DefaultAzureCredentialBuilder` from the [com.azure:azure-identity](https://search.maven.org/artifact/com.azure/azure-identity) package.

#### Authenticating using Azure Active Directory
You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity].
To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:
[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.5.2</version>
</dependency>
```
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

#### Synchronous clients

```java readme-sample-createLogsIngestionClient
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint>")
        .credential(tokenCredential)
        .buildClient();
```

#### Asynchronous clients

```java readme-sample-createLogsIngestionAsyncClient
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionAsyncClient asyncClient = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint>")
        .credential(tokenCredential)
        .buildAsyncClient();
```
### Uploading logs

For examples on how to upload logs, see the [Examples](#examples) section.

## Key concepts

### Data Collection Endpoint

Data Collection Endpoints (DCEs) allow you to uniquely configure ingestion settings for Azure Monitor. [This 
article][data_collection_endpoint] provides an overview of data collection endpoints including their contents and structure and how you can create
and work with them.

### Data Collection Rule

Data collection rules define data collected by Azure Monitor and specify how and where that data should be sent or
stored. The REST API call must specify a DCR to use. A single DCE can support multiple DCRs, so you can specify a
different DCR for different sources and target tables.

The DCR must understand the structure of the input data and the structure of the target table. If the two don't match,
it can use a transformation to convert the source data to match the target table. You may also use the transform to
filter source data and perform any other calculations or conversions.

For more details, refer to [Data collection rules in Azure Monitor](https://docs.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-overview).

### Log Analytics Workspace Tables

Custom logs can send data to any custom table that you create and to certain built-in tables in your Log Analytics 
workspace. The target table must exist before you can send data to it. The following built-in tables are currently supported:

- [CommonSecurityLog](https://docs.microsoft.com/azure/azure-monitor/reference/tables/commonsecuritylog)
- [SecurityEvents](https://docs.microsoft.com/azure/azure-monitor/reference/tables/securityevent)
- [Syslog](https://docs.microsoft.com/azure/azure-monitor/reference/tables/syslog)
- [WindowsEvents](https://docs.microsoft.com/azure/azure-monitor/reference/tables/windowsevent)

## Examples

- [Upload custom logs](#upload-custom-logs)
- [Upload custom logs with max concurrency](#upload-custom-logs-with-max-concurrency)

### Upload custom logs

```java readme-sample-uploadLogs
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint")
        .credential(tokenCredential)
        .buildClient();

List<Object> logs = getLogs();
UploadLogsResult result = client.upload("<data-collection-rule-id>", "<stream-name>", logs);
System.out.println("Logs upload result status " + result.getStatus());
```

### Upload custom logs with max concurrency
```java readme-sample-uploadLogsWithMaxConcurrency
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint")
        .credential(tokenCredential)
        .buildClient();

List<Object> logs = getLogs();
UploadLogsOptions uploadLogsOptions = new UploadLogsOptions()
        .setMaxConcurrency(3);
UploadLogsResult result = client.upload("<data-collection-rule-id>", "<stream-name>", logs, uploadLogsOptions,
        Context.NONE);
System.out.println("Logs upload result status " + result.getStatus());
```

## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps
More samples can be found [here][samples].

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
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-ingestion/src/samples/java/com/azure/monitor/ingestion
[data_collection_endpoint]: https://docs.microsoft.com//azure/azure-monitor/essentials/data-collection-endpoint-overview
[data_collection_rule]: https://docs.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-overview
[log_analytics_workspace]: https://docs.microsoft.com//azure/azure-monitor/logs/log-analytics-workspace-overview
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmonitor%2Fazure-monitor-ingestion%2FREADME.png)
