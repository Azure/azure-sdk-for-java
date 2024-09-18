# Release History

## 1.0.17 (2024-09-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.
- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.


## 1.0.16 (2024-08-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.


## 1.0.15 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.0.14 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.


## 1.0.13 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.


## 1.0.12 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.0.11 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 1.0.10 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.


## 1.0.9 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.0.8 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.


## 1.0.7 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.


## 1.0.6 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.


## 1.0.5 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 1.0.4 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.

## 1.0.3 (2023-06-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 1.0.2 (2023-05-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.
- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.

## 1.0.1 (2023-04-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.

## 1.0.0 (2023-03-07)

- First stable release of Azure Load Testing client library for Java

### Breaking Changes

- Reintroduce two client builders - `LoadTestAdministrationClientBuilder` and `LoadTestRunClientBuilder`
- Rename `metricname` parameter to `metricName` in `LoadTestRunClient.listMetrics`
- Rename `LoadTestRunClient.listMetricDefinitionsWithResponse` to `LoadTestRunClient.getMetricDefinitionsWithResponse`
- Rename `LoadTestRunClient.listMetricNamespacesWithResponse` to `LoadTestRunClient.getMetricNamespacesWithResponse`

### Other Changes

- Updated samples and test cases

## 1.0.0-beta.2 (2023-01-22)

- Second preview release of Azure Load Testing client library for Java.

### Features Added

- Added sync and async versions of `LoadTestRunClient.listMetricNamespacesWithResponse`
- Added sync and async versions of `LoadTestRunClient.listMetricDefinitionsWithResponse`
- Added sync and async versions of `LoadTestRunClient.listMetrics`
- Added sync and async versions of `LoadTestRunClient.listMetricDimensionValues`
- Added sync and async versions of `LoadTestRunClient.createOrUpdateAppComponentWithResponse`
- Added sync and async versions of `LoadTestRunClient.getAppComponentsWithResponse`
- Added sync and async versions of `LoadTestRunClient.createOrUpdateServerMetricsConfigWithResponse`
- Added sync and async versions of `LoadTestRunClient.getServerMetricsConfigWithResponse`
- Added sync and async versions of `LoadTestAdministrationClient.beginUploadTestFile` and `LoadTestRunClient.beginTestRun` Long-Running operation

### Breaking Changes

- There is a single builder which builds `LoadTestAdministration` and `LoadTestRun` clients, rather than a client which provides them via accessor methods previously
- Added metric namespaces and metric dimensions
- File upload now uses `application/octet-stream` instead of `multipart/form-data`
- File upload now uses file name as primary identifier instead of `fileId`
- Removed sync and async versions of `LoadTestAdministrationClient.ListSupportedResourceType`
- Removed sync and async versions of `LoadTestAdministrationClient.DeleteAppComponent` and `LoadTestAdministrationClient.DeleteAppComponentByName`
- Removed sync and async versions of `LoadTestAdministrationClient.DeleteServerMetrics` and `LoadTestAdministrationClient.DeleteServerMetricsByName`
- Removed sync and async versions of `TestRunClient.GetTestRunClientMetrics` and `TestRunClient.GetTestRunClientMetricsFilters`
- Removed sync and async versions of `TestRunClient.GetServerDefaultMetrics`

### Other Changes

- README updated
- Added samples and test cases

## 1.0.0-beta.1 (2022-10-22)

- Initial preview release of Azure Load Testing client library for Java with single top-level `LoadTesting` client, and `LoadTestAdministration` and `TestRun` subclients.
