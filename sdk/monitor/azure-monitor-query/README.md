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
    <version>1.0.0-beta.2</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Create Logs query client

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L39-L41 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Create Logs query async client


<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L43-L45 -->
```java
LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

### Get logs for a query

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L166-L196 -->
```java
LogsQueryResult queryResults = logsQueryClient.queryLogs("{workspace-id}", "{kusto-query}",
    new QueryTimeSpan(Duration.ofDays(2)));
System.out.println("Number of tables = " + queryResults.getLogsTables().size());

// Sample to iterate over all cells in the table
for (LogsTable table : queryResults.getLogsTables()) {
    for (LogsTableCell tableCell : table.getAllTableCells()) {
        System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
    }
}

// Sample to iterate over each row
for (LogsTable table : queryResults.getLogsTables()) {
    for (LogsTableRow tableRow : table.getRows()) {
        for (LogsTableCell tableCell : tableRow.getRow()) {
            System.out.println("Column = " + tableCell.getColumnName()
                + "; value = " + tableCell.getValueAsString());
        }
    }
}

// Sample to get a specific column by name
for (LogsTable table : queryResults.getLogsTables()) {
    for (LogsTableRow tableRow : table.getRows()) {
        Optional<LogsTableCell> tableCell = tableRow.getColumnValue("DurationMs");
        tableCell
            .ifPresent(logsTableCell ->
                System.out.println("Column = " + logsTableCell.getColumnName()
                    + "; value = " + logsTableCell.getValueAsString()));
    }
}
```
### Get logs for a query and read the response as a model type

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L207-L214 -->
```java
LogsQueryResult queryResults = logsQueryClient.queryLogs("{workspace-id}", "{kusto-query}",
        new QueryTimeSpan(Duration.ofDays(2)));

List<CustomModel> results = queryResults.getResultAsObject(CustomModel.class);
results.forEach(model -> {
    System.out.println("Time generated " + model.getTimeGenerated() + "; success = " + model.getSuccess()
            + "; operation name = " + model.getOperationName());
});
```

### Get logs for a batch of queries

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L69-L89 -->
```java
LogsBatchQuery logsBatchQuery = new LogsBatchQuery()
    .addQuery("{workspace-id}", "{query-1}", new QueryTimeSpan(Duration.ofDays(2)))
    .addQuery("{workspace-id}", "{query-2}", new QueryTimeSpan(Duration.ofDays(30)));

LogsBatchQueryResultCollection batchResultCollection = logsQueryClient
    .queryLogsBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

for (LogsBatchQueryResult response : responses) {
    LogsQueryResult queryResult = response.getQueryResult();

    // Sample to iterate by row
    for (LogsTable table : queryResult.getLogsTables()) {
        for (LogsTableRow row : table.getRows()) {
            System.out.println("Row index " + row.getRowIndex());
            row.getRow()
                .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
        }
    }
}
```

### Get logs for a query with server timeout

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L101-L117 -->
```java
// set request options: server timeout, rendering, statistics
LogsQueryOptions options = new LogsQueryOptions()
    .setServerTimeout(Duration.ofMinutes(10));

// make service call with these request options set as filter header
Response<LogsQueryResult> response = logsQueryClient.queryLogsWithResponse("{workspace-id}",
        "{query}", new QueryTimeSpan(Duration.ofDays(2)), options, Context.NONE);
LogsQueryResult logsQueryResult = response.getValue();

// Sample to iterate by row
for (LogsTable table : logsQueryResult.getLogsTables()) {
    for (LogsTableRow row : table.getRows()) {
        System.out.println("Row index " + row.getRowIndex());
        row.getRow()
            .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
    }
}
```

### Create Metrics query client

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L52-L54 -->
```java
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Create Metrics query async client

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L56-L58 -->
```java
MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

### Get metrics

A resource ID, as denoted by the `{resource-id}` placeholder in the sample below, is required to query metrics. To find the resource ID:

1. Navigate to your resource's page in the Azure portal.
2. From the **Overview** blade, select the **JSON View** link.
3. In the resulting JSON, copy the value of the `id` property.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L128-L155 -->
```java
Response<MetricsQueryResult> metricsResponse = metricsQueryClient
    .queryMetricsWithResponse(
        "{resource-id}",
        Arrays.asList("SuccessfulCalls"),
        new MetricsQueryOptions()
            .setMetricsNamespace("Microsoft.CognitiveServices/accounts")
            .setTimeSpan(new QueryTimeSpan(Duration.ofDays(30)))
            .setInterval(Duration.ofHours(1))
            .setTop(100)
            .setAggregation(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
        Context.NONE);

MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
List<Metric> metrics = metricsQueryResult.getMetrics();
metrics.stream()
    .forEach(metric -> {
        System.out.println(metric.getMetricsName());
        System.out.println(metric.getId());
        System.out.println(metric.getType());
        System.out.println(metric.getUnit());
        System.out.println(metric.getTimeSeries().size());
        System.out.println(metric.getTimeSeries().get(0).getData().size());
        metric.getTimeSeries()
            .stream()
            .flatMap(ts -> ts.getData().stream())
            .forEach(mv -> System.out.println(mv.getTimeStamp().toString() + "; Count = " + mv.getCount()
                + "; Average = " + mv.getAverage()));
    });
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
* [Get metrics](#get-metrics "Get metrics")

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
