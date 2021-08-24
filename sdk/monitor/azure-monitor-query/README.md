# Azure Monitor query client library for Java

Azure Monitor helps you maximize the availability and performance of your applications and services. It delivers a
comprehensive solution for collecting, analyzing, and acting on telemetry from your cloud and on-premises environments.

All data collected by Azure Monitor fits into one of two fundamental types, metrics and logs. Metrics are numerical
values that describe some aspect of a system at a particular point in time. They are lightweight and capable of
supporting near real-time scenarios. Logs contain different kinds of data organized into records with different sets of
properties for each type. Telemetry such as events and traces are stored as logs in addition to performance data so that
it can all be combined for analysis.

This client library provides access to query metrics and logs collected by Azure Monitor.

[Source code][source_code] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-monitor-query;current})

```xml

<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-query</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Create Logs query client

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L40-L42 -->
```java



```

### Create Logs query async client

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L44-L46 -->
```java
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();
}
```
### Create Metrics query client

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L53-L55 -->
```java



```

### Create Metrics query async client

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L57-L59 -->
```java
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();
}
```

## Key concepts

### Logs

Azure Monitor Logs is a feature of Azure Monitor that collects and organizes log and performance data from monitored
resources. Data from different sources such as platform logs from Azure services, log and performance data from virtual
machines agents, and usage and performance data from applications can be consolidated into a single workspace so they
can be analyzed together using a sophisticated query language that's capable of quickly analyzing millions of records.
You may perform a simple query that just retrieves a specific set of records or perform sophisticated data analysis to
identify critical patterns in your monitoring data.

#### Log Analytics workspaces

Data collected by Azure Monitor Logs is stored in one or more Log Analytics workspaces. The workspace defines the
geographic location of the data, access rights defining which users can access data, and configuration settings such as
the pricing tier and data retention.

You must create at least one workspace to use Azure Monitor Logs. A single workspace may be sufficient for all of your
monitoring data, or may choose to create multiple workspaces depending on your requirements. For example, you might have
one workspace for your production data and another for testing.

#### Log queries

Data is retrieved from a Log Analytics workspace using a log query which is a read-only request to process data and
return results. Log queries are written
in [Kusto Query Language (KQL)](https://docs.microsoft.com/azure/data-explorer/kusto/query/), which is the same query
language used by Azure Data Explorer. You can write log queries in Log Analytics to interactively analyze their results,
use them in alert rules to be proactively notified of issues, or include their results in workbooks or dashboards.
Insights include prebuilt queries to support their views and workbooks.

### Metrics

Azure Monitor Metrics is a feature of Azure Monitor that collects numeric data from monitored resources into a time
series database. Metrics are numerical values that are collected at regular intervals and describe some aspect of a
system at a particular time. Metrics in Azure Monitor are lightweight and capable of supporting near real-time scenarios
making them particularly useful for alerting and fast detection of issues. You can analyze them interactively with
metrics explorer, be proactively notified with an alert when a value crosses a threshold, or visualize them in a
workbook or dashboard.

#### Metrics data structure

Data collected by Azure Monitor Metrics is stored in a time-series database which is optimized for analyzing
time-stamped data. Each set of metric values is a time series with the following properties:

- The time the value was collected
- The resource the value is associated with
- A namespace that acts like a category for the metric
- A metric name
- The value itself
- Some metrics may have multiple dimensions as described in Multi-dimensional metrics. Custom metrics can have up to 10
  dimensions.

## Examples

* [Get logs for a query](#get-query "Get logs for a query")
* [Get logs for a batch for queries](#get-batch-query "Get logs for a batch of queries")
* [Get logs for a query with server timeout](#get-query-server-timeout "Get logs for a query with server timeout")
* [Get logs from multiple workspaces](#get-query-multiple-workspaces "Get logs from multiple workspaces")
* [Get metrics](#get-metrics "Get metrics")
* [Get average and count metrics ](#get-aggregation-metrics "Get average and count metrics")


### Get logs for a query

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

### Get logs for a query and read the response as a model type

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
### Get logs for a batch of queries

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L113-L132 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
String query1 = logsBatchQuery.addQuery("{workspace-id}", "{query-1}", new TimeInterval(Duration.ofDays(2)));
String query2 = logsBatchQuery.addQuery("{workspace-id}", "{query-2}", new TimeInterval(Duration.ofDays(30)));

LogsBatchQueryResults batchResultCollection = logsQueryClient
        .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

LogsBatchQueryResult result = batchResultCollection.getResult(query1);
for (LogsTableRow row : result.getTable().getRows()) {
    System.out.println(row.getColumnValue("OperationName") + " " + row.getColumnValue("ResourceGroup"));
}

List<CustomLogModel> customLogModels = batchResultCollection.getResult(query2, CustomLogModel.class);
for (CustomLogModel customLogModel : customLogModels) {
    System.out.println(customLogModel.getOperationName() + " " + customLogModel.getResourceGroup());
}
```

### Get logs for a query with server timeout

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L140-L149 -->
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

### Get logs from multiple workspaces

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L156-L164 -->
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

#### Handling the response for Logs Query

The `query` API returns the `LogsQueryResult` while the `queryBatch` API returns the `LogsBatchQueryResult`.

Here is a hierarchy of the response:

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

### Get metrics

A resource ID, as denoted by the `{resource-id}` placeholder in the sample below, is required to query metrics. To find the resource ID:

1. Navigate to your resource's page in the Azure portal.
2. From the **Overview** blade, select the **JSON View** link.
3. In the resulting JSON, copy the value of the `id` property.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L172-L187 -->
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

### Get average and count metrics

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L194-L215 -->
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
### Handle metrics response

The metrics query API returns a `MetricsResult` object. The `MetricsResult` object contains properties such as a list of `Metric`-typed objects, `interval`, `namespace`, and `timespan`. The `Metric` objects list can be accessed using the `metrics` param. Each `Metric` object in this list contains a list of `TimeSeriesElement` objects. Each `TimeSeriesElement` contains `data` and `metadata_values` properties. In visual form, the object hierarchy of the response resembles the following structure:

```
MetricsQueryResult
|---interval
|---timespan
|---cost
|---namespace
|---resourceregion
|---metrics (list of `Metric` objects)
    |---id
    |---type
    |---name
    |---unit
    |---timeseries (list of `TimeSeriesElement` objects)
        |---metadata_values (dimenstions)
        |---data (list of data points represented by `MetricValue` objects)
             |--- timeStamp
             |--- count
             |--- average
             |--- total
             |--- maximum
             |--- minimum
```

## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][log_levels].

### Default HTTP Client

All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the
client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library

All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL com.azure.monitor.collect.metrics.implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

- Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to
a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see
the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or
comments.

<!-- LINKS -->

[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query/src

[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/README.md

[azure_subscription]: https://azure.microsoft.com/free/java

[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

[product_documentation]: https://docs.microsoft.com/azure/azure-monitor/overview

[log_levels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java

[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

[cla]: https://cla.microsoft.com

[coc]: https://opensource.microsoft.com/codeofconduct/

[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/

[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmonitor%2Fazure-monitor-query%2FREADME.png)
