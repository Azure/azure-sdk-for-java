# Azure Monitor Ingestion client library for Java

The Azure Monitor Ingestion client library is used to send custom logs to [Azure Monitor][azure_monitor_overview] using 
the [Logs Ingestion API][ingestion_overview].

This library allows you to send data from virtually any source to supported built-in tables or to custom tables 
that you create in Log Analytics workspace. You can even extend the schema of built-in tables with custom columns.

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Azure Subscription][azure_subscription]
- A [Data Collection Endpoint][data_collection_endpoint]
- A [Data Collection Rule][data_collection_rule]
- A [Log Analytics workspace][log_analytics_workspace]

### Include the package

#### Include the BOM file

Please include the `azure-sdk-bom` to your project to take a dependency on the latest stable version of the library. In 
the following snippet, replace the `{bom_version_to_target}` placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-ingestion</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-monitor-ingestion;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-ingestion</artifactId>
    <version>1.2.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create the client

An authenticated client is required to upload logs to Azure Monitor. The library includes both synchronous and asynchronous forms of the clients. To authenticate, 
the following examples use `DefaultAzureCredentialBuilder` from the [azure-identity](https://central.sonatype.com/artifact/com.azure/azure-identity/1.8.1) package.

#### Authenticating using Azure Active Directory
You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity].
To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.12.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

#### Synchronous Logs Ingestion client

```java readme-sample-createLogsIngestionClient
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint>")
        .credential(tokenCredential)
        .buildClient();
```

#### Asynchronous Logs Ingestion client

```java readme-sample-createLogsIngestionAsyncClient
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionAsyncClient asyncClient = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint>")
        .credential(tokenCredential)
        .buildAsyncClient();
```
## Key concepts

### Data Collection Endpoint

Data Collection Endpoints (DCEs) allow you to uniquely configure ingestion settings for Azure Monitor. [This 
article][data_collection_endpoint] provides an overview of data collection endpoints including their contents and 
structure and how you can create and work with them.

### Data Collection Rule

Data collection rules (DCR) define data collected by Azure Monitor and specify how and where that data should be sent or
stored. The REST API call must specify a DCR to use. A single DCE can support multiple DCRs, so you can specify a
different DCR for different sources and target tables.

The DCR must understand the structure of the input data and the structure of the target table. If the two don't match,
it can use a transformation to convert the source data to match the target table. You may also use the transform to
filter source data and perform any other calculations or conversions.

For more details, see [Data collection rules in Azure Monitor][data_collection_rule]. For information on how to retrieve 
a DCR ID, see [this tutorial][data_collection_rule_tutorial].

### Log Analytics Workspace Tables

Custom logs can send data to any custom table that you create and to certain built-in tables in your Log Analytics 
workspace. The target table must exist before you can send data to it. The following built-in tables are currently supported:

- [CommonSecurityLog](https://learn.microsoft.com/azure/azure-monitor/reference/tables/commonsecuritylog)
- [SecurityEvents](https://learn.microsoft.com/azure/azure-monitor/reference/tables/securityevent)
- [Syslog](https://learn.microsoft.com/azure/azure-monitor/reference/tables/syslog)
- [WindowsEvents](https://learn.microsoft.com/azure/azure-monitor/reference/tables/windowsevent)

### Logs retrieval
The logs that were uploaded using this library can be queried using the 
[Azure Monitor Query](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query#readme) 
client library.

## Examples

- [Upload custom logs](#upload-custom-logs)
- [Upload custom logs with max concurrency](#upload-custom-logs-with-max-concurrency)
- [Upload custom logs with error handling](#upload-custom-logs-with-error-handling)

### Upload custom logs

```java readme-sample-uploadLogs
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint>")
        .credential(tokenCredential)
        .buildClient();

List<Object> logs = getLogs();
client.upload("<data-collection-rule-id>", "<stream-name>", logs);
System.out.println("Logs uploaded successfully");
```

### Upload custom logs with max concurrency

If the in input logs collection is too large, the client will split the input into multiple smaller requests. These 
requests are sent serially, by default, but by configuring the max concurrency in `LogsUploadOptions`, these requests
can be concurrently sent to the service as shown in the example below.

```java readme-sample-uploadLogsWithMaxConcurrency
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint>")
        .credential(tokenCredential)
        .buildClient();

List<Object> logs = getLogs();
LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
        .setMaxConcurrency(3);
client.upload("<data-collection-rule-id>", "<stream-name>", logs, logsUploadOptions,
        Context.NONE);
System.out.println("Logs uploaded successfully");
```

### Upload custom logs with error handling

When uploading large collection of logs, the client splits the input into multiple smaller service requests. The upload 
method provides an option to handle individual service errors through an error handler as shown in the example below. 
This error handler include the exception details and the list of all logs that failed to upload. If an error handler is 
not provided, the upload method will throw an aggregate exception that includes all the service errors.

```java readme-sample-uploadLogs-error-handler
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint>")
        .credential(tokenCredential)
        .buildClient();

List<Object> logs = getLogs();

LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
        .setLogsUploadErrorConsumer(uploadLogsError -> {
            System.out.println("Error message " + uploadLogsError.getResponseException().getMessage());
            System.out.println("Total logs failed to upload = " + uploadLogsError.getFailedLogs().size());

            // throw the exception here to abort uploading remaining logs
            // throw uploadLogsError.getResponseException();
        });
client.upload("<data-collection-rule-id>", "<stream-name>", logs, logsUploadOptions,
        Context.NONE);
```
## Troubleshooting

For details on diagnosing various failure scenarios, see our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-ingestion/TROUBLESHOOTING.md).

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
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[azure_monitor_overview]: https://learn.microsoft.com/azure/azure-monitor/overview
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[data_collection_endpoint]: https://learn.microsoft.com//azure/azure-monitor/essentials/data-collection-endpoint-overview
[data_collection_rule]: https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-overview
[data_collection_rule_tutorial]: https://learn.microsoft.com/azure/azure-monitor/logs/tutorial-logs-ingestion-portal#collect-information-from-the-dcr
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[ingestion_overview]: https://learn.microsoft.com/azure/azure-monitor/logs/logs-ingestion-api-overview
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[log_analytics_workspace]: https://learn.microsoft.com//azure/azure-monitor/logs/log-analytics-workspace-overview
[logging]: https://learn.microsoft.com//azure/developer/java/sdk/logging-overview
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-ingestion/src/samples/java/com/azure/monitor/ingestion
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmonitor%2Fazure-monitor-ingestion%2FREADME.png)
