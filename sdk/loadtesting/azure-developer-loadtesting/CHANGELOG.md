# Release History

## 1.1.0-beta.2 (Unreleased)

### Breaking Changes

#### `implementation.LoadTestRunsImpl` was removed

#### `implementation.LoadTestingClientImpl` was removed

#### `implementation.LoadTestRunsImpl$LoadTestRunsService` was removed

#### `implementation.LoadTestAdministrationsImpl` was removed

#### `implementation.LoadTestAdministrationsImpl$LoadTestAdministrationsService` was removed

### Features Added

* `implementation.JsonMergePatchHelper$ResourceMetricAccessor` was added

* `models.FunctionFlexConsumptionTargetResourceConfigurations` was added

* `models.MetricDefinition` was added

* `implementation.JsonMergePatchHelper$AutoStopCriteriaAccessor` was added

* `models.TestRunServerMetricsConfiguration` was added

* `models.PfMetrics` was added

* `models.PassFailMetric` was added

* `models.TestRunStatistics` was added

* `models.DimensionValueList` was added

* `models.PFAction` was added

* `implementation.JsonMergePatchHelper` was added

* `models.FunctionFlexConsumptionResourceConfiguration` was added

* `models.RequestDataLevel` was added

* `models.TestRunFileInfo` was added

* `models.PFTestResult` was added

* `implementation.JsonMergePatchHelper$FunctionFlexConsumptionResourceConfigurationAccessor` was added

* `models.FileValidationStatus` was added

* `models.PFResult` was added

* `implementation.JsonMergePatchHelper$PassFailServerMetricAccessor` was added

* `models.TestKind` was added

* `models.RecommendationCategory` was added

* `models.DimensionValue` was added

* `models.TestCertificate` was added

* `implementation.JsonMergePatchHelper$LoadTestingAppComponentAccessor` was added

* `models.AutoStopCriteria` was added

* `models.TestFileInfo` was added

* `models.PassFailAggregationFunction` was added

* `models.FileStatus` was added

* `models.ArtifactsContainerInfo` was added

* `models.TestRunArtifacts` was added

* `models.TimeGrain` was added

* `models.MetricAvailability` was added

* `models.NameAndDescription` was added

* `implementation.JsonMergePatchHelper$TestRunServerMetricsConfigurationAccessor` was added

* `models.Aggregation` was added

* `implementation.JsonMergePatchHelper$TestAppComponentsAccessor` was added

* `implementation.LoadTestRunClientImpl$LoadTestRunClientService` was added

* `models.TestProfileRun` was added

* `implementation.JsonMergePatchHelper$RegionalConfigurationAccessor` was added

* `implementation.JsonMergePatchHelper$TestProfileAccessor` was added

* `models.CreateByTypes` was added

* `models.TestInputArtifacts` was added

* `models.CertificateType` was added

* `models.FileType` was added

* `implementation.JsonMergePatchHelper$LoadTestConfigurationAccessor` was added

* `models.TestRunStatus` was added

* `implementation.JsonMergePatchHelper$TestServerMetricsConfigurationAccessor` was added

* `models.PassFailTestResult` was added

* `models.LoadTestingFileType` was added

* `models.LoadTestKind` was added

* `models.PassFailResult` was added

* `models.TestRunDetail` was added

* `models.MetricsFilters` was added

* `implementation.JsonMergePatchHelper$LoadTestAccessor` was added

* `models.MetricNamespaces` was added

* `models.NameAndDesc` was added

* `models.TestProfile` was added

* `models.CreatedByType` was added

* `implementation.JsonMergePatchHelper$TestCertificateAccessor` was added

* `models.PassFailAction` was added

* `models.Status` was added

* `models.ResourceKind` was added

* `implementation.JsonMergePatchHelper$OptionalLoadTestConfigurationAccessor` was added

* `implementation.JsonMergePatchHelper$TargetResourceConfigurationsAccessor` was added

* `models.LoadTestConfiguration` was added

* `implementation.LoadTestRunClientImpl` was added

* `models.SecretType` was added

* `models.TestRunOutputArtifacts` was added

* `models.TestProfileRunStatus` was added

* `models.TestServerMetricsConfiguration` was added

* `implementation.JsonMergePatchHelper$TestSecretAccessor` was added

* `models.TestRunAppComponents` was added

* `models.ResourceMetric` was added

* `models.MetricNamespace` was added

* `implementation.JsonMergePatchHelper$LoadTestRunAccessor` was added

* `models.RegionalConfiguration` was added

* `models.LoadTestingManagedIdentityType` was added

* `models.MetricRequestPayload` was added

* `implementation.JsonMergePatchHelper$PassFailMetricAccessor` was added

* `models.LoadTestingAppComponent` was added

* `models.TestProfileRunRecommendation` was added

* `implementation.LoadTestAdministrationClientImpl` was added

* `implementation.JsonMergePatchHelper$TestRunAppComponentsAccessor` was added

* `implementation.JsonMergePatchHelper$PassFailCriteriaAccessor` was added

* `models.TestRunInputArtifacts` was added

* `models.ErrorDetails` was added

* `models.MetricUnit` was added

* `models.AggregationType` was added

* `models.MetricValue` was added

* `models.MetricDefinitions` was added

* `models.TargetResourceConfigurations` was added

* `models.TimeSeriesElement` was added

* `models.PFAgFunc` was added

* `implementation.JsonMergePatchHelper$TestProfileRunAccessor` was added

* `models.LoadTestRun` was added

* `models.TestSecret` was added

* `implementation.LoadTestAdministrationClientImpl$LoadTestAdministrationClientService` was added

* `models.OptionalLoadTestConfiguration` was added

* `models.PassFailServerMetric` was added

* `models.DimensionFilter` was added

* `models.TestAppComponents` was added

* `models.LoadTest` was added

* `models.PassFailCriteria` was added

#### `LoadTestAdministrationAsyncClient` was modified

* `getTestProfileWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `deleteTestFile(java.lang.String,java.lang.String)` was added
* `getTestFile(java.lang.String,java.lang.String)` was added
* `createOrUpdateTestProfileWithResponse(java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `deleteTestProfile(java.lang.String)` was added
* `listTests(java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime)` was added
* `listTestProfiles(java.time.OffsetDateTime,java.time.OffsetDateTime,java.util.List,java.util.List)` was added
* `listTestFiles(java.lang.String)` was added
* `beginUploadTestFile(java.lang.String,java.lang.String,com.azure.core.util.BinaryData)` was added
* `createOrUpdateTestProfile(java.lang.String,models.TestProfile)` was added
* `getServerMetricsConfig(java.lang.String)` was added
* `getAppComponents(java.lang.String)` was added
* `getTestProfile(java.lang.String)` was added
* `getTest(java.lang.String)` was added
* `deleteTest(java.lang.String)` was added
* `listTests()` was added
* `listTestProfiles(com.azure.core.http.rest.RequestOptions)` was added
* `createOrUpdateTest(java.lang.String,models.LoadTest)` was added
* `deleteTestProfileWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `listTestProfiles()` was added
* `createOrUpdateServerMetricsConfig(java.lang.String,models.TestServerMetricsConfiguration)` was added
* `createOrUpdateAppComponents(java.lang.String,models.TestAppComponents)` was added

#### `LoadTestRunAsyncClient` was modified

* `getTestProfileRunWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `listTestRuns()` was added
* `stopTestProfileRun(java.lang.String)` was added
* `getServerMetricsConfig(java.lang.String)` was added
* `getAppComponents(java.lang.String)` was added
* `getTestRunFile(java.lang.String,java.lang.String)` was added
* `listMetricDimensionValues(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `beginTestProfileRun(java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `stopTestProfileRunWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `deleteTestProfileRun(java.lang.String)` was added
* `createOrUpdateAppComponents(java.lang.String,models.TestRunAppComponents)` was added
* `beginTestProfileRun(java.lang.String,models.TestProfileRun)` was added
* `getTestProfileRun(java.lang.String)` was added
* `listTestProfileRuns(com.azure.core.http.rest.RequestOptions)` was added
* `stopTestRun(java.lang.String)` was added
* `listTestRuns(java.lang.String,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.util.List)` was added
* `deleteTestRun(java.lang.String)` was added
* `listTestProfileRuns(java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.util.List,java.util.List,java.util.List)` was added
* `listMetricDimensionValues(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.TimeGrain)` was added
* `getTestRun(java.lang.String)` was added
* `createOrUpdateServerMetricsConfig(java.lang.String,models.TestRunServerMetricsConfiguration)` was added
* `beginTestRun(java.lang.String,models.LoadTestRun)` was added
* `getMetricDefinitions(java.lang.String,java.lang.String)` was added
* `listTestProfileRuns()` was added
* `getMetricNamespaces(java.lang.String)` was added
* `deleteTestProfileRunWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added

#### `LoadTestRunClient` was modified

* `getServerMetricsConfig(java.lang.String)` was added
* `getTestRunFile(java.lang.String,java.lang.String)` was added
* `stopTestRun(java.lang.String)` was added
* `deleteTestProfileRunWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `getAppComponents(java.lang.String)` was added
* `listTestProfileRuns(java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.time.OffsetDateTime,java.util.List,java.util.List,java.util.List)` was added
* `beginTestProfileRun(java.lang.String,models.TestProfileRun)` was added
* `createOrUpdateServerMetricsConfig(java.lang.String,models.TestRunServerMetricsConfiguration)` was added
* `getMetricDefinitions(java.lang.String,java.lang.String)` was added
* `listMetricDimensionValues(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.TimeGrain)` was added
* `stopTestProfileRunWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `getTestProfileRun(java.lang.String)` was added
* `beginTestProfileRun(java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `createOrUpdateAppComponents(java.lang.String,models.TestRunAppComponents)` was added
* `getTestProfileRunWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `listTestProfileRuns(com.azure.core.http.rest.RequestOptions)` was added
* `listMetricDimensionValues(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listTestRuns()` was added
* `stopTestProfileRun(java.lang.String)` was added
* `deleteTestRun(java.lang.String)` was added
* `getMetricNamespaces(java.lang.String)` was added
* `listTestRuns(java.lang.String,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.util.List)` was added
* `getTestRun(java.lang.String)` was added
* `beginTestRun(java.lang.String,models.LoadTestRun,java.lang.String)` was added
* `deleteTestProfileRun(java.lang.String)` was added
* `listTestProfileRuns()` was added

#### `LoadTestAdministrationClient` was modified

* `getTestProfile(java.lang.String)` was added
* `createOrUpdateTestProfileWithResponse(java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `getTest(java.lang.String)` was added
* `getTestProfileWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `getServerMetricsConfig(java.lang.String)` was added
* `createOrUpdateAppComponents(java.lang.String,models.TestAppComponents)` was added
* `deleteTestFile(java.lang.String,java.lang.String)` was added
* `createOrUpdateServerMetricsConfig(java.lang.String,models.TestServerMetricsConfiguration)` was added
* `beginUploadTestFile(java.lang.String,java.lang.String,com.azure.core.util.BinaryData)` was added
* `listTestProfiles(java.time.OffsetDateTime,java.time.OffsetDateTime,java.util.List,java.util.List)` was added
* `listTestProfiles(com.azure.core.http.rest.RequestOptions)` was added
* `listTests(java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime)` was added
* `deleteTest(java.lang.String)` was added
* `getTestFile(java.lang.String,java.lang.String)` was added
* `deleteTestProfile(java.lang.String)` was added
* `createOrUpdateTest(java.lang.String,models.LoadTest)` was added
* `listTestFiles(java.lang.String)` was added
* `deleteTestProfileWithResponse(java.lang.String,com.azure.core.http.rest.RequestOptions)` was added
* `createOrUpdateTestProfile(java.lang.String,models.TestProfile)` was added
* `getAppComponents(java.lang.String)` was added
* `listTests()` was added
* `listTestProfiles()` was added

## 1.0.24 (2025-07-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.4` to version `1.55.5`.
- Upgraded `azure-core-http-netty` from `1.15.12` to version `1.15.13`.


## 1.0.23 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.
- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.

## 1.1.0-beta.1 (2025-05-24)

### Features Added

- Upgraded all clients to use new API Version `2024-05-01-preview`
- Added models for all APIs supported by the service
- Added methods that work with and return models for all existing methods supported by both `LoadTestAdministration` and `LoadTestRun` clients
- Added methods to interact with `TestProfile` APIs in `LoadTestAdministrationClient.createOrUpdateTestProfile()`, `LoadTestAdministrationClient.getTestProfile()`, `LoadTestAdministrationClient.deleteTestProfile()` and `LoadTestAdministrationClient.listTestProfiles()` and their equivalent async variants
- Added methods to interact with `TestProfileRun` APIs in `LoadTestRunClient.beginTestProfileRun()`, `LoadTestRunClient.getTestProfileRun()`, `LoadTestRunClient.deleteTestProfileRun()` and `LoadTestRunClient.listTestProfileRuns()` and their equivalent async variants

## 1.0.22 (2025-03-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to version `1.55.3`.
- Upgraded `azure-core-http-netty` from `1.15.10` to version `1.15.11`.


## 1.0.21 (2025-02-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.54.1` to version `1.55.2`.
- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.10`.

## 1.0.20 (2025-01-20)

### Other Changes

- Add NOT_VALIDATED to the list of terminal states for the file validation poller.


## 1.0.19 (2024-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.53.0` to version `1.54.1`.
- Upgraded `azure-core-http-netty` from `1.15.5` to version `1.15.7`.


## 1.0.18 (2024-10-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.4` to version `1.15.5`.
- Upgraded `azure-core` from `1.52.0` to version `1.53.0`.


## 1.0.17 (2024-09-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.


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
