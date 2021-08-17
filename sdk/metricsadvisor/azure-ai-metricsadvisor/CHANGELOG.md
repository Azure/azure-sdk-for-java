# Release History

## 1.1.0-beta.1 (Unreleased)


## 1.0.1 (2021-08-11)
### Dependency Updates
- Updated `azure-core` to `1.19.0`.
- Updated `azure-core-http-netty` to `1.10.2`.

## 1.0.0 (2021-07-08)

### Features added

- Added `getAdmins` and `setAdmins` methods to `NotificationHook` model
- Added the `getMeasureType` and `setMeasureType` to `MetricBoundaryCondition` model
- Added `getViewers` and `setViewers` methods to `DataFeedOptions` model

### Breaking changes

- Removed the prefix `Anomaly` from all Alert Configuration APIs
- Removed the prefix `MetricAnomaly` from all Detection Configuration APIs
- Renamed `MetricAnomalyAlertConfiguration` to `MetricAlertConfiguration`
- Renamed `DetectionConditionsOperator` to `DetectionConditionOperator`
- Renamed the `getDimensionsToSplitAlert` method in the model `AnomalyAlertConfiguration` to `getDimensionsToSplitAlert`
- Renamed the `setDimensionsToSplitAlert` method in the model `AnomalyAlertConfiguration` to `setDimensionsToSplitAlert`
- Updated the model getter and setter methods for primitive types to use corresponding reference types
- Renamed `setCrossConditionOperator` method in all detection configuration condition models to `setConditionOperator`
- Renamed `getCrossConditionOperator` method in all detection configuration condition models to `getConditionOperator`
- Updated the constructors of `DataFeedMetric`, `DataFeedDimension`, `ChangeThresholdCondition`, `HardThresholdCondition`, `SmartDetectionCondition`, `SeverityCondition`, `MetricAnomalyAlertSnoozeCondition` and `TopNGroupScope` to take required parameters.
- Removed `SingleBoundaryCondition` model
- Removed `setSingleBoundary` and `setBothBoundary` methods in `MetricBoundaryCondition` model
- Replaced `listAnomalies` API with `listAnomaliesForAlert` and `listAnomaliesForDetectionConfig`
- Replaced `listIncidents` API with `listIncidentsForAlert` and `listIncidentsForDetectionConfig`

## 1.0.0-beta.4 (2021-06-09)

### Features added
- Added support for Azure Log Analytics DataFeed source
- Added data source credential API support to client
- Added authentication type support for data feed
- Added property `splitAlertByDimensions` to AnomalyAlertConfiguration model
- Added `clientOptions()` methods to the `MetricsAdvisorClientBuilder` and `MetricsAdvisorAdministrationClientBuilder`

### Breaking changes
- Replaced updateSubscriptionKey and updateApiKey into one method updateKey
- Deprecated support for HttpRequestDataFeed and ElasticsearchDataFeed source type
- Removed granularity type DataFeedGranularityType.PerSecond as it's not supported by the service anymore
- Renamed `value` and `expectedValue` to `valueOfRootNode` and `expectedValueOfRootNode`
- Renamed `top` parameter to `maxPageSize`
- Renamed method `listAnomaliesForAlert` and `listAnomaliesForDetectionConfig` to `listAnomalies`
- Renamed method `listIncidentsForAlert` and `listIncidentsForDetectionConfig` to `listIncidents`
- Renamed `ErrorCodeException` and `ErrorCode` to `MetricsAdvisorResponseException` and `MetricsAdvisorError`

## 1.0.0-beta.3 (2021-02-09)
- Support Azure Active Directory (AAD) authentication for Metrics Advisor clients. 
- Renamed method `listDimensionValuesWithAnomalies` and `ListDimensionValuesWithAnomaliesOptions`.
  to `listAnomalyDimensionValues` and  `ListAnomalyDimensionValuesOptions` respectively.
- Updated `DataFeed.metricIds` to return a `Map<metricName, metricId>`.
- Support updating api and subscription keys for `MetricsAdvisorKeyCredential`.

## 1.0.0-beta.2 (2020-11-10)

### Breaking changes
- Updated `createdDataFeed` method to take one `DataFeed` object.
- Renamed `listValuesOfDimensionWithAnomalies` method to `listDimensionValuesWithAnomalies`.
- Renamed model `ListValuesOfDimensionWithAnomaliesOptions` method to `ListDimensionValuesWithAnomaliesOptions`.
- Renamed properties `viewers` , `admins` and their accessors to `viewerEmails` and `adminEmails` respectively on
`DataFeedOptions` model.
- Renamed model `DataSourceMissingDataPointFillType` to `DataFeedMissingDataPointFillType`.
- Renamed properties on `MetricEnrichedSeriesData` and `MetricSeriesData` model.
- Renamed method `setSeverity` to `setSeverityRangeCondition` on `MetricAnomalyAlertConditions` model.
- Renamed property `confidenceScore` to `contributionScore` and its accessors on `IncidentRootCause` model.
- Removed model `ListMetricSeriesData` as top and skip parameters are not valid for this API.
- Moved `startTime` and `endTime` to positional arguments on several methods as they are required.
- Renamed Data feed ingestion granularity type to `"PerMinute"` and `"PerSecond"` instead of `"Minutely"` and `"Secondly"`.
- Renamed Feedback api's from `createMetricFeedback`, `getMetricFeedback` and `listMetricFeedbacks` 
to `addFeedback`, `getFeedback` and `listFeedback` respectively.
- Removed `getSubscriptionKey` and `getApiKey` from `MetricsAdvisorKeyCredential` and introduced `MetricsAdvisorKeys`.
- Renamed model `ErrorCode` to `MetricsAdvisorError` and `ErrorCodeException`
to `MetricsAdvisorResponseException`
## 1.0.0-beta.1 (2020-10-07)
Version 1.0.0-beta.1 is a preview of our efforts in creating a Azure Metrics Advisor client library that is developer-friendly
and idiomatic to the Java ecosystem. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

- Two client design:
    - `MetricsAdvisorAdministrationClient` to perform creation, updation and deletion of Metrics Advisor resources.
    - `MetricsAdvisorClient` helps with querying API's to helps with listing incidents, listing root causes of incidents
    and adding feedback to tune your model.
- Authentication with API key supported using `MetricsAdvisorKeyCredential("<subscription_key>", "<api_key>")`.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples)
demonstrate the new API.

