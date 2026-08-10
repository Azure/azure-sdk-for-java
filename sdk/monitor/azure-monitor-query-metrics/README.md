# Azure Monitor Metrics Query client library for Java

The Azure Monitor Metrics Query client library is used to execute read-only queries against [Azure Monitor][azure_monitor_overview]'s metrics data platform:

- [Metrics](https://learn.microsoft.com/azure/azure-monitor/essentials/data-platform-metrics) - Collects numeric data from monitored resources into a time series database. Metrics are numerical values that are collected at regular intervals and describe some aspect of a system at a particular time. Metrics are lightweight and capable of supporting near real-time scenarios, making them useful for alerting and fast detection of issues.

**Resources:**

- [Source code][source]
- [Package (Maven)][package]
- [API reference documentation][msdocs_apiref]
- [Service documentation][azure_monitor_overview]
- [Samples][samples]
- [Change log][changelog]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later
    - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- An [Azure subscription][azure_subscription]
- A [TokenCredential](https://learn.microsoft.com/java/api/com.azure.core.credential.tokencredential?view=azure-java-stable) implementation, such as an [Azure Identity library credential type](https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#credential-classes).
- To query Metrics, you need an Azure resource of any kind (Storage Account, Key Vault, Cosmos DB, etc.).

### Include the package

#### Include the BOM file

Include the `azure-sdk-bom` to your project to take a dependency on the stable version of the library. In the following snippet, replace the `{bom_version_to_target}` placeholder with the version number. To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

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

Then include the direct dependency in the `dependencies` section without the version tag:

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-query-metrics</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency

If you want to take dependency on a particular version of the library that isn't present in the BOM, add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-monitor-query-metrics;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-query-metrics</artifactId>
    <version>1.0.0</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authenticate using Microsoft Entra ID

You can authenticate with Microsoft Entra ID using the [Azure Identity library][azure_identity]. Regional endpoints don't support Microsoft Entra authentication. Create a [custom subdomain][custom_subdomain] for your resource to use this type of authentication.

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure Identity library, include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.18.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

Set the values of the client ID, tenant ID, and client secret of the Microsoft Entra application as environment variables: `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_CLIENT_SECRET`.

#### Synchronous clients

```java readme-sample-createMetricsClient
MetricsClient metricsClient = new MetricsClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Asynchronous clients

```java readme-sample-createMetricsAsyncClient
MetricsAsyncClient metricsAsyncClient = new MetricsClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

#### Configure client for Azure sovereign cloud

By default, `MetricsClient` is configured to connect to the Azure Public Cloud. To use a sovereign cloud instead, set the correct endpoint in the client builders.

- Creating a `MetricsClient` for Azure China Cloud:

    ```java readme-sample-createSovereignMetricsClient
    MetricsClient metricsClient = new MetricsClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .endpoint("{china_cloud_endpoint}")
        .audience(MetricsAudience.AZURE_CHINA)
        .buildClient();
    ```

### Execute the query

For examples of Metrics queries, see the [Examples](#examples) section.

## Key concepts

### Metrics data structure

Each set of metric values is a time series with the following characteristics:

- The time the value was collected
- The resource associated with the value
- A namespace that acts like a category for the metric
- A metric name
- The value itself
- Some metrics have multiple dimensions as described in multi-dimensional metrics. Custom metrics can have up to 10 dimensions.

## Examples

### Metrics query

A resource ID, as denoted by the `{resource-uri}` placeholder in the following sample, is required to query metrics. To find the resource ID:

1. Navigate to your resource's page in the Azure portal.
2. From the **Overview** blade, select the **JSON View** link.
3. In the resulting JSON, copy the value of the `id` property.

### Metrics query resources

```java readme-sample-metricsqueryresource
MetricsClient metricsClient = new MetricsClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

MetricsQueryResourcesResult metricsQueryResult = metricsClient.queryResources(
    Arrays.asList("{resourceId}", "{resourceId2}"),
    Arrays.asList("{metric1}", "{metric2}"),
    "{metricNamespace}");

for (MetricsQueryResult queryResult : metricsQueryResult.getMetricsQueryResults()) {
    System.out.println("Resource ID: " + queryResult.getResourceId());
    System.out.println("Metrics: " + queryResult.getMetrics().size() + "\n");
    // print out the metrics for each query result
    for (MetricResult metric : queryResult.getMetrics()) {
        System.out.println("Metric Name: " + metric.getMetricName());
        System.out.println("Unit: " + metric.getUnit());
        System.out.println("Time Series Count: " + metric.getTimeSeries().size());

        metric.getTimeSeries().forEach(timeSeries -> {
            timeSeries.getValues().forEach(value -> {
                System.out.println("Timestamp: " + value.getTimeStamp()
                    + ", Total: " + value.getTotal()
                    + ", Average: " + value.getAverage()
                    + ", Count: " + value.getCount());
            });
        });
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

```java readme-sample-metricsqueryaggregation
MetricsClient metricsClient = new MetricsClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

Response<MetricsQueryResourcesResult> metricsResponse = metricsClient.queryResourcesWithResponse(
    Arrays.asList("{resourceId}", "{resourceId2}"),
    Arrays.asList("{metric1}", "{metric2}"),
    "{metricNamespace}",
    new MetricsQueryResourcesOptions()
        .setGranularity(Duration.ofHours(1))
        .setAggregations(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
    Context.NONE);

MetricsQueryResourcesResult metricsQueryResult = metricsResponse.getValue();

for (MetricsQueryResult queryResult : metricsQueryResult.getMetricsQueryResults()) {
    System.out.println("Resource ID: " + queryResult.getResourceId());
    System.out.println("Metrics: " + queryResult.getMetrics().size() + "\n");
    // print out the metrics for each query result
    for (MetricResult metric : queryResult.getMetrics()) {
        System.out.println("Metric Name: " + metric.getMetricName());
        System.out.println("Unit: " + metric.getUnit());
        System.out.println("Time Series Count: " + metric.getTimeSeries().size());

        metric.getTimeSeries().forEach(timeSeries -> {
            timeSeries.getValues().forEach(value -> {
                System.out.println("Timestamp: " + value.getTimeStamp()
                    + ", Average: " + value.getAverage()
                    + ", Count: " + value.getCount());
            });
        });
    }
}
```

## Troubleshooting

See our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-metrics/TROUBLESHOOTING.md)
for details on how to diagnose various failure scenarios.

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
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md
[azure_monitor_overview]: https://learn.microsoft.com/azure/azure-monitor/overview
[azure_subscription]: https://azure.microsoft.com/free/java
[changelog]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-metrics/CHANGELOG.md
[custom_subdomain]: https://learn.microsoft.com/azure/cognitive-services/authentication?tabs=powershell#create-a-resource-with-a-custom-subdomain
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[msdocs_apiref]: https://learn.microsoft.com/java/api/com.azure.monitor.query?view=azure-java-stable
[package]: https://central.sonatype.com/artifact/com.azure/azure-monitor-query-metrics
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-metrics/src/samples/java/README.md
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query-metrics/src

[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

