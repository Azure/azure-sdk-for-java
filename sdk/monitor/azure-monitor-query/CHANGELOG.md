# Release History

## 1.0.0-beta.3 (2021-08-11)

### Breaking changes
- `queryLogs` APIs on `LogsQueryClient` and `LogsQueryAsyncClient` renamed to `query`.
- `queryLogsBatch` APIs on `LogsQueryClient` and `LogsQueryAsyncClient` renamed to `queryBatch`.
- `queryMetrics` APIs on `MetricQueryClient` and `MetricsQueryAsyncClient` renamed to `query`.
- `listMetricsNamespace` APIs on `MetricQueryClient` and `MetricsQueryAsyncClient` renamed to `listMetricNamespaces`.
- `listMetricsDefinition` APIs on `MetricQueryClient` and `MetricsQueryAsyncClient` renamed to `listMetricDefinitions`.

### Dependency Updates
- Upgraded `azure-core` from `1.18.0` to `1.19.0`.
- Upgraded `azure-core-http-netty` from `1.10.1` to `1.10.2`.

## 1.0.0-beta.2 (2021-07-08)

### Dependency Updates
- Upgraded `azure-core` from `1.17.0` to `1.18.0`.
- Upgraded `azure-core-http-netty` from `1.10.0` to `1.10.1`.

## 1.0.0-beta.1 (2021-06-09)
Version 1.0.0-beta.1 is a preview of our efforts in creating a client library for querying Azure Monitor logs and 
metrics that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages 
and platforms as possible. The principles that guide our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### Features Added
- Initial release. Please see the README and wiki for information on using the new library.
