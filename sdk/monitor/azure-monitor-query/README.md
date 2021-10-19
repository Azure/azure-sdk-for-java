# Azure Monitor Query client library for Java

The Azure Monitor Query client library is used to execute read-only queries against [Azure Monitor][azure_monitor_overview]'s two data platforms:

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
    <version>1.0.0</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Create the client

An authenticated client is required to query Logs or Metrics. The library includes both synchronous and asynchronous forms of the clients. To authenticate, the following examples use `DefaultAzureCredentialBuilder` from the [com.azure:azure-identity](https://search.maven.org/artifact/com.azure/azure-identity) package.

#### Synchronous clients

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L44-L46 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L57-L59 -->
```java
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Asynchronous clients

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L48-L50 -->
```java
LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L61-L63 -->
```java
MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

### Execute the query

For examples of Logs and Metrics queries, see the [Examples](#examples) section.

## Key concepts

### Logs query rate limits and throttling

The Log Analytics service applies throttling when the request rate is too high. Limits, such as the maximum number of rows returned, are also applied on the Kusto queries. For more information, see [Rate and query limits](https://dev.loganalytics.io/documentation/Using-the-API/Limits).

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

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L70-L79 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

LogsQueryResult queryResults = logsQueryClient.queryWorkspace("{workspace-id}", "{kusto-query}",
        new QueryTimeInterval(Duration.ofDays(2)));

for (LogsTableRow row : queryResults.getTable().getRows()) {
    System.out.println(row.getColumnValue("OperationName") + " " + row.getColumnValue("ResourceGroup"));
}
```

#### Map logs query results to a model

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L85-L96 -->
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

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L102-L111 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

List<CustomLogModel> customLogModels = logsQueryClient.queryWorkspace("{workspace-id}", "{kusto-query}",
        new QueryTimeInterval(Duration.ofDays(2)), CustomLogModel.class);

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

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L118-L143 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
String query1 = logsBatchQuery.addWorkspaceQuery("{workspace-id}", "{query-1}", new QueryTimeInterval(Duration.ofDays(2)));
String query2 = logsBatchQuery.addWorkspaceQuery("{workspace-id}", "{query-2}", new QueryTimeInterval(Duration.ofDays(30)));
String query3 = logsBatchQuery.addWorkspaceQuery("{workspace-id}", "{query-3}", new QueryTimeInterval(Duration.ofDays(10)));

LogsBatchQueryResultCollection batchResults = logsQueryClient
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
if (query3Result.getQueryResultStatus() == LogsQueryResultStatus.FAILURE) {
    System.out.println(query3Result.getError().getMessage());
}
```

### Advanced logs query scenarios

#### Set logs query timeout

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L151-L160 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

// set request options: server timeout
LogsQueryOptions options = new LogsQueryOptions()
    .setServerTimeout(Duration.ofMinutes(10));

Response<LogsQueryResult> response = logsQueryClient.queryWorkspaceWithResponse("{workspace-id}",
        "{kusto-query}", new QueryTimeInterval(Duration.ofDays(2)), options, Context.NONE);
```

#### Query multiple workspaces

To run the same query against multiple Log Analytics workspaces, use the `LogsQueryOptions.setAdditionalWorkspaces` method:

When multiple workspaces are included in the query, the logs in the result table are not grouped according to the 
workspace from which it was retrieved. To identify the workspace of a row in the result table, you can inspect the 
"TenantId" column in the result table. If this column is not in the table, then you may have to update your query string
to include this column.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L167-L175 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

Response<LogsQueryResult> response = logsQueryClient.queryWorkspaceWithResponse("{workspace-id}", "{kusto-query}",
        new QueryTimeInterval(Duration.ofDays(2)), new LogsQueryOptions()
                .setAdditionalWorkspaces(Arrays.asList("{additional-workspace-identifiers}")),
        Context.NONE);
LogsQueryResult result = response.getValue();
```

### Metrics query

A resource ID, as denoted by the `{resource-id}` placeholder in the sample below, is required to query metrics. To find the resource ID:

1. Navigate to your resource's page in the Azure portal.
2. From the **Overview** blade, select the **JSON View** link.
3. In the resulting JSON, copy the value of the `id` property.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L183-L198 -->
```java
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

MetricsQueryResult metricsQueryResult = metricsQueryClient.queryResource("{resource-uri}",
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

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L205-L226 -->
```java
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

Response<MetricsQueryResult> metricsResponse = metricsQueryClient
    .queryResourceWithResponse("{resource-id}", Arrays.asList("SuccessfulCalls", "TotalCalls"),
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

### Enable HTTP request/response logging
Reviewing the HTTP request sent or response received over the wire to/from the Azure Monitor service can be useful in troubleshooting issues. To enable logging the HTTP request and response payload, the LogsQueryClient and the MetricsQueryClient can be configured as shown below:

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L235-L243 -->
```java
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(credential)
        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        .buildClient();
// or
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
        .credential(credential)
        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        .buildClient();
```

Alternatively, you can configure logging HTTP requests and responses for your entire application by setting the following environment variable. Note that this change will enable logging for every Azure client that supports logging HTTP request/response.

Environment variable name: `AZURE_HTTP_LOG_DETAIL_LEVEL`
| Value            | Logging level                                                        |
|------------------|----------------------------------------------------------------------|
| none             | HTTP request/response logging is disabled                            |
| basic            | Logs only URLs, HTTP methods, and time to finish the request.        |
| headers          | Logs everything in BASIC, plus all the request and response headers. |
| body             | Logs everything in BASIC, plus all the request and response body.    |
| body_and_headers | Logs everything in HEADERS and BODY.                                 |


### Unable to authenticate requests

Azure Monitor Query supports Azure Active Directory authentication. Both LogsQueryClientBuilder and MetricsQueryClientBuilder have methods to set the `credential`. To provide a valid credential, you can use `azure-identity` dependency. For more details on getting started, refer to the [README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query#create-the-client) of Azure Monitor Query library. You can also refer to the [Azure Identity documentation](https://docs.microsoft.com/azure/developer/java/sdk/identity) for more details on the various types of credential supported in `azure-identity`.

### No results in logs query

If your Kusto query returns empty no logs, please validate the following:
- You have the right workspace ID
- You are setting the correct time interval for the query. Try expanding the time interval for your query to see if that returns any results.
- If your Kusto query also has a time interval, the query is evaluated for the intersection of the time interval in the query string and the time interval set in the `QueryTimeInterval` param provided the query API. The intersection of these time intervals may not have any logs. To avoid any confusion, it's recommended to remove any time interval in the Kusto query string and use `QueryTimeInterval` explicitly.

### Client times out when executing a logs query request

Some Kusto queries can run for a long time on the server depending on the complexity of the query and the number of results that the query has to fetch. This can lead to the client timing out before the server has had chance to respond. To increase the client side timeout, you can configure the HTTP client to have an extended timeout by doing the following.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L252-L255 -->
```java
LogsQueryClient client = new LogsQueryClientBuilder()
        .credential(credential)
        .clientOptions(new HttpClientOptions().setResponseTimeout(Duration.ofSeconds(120)))
        .buildClient();
```
The above code will create a LogsQueryClient with a Netty HTTP client that waits for a response for up to 120 seconds. The default is 60 seconds.

### Server times out when executing a logs query request

Similar to the above section, complex Kusto queries can take a long time to complete and such queries are aborted by the service if they run for more than 3 minutes. For such scenarios, the query APIs on `LogsQueryClient`, provide options to configure the timeout on the server. The server timeout can be extended up to 10 minutes.

The following code shows a sample on how to set the server timeout to 10 minutes. Note that by setting this server timeout, the Azure Monitor Query library will automatically also extend the client timeout to wait for 10 minutes for the server to respond. You don't need to configure your HTTP client to extend the response timeout as shown in the previous section.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L263-L268 -->
```java
LogsQueryClient client = new LogsQueryClientBuilder()
        .credential(credential)
        .buildClient();

client.queryWorkspaceWithResponse("{workspaceId}", "{kusto-query-string}", QueryTimeInterval.LAST_DAY,
        new LogsQueryOptions().setServerTimeout(Duration.ofMinutes(10)), Context.NONE);
```

### Server timeout not working for OkHTTP client

Due to the limitations in OkHTTP client, extending the timeout of a specific logs query request is not supported. So, to workaround this, the client has to be configured with longer timeout value at the time of building the client as shown below. The downside to doing this is that every request from this client will have this extended client-side timeout.

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L252-L255 -->
```java
LogsQueryClient client = new LogsQueryClientBuilder()
        .credential(credential)
        .clientOptions(new HttpClientOptions().setResponseTimeout(Duration.ofSeconds(120)))
        .buildClient();
```


### Logs query throwing exception when the query execution is partially successful

By default, if the execution of a Kusto query resulted in a partially successful response, the Azure Monitor Query client library will throw an exception to indicate to the user that the query was not fully successful. To turn this behavior off and consume the partially successful response, you can set the `allowPartialErrors` property to `true` in `LogsQueryOptions` as shown below:

<!-- embedme ./src/samples/java/com/azure/monitor/query/ReadmeSamples.java#L280-L281 -->
```java
client.queryWorkspaceWithResponse("{workspaceId}", "{kusto-query-string}", QueryTimeInterval.LAST_DAY,
        new LogsQueryOptions().setAllowPartialErrors(true), Context.NONE);
```

### Metrics query granularity not supported

If you notice the following exception, this is due to an invalid time granularity in the metrics query request. Your query might look something like the following where `MetricsQueryOptions().setGranularity()` is set to an unsupported duration.

```
com.azure.core.exception.HttpResponseException: Status code 400, "{"code":"BadRequest","message":"Invalid time grain duration: PT10M, supported ones are: 00:01:00,00:05:00,00:15:00,00:30:00,01:00:00,06:00:00,12:00:00,1.00:00:00"}"

	at com.azure.monitor.query@1.0.0-beta.5/com.azure.monitor.query.MetricsQueryAsyncClient.lambda$queryResourceWithResponse$4(MetricsQueryAsyncClient.java:205)
	at reactor.core@3.4.10/reactor.core.publisher.Mono.lambda$onErrorMap$30(Mono.java:3680)
	at reactor.core@3.4.10/reactor.core.publisher.Mono.lambda$onErrorResume$32(Mono.java:3770)
	at reactor.core@3.4.10/reactor.core.publisher.FluxOnErrorResume$ResumeSubscriber.onError(FluxOnErrorResume.java:94)
	at reactor.core@3.4.10/reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onError(FluxMapFuseable.java:140)
	at reactor.core@3.4.10/reactor.core.publisher.MonoFlatMap$FlatMapMain.onError(MonoFlatMap.java:172)

```

As documented in the error message, the supported granularity for metrics queries are 1 minute, 5 minutes, 15 minutes, 30 minutes, 1 hour, 6 hours, 12 hours and 1 day.


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
