# Release History

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
