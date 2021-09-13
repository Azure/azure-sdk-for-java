# Azure Monitor Query client library for Java

The Azure Monitor Query client library is used to execute read-only queries against [Azure Monitor](https://docs.microsoft.com/azure/azure-monitor/overview)'s two data platforms:

- [Logs](https://docs.microsoft.com/azure/azure-monitor/logs/data-platform-logs) - Collects and organizes log and performance data from monitored resources. Data from different sources such as platform logs from Azure services, log and performance data from virtual machines agents, and usage and performance data from apps can be consolidated into a single [Azure Log Analytics workspace](https://docs.microsoft.com/azure/azure-monitor/logs/data-platform-logs#log-analytics-workspaces). The various data types can be analyzed together using the [Kusto Query Language][kusto_query_language].
- [Metrics](https://docs.microsoft.com/azure/azure-monitor/essentials/data-platform-metrics) - Collects numeric data from monitored resources into a time series database. Metrics are numerical values that are collected at regular intervals and describe some aspect of a system at a particular time. Metrics are lightweight and capable of supporting near real-time scenarios, making them particularly useful for alerting and fast detection of issues.

**Resources:**

- [Source code][source]
- [Package (Maven)][package]
- [Service documentation][azure_monitor_overview]
- [Samples][samples]
- [Change log][changelog]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later
- An [Azure subscription][azure_subscription]
- To query Logs, you need an [Azure Log Analytics workspace][azure_monitor_create_using_portal].
- To query Metrics, you need an Azure resource of any kind (Storage Account, Key Vault, Cosmos DB, etc.).

### Install the package

Install the Azure Monitor Query client library for Java by adding the following to your *pom.xml* file:

[//]: # ({x-version-update-start;com.azure:azure-monitor-query;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-query</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Create the client

An authenticated client is required to query Logs or Metrics. The library includes both synchronous and asynchronous forms of the clients. To authenticate, the following examples use `DefaultAzureCredentialBuilder` from the [com.azure:azure-identity](https://search.maven.org/artifact/com.azure/azure-identity) package.

#### Synchronous clients

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L39-L41 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L52-L54 -->
```java
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Asynchronous clients

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L43-L45 -->
```java
LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L56-L58 -->
```java
MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

## Key concepts

### Metrics data structure

Each set of metric values is a time series with the following characteristics:

- The time the value was collected
- The resource associated with the value
- A namespace that acts like a category for the metric
- A metric name
- The value itself
- Some metrics may have multiple dimensions as described in multi-dimensional metrics. Custom metrics can have up to 10 dimensions.

## Examples

- [Logs query](#logs-query)
  - [Map logs query results to a model](#map-logs-query-results-to-a-model)
  - [Handle logs query response](#handle-logs-query-response)
- [Batch logs query](#batch-logs-query)
- [Advanced logs query scenarios](#advanced-logs-query-scenarios)
  - [Set logs query timeout](#set-logs-query-timeout)
  - [Query multiple workspaces](#query-multiple-workspaces)
- [Metrics query](#metrics-query)
  - [Handle metrics query response](#handle-metrics-query-response)
  - [Get average and count metrics](#get-average-and-count-metrics)

### Logs query

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L65-L74 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

LogsQueryResult queryResults = logsQueryClient.query("{workspace-id}", "{kusto-query}",
        new TimeInterval(Duration.ofDays(2)));

for (LogsTableRow row : queryResults.getTable().getRows()) {
    System.out.println(row.getColumnValue("OperationName") + " " + row.getColumnValue("ResourceGroup"));
}
```

#### Map logs query results to a model

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L80-L91 -->
```java
public class CustomLogModel {
    private String resourceGroup;
    private String operationName;

    public String getResourceGroup() {
        return resourceGroup;
    }

    public String getOperationName() {
        return operationName;
    }
}
```

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L97-L106 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

List<CustomLogModel> customLogModels = logsQueryClient.query("{workspace-id}", "{kusto-query}",
        new TimeInterval(Duration.ofDays(2)), CustomLogModel.class);

for (CustomLogModel customLogModel : customLogModels) {
    System.out.println(customLogModel.getOperationName() + " " + customLogModel.getResourceGroup());
}
```

#### Handle logs query response

The `query` API returns the `LogsQueryResult`, while the `queryBatch` API returns the `LogsBatchQueryResult`. Here's a hierarchy of the response:

```
LogsQueryResult / LogsBatchQueryResult
|---id (this exists in `LogsBatchQueryResult` object only)
|---status (this exists in `LogsBatchQueryResult` object only)
|---statistics
|---visualization
|---error
|---tables (list of `LogsTable` objects)
    |---name
    |---rows (list of `LogsTableRow` objects)
        |--- rowIndex
        |--- rowCells (list of `LogsTableCell` objects)
    |---columns (list of `LogsTableColumn` objects)
        |---name
        |---type
```

### Batch logs query

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L113-L138 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
String query1 = logsBatchQuery.addQuery("{workspace-id}", "{query-1}", new TimeInterval(Duration.ofDays(2)));
String query2 = logsBatchQuery.addQuery("{workspace-id}", "{query-2}", new TimeInterval(Duration.ofDays(30)));
String query3 = logsBatchQuery.addQuery("{workspace-id}", "{query-3}", new TimeInterval(Duration.ofDays(10)));

LogsBatchQueryResults batchResults = logsQueryClient
        .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

LogsBatchQueryResult query1Result = batchResults.getResult(query1);
for (LogsTableRow row : query1Result.getTable().getRows()) {
    System.out.println(row.getColumnValue("OperationName") + " " + row.getColumnValue("ResourceGroup"));
}

List<CustomLogModel> customLogModels = batchResults.getResult(query2, CustomLogModel.class);
for (CustomLogModel customLogModel : customLogModels) {
    System.out.println(customLogModel.getOperationName() + " " + customLogModel.getResourceGroup());
}

LogsBatchQueryResult query3Result = batchResults.getResult(query3);
if (query3Result.hasFailed()) {
    System.out.println(query3Result.getError().getMessage());
}
```

### Advanced logs query scenarios

#### Set logs query timeout

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L146-L155 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

// set request options: server timeout
LogsQueryOptions options = new LogsQueryOptions()
    .setServerTimeout(Duration.ofMinutes(10));

Response<LogsQueryResult> response = logsQueryClient.queryWithResponse("{workspace-id}",
        "{kusto-query}", new TimeInterval(Duration.ofDays(2)), options, Context.NONE);
```

#### Query multiple workspaces

To run the same query against multiple Log Analytics workspaces, use the `LogsQueryOptions.setAdditionalWorkspaces` method:

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L162-L170 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

Response<LogsQueryResult> response = logsQueryClient.queryWithResponse("{workspace-id}", "{kusto-query}",
        new TimeInterval(Duration.ofDays(2)), new LogsQueryOptions()
                .setAdditionalWorkspaces(Arrays.asList("{additional-workspace-identifiers}")),
        Context.NONE);
LogsQueryResult result = response.getValue();
```

### Metrics query

A resource ID, as denoted by the `{resource-id}` placeholder in the sample below, is required to query metrics. To find the resource ID:

1. Navigate to your resource's page in the Azure portal.
2. From the **Overview** blade, select the **JSON View** link.
3. In the resulting JSON, copy the value of the `id` property.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L178-L193 -->
```java
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

MetricsQueryResult metricsQueryResult = metricsQueryClient.query("{resource-uri}",
        Arrays.asList("SuccessfulCalls", "TotalCalls"));

for (MetricResult metric : metricsQueryResult.getMetrics()) {
    System.out.println("Metric name " + metric.getMetricName());
    for (TimeSeriesElement timeSeriesElement : metric.getTimeSeries()) {
        System.out.println("Dimensions " + timeSeriesElement.getMetadata());
        for (MetricValue metricValue : timeSeriesElement.getValues()) {
            System.out.println(metricValue.getTimeStamp() + " " + metricValue.getTotal());
        }
    }
}
```

#### Handle metrics query response

The metrics query API returns a `MetricsQueryResult` object. The `MetricsQueryResult` object contains properties such as a list of `MetricResult`-typed objects, `granularity`, `namespace`, and `timeInterval`. The `MetricResult` objects list can be accessed using the `metrics` param. Each `MetricResult` object in this list contains a list of `TimeSeriesElement` objects. Each `TimeSeriesElement` contains `data` and `metadata_values` properties. In visual form, the object hierarchy of the response resembles the following structure:

```
MetricsQueryResult
|---granularity
|---timeInterval
|---cost
|---namespace
|---resourceRegion
|---metrics (list of `MetricResult` objects)
    |---id
    |---type
    |---name
    |---unit
    |---timeSeries (list of `TimeSeriesElement` objects)
        |---metadata (dimensions)
        |---metricValues (list of data points represented by `MetricValue` objects)
             |--- timeStamp
             |--- count
             |--- average
             |--- total
             |--- maximum
             |--- minimum
```

#### Get average and count metrics

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L200-L221 -->
```java
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

Response<MetricsQueryResult> metricsResponse = metricsQueryClient
    .queryWithResponse("{resource-id}", Arrays.asList("SuccessfulCalls", "TotalCalls"),
        new MetricsQueryOptions()
            .setGranularity(Duration.ofHours(1))
            .setAggregations(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
        Context.NONE);

MetricsQueryResult metricsQueryResult = metricsResponse.getValue();

for (MetricResult metric : metricsQueryResult.getMetrics()) {
    System.out.println("Metric name " + metric.getMetricName());
    for (TimeSeriesElement timeSeriesElement : metric.getTimeSeries()) {
        System.out.println("Dimensions " + timeSeriesElement.getMetadata());
        for (MetricValue metricValue : timeSeriesElement.getValues()) {
            System.out.println(metricValue.getTimeStamp() + " " + metricValue.getTotal());
        }
    }
}
```

## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][log_levels].

### Default HTTP client

All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the
client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library

All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL com.azure.monitor.collect.metrics.implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

To learn more about Azure Monitor, see the [Azure Monitor service documentation][azure_monitor_overview].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information, see
the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or
comments.

<!-- LINKS -->

[azure_monitor_create_using_portal]: https://docs.microsoft.com/azure/azure-monitor/logs/quick-create-workspace
[azure_monitor_overview]: https://docs.microsoft.com/azure/azure-monitor/overview
[azure_subscription]: https://azure.microsoft.com/free/java
[changelog]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/CHANGELOG.md
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[kusto_query_language]: https://docs.microsoft.com/azure/data-explorer/kusto/query/
[log_levels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[package]: https://search.maven.org/artifact/com.azure/azure-monitor-query
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/README.md
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query/src
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmonitor%2Fazure-monitor-query%2FREADME.png)
