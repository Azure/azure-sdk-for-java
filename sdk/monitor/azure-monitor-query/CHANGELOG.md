# Release History

## 1.0.8 (2022-07-07)

### Other Changes

#### Dependency updates
- Upgraded `azure-core` to version `1.30.0`.
- Upgraded `azure-core-http-netty` to version `1.12.3`.

## 1.0.7 (2022-06-09)

### Bugs Fixed

- Fixed bug where partial queries fail when `LogsQueryOptions.setAllowPartialErrors(true)`.
- [Fixed bug that causes `NullPointerException` when batch queries have server timeout configured.](https://github.com/Azure/azure-sdk-for-java/issues/29339) 

### Other Changes

#### Dependency updates
- Upgraded `azure-core` to version `1.29.1`.
- Upgraded `azure-core-http-netty` to version `1.12.2`.

## 1.0.6 (2022-05-12)

### Other Changes

#### Dependency updates
- Upgraded `azure-core` to version `1.28.0`.
- Upgraded `azure-core-http-netty` to version `1.12.0`.

## 1.0.5 (2022-04-08)

### Other Changes

#### Dependency updates
- Upgraded `azure-core` to version `1.27.0`.
- Upgraded `azure-core-http-netty` to version `1.11.9`.

## 1.0.4 (2022-03-11)

### Bugs Fixed
- [Fixed metrics aggregation enum value](https://github.com/Azure/azure-sdk-for-java/issues/27454)

### Other Changes

#### Dependency updates 
- Upgraded `azure-core` to version `1.26.0`.
- Upgraded `azure-core-http-netty` to version `1.11.8`.

## 1.0.3 (2022-02-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.

## 1.0.2 (2022-01-14)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` to `1.24.1`
- Upgraded `azure-core-http-netty` to `1.11.6`

## 1.0.1 (2021-11-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` to `1.22.0`
- Upgraded `azure-core-http-netty` to `1.11.2`

## 1.0.0 (2021-10-07)

### Features Added
- Added `getMetricByName` API on `MetricsQueryResult` to get the metric result for a specific metric name.
- Added `LogsQueryStatus` enum to specify if the query was successful, partially successful or failed.

### Breaking Changes
- Changed `query` API name in `LogsQuery*Client` to `queryWorkspace`
- Changed `query` API name in `MetricsQuery*Client` to `queryResource`
- Changed `addQuery` API name in `LogsQueryBatch` to `addWorkspaceQuery`
- Removed `status` from `LogsBatchQueryResult`
- Throws exception if a logs query is partially successful with an option in `LogsQueryOptions` to disable this 
  behavior.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` to `1.21.0`.
- Upgraded `azure-core-http-netty` to `1.11.1`.

## 1.0.0-beta.4 (2021-09-10)

### Features Added
- Added an API in `MetricsQueryResult` to get metric result of a specific metric name when there are multiple metric 
  results in the response.

### Breaking changes
- `LogsBatchQueryResults` renamed to `LogsBatchQueryResultCollection`.
- Removed `LocalizableString` and flatten `getName().getValue()` call to `getName()`.

### Other Changes 

#### Dependency Updates
- Upgraded `azure-core` from `1.19.0` to `1.20.0`.
- Upgraded `azure-core-http-netty` from `1.10.2` to `1.11.0`.


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
