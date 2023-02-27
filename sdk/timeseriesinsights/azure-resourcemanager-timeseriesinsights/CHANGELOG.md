# Release History

## 1.0.0-beta.2 (2023-02-27)

- Azure Resource Manager TimeSeriesInsights client library for Java. This package contains Microsoft Azure SDK for TimeSeriesInsights Management SDK. Time Series Insights client. Package tag package-2020-05-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.EventHubEventSourceMutableProperties` was removed

* `models.Gen2EnvironmentResourceProperties` was removed

* `models.EventHubEventSourceCreationProperties` was removed

* `models.ReferenceDataSetResourceProperties` was removed

* `models.IoTHubEventSourceResourceProperties` was removed

* `models.Gen1EnvironmentCreationProperties` was removed

* `models.IoTHubEventSourceMutableProperties` was removed

* `models.EventHubEventSourceResourceProperties` was removed

* `models.Gen1EnvironmentResourceProperties` was removed

* `models.IoTHubEventSourceCreationProperties` was removed

* `models.ReferenceDataSetCreationProperties` was removed

#### `models.EventSourceMutableProperties` was modified

* `localTimestamp()` was removed
* `withLocalTimestamp(models.LocalTimestamp)` was removed

#### `models.Environments` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WarmStorageEnvironmentStatus` was modified

* `withMaxCount(java.lang.Integer)` was removed
* `withCurrentCount(java.lang.Integer)` was removed

#### `models.EventHubEventSourceUpdateParameters` was modified

* `withLocalTimestamp(models.LocalTimestamp)` was removed
* `localTimestamp()` was removed

#### `models.IoTHubEventSourceUpdateParameters` was modified

* `localTimestamp()` was removed
* `withLocalTimestamp(models.LocalTimestamp)` was removed

### Features Added

* `models.MetricAvailability` was added

* `models.IngressStartAtType` was added

* `models.LogSpecification` was added

* `models.ServiceSpecification` was added

* `models.MetricSpecification` was added

* `models.Dimension` was added

#### `models.ReferenceDataSetResource` was modified

* `resourceGroupName()` was added

#### `models.AzureEventSourceProperties` was modified

* `withTime(java.lang.String)` was added
* `withType(models.IngressStartAtType)` was added
* `withLocalTimestamp(models.LocalTimestamp)` was added

#### `models.EventHubEventSourceResource` was modified

* `localTimestamp()` was added
* `typePropertiesType()` was added
* `withTypePropertiesType(models.IngressStartAtType)` was added
* `withLocalTimestamp(models.LocalTimestamp)` was added
* `time()` was added
* `withTime(java.lang.String)` was added

#### `models.Environments` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `TimeSeriesInsightsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.EventHubEventSourceCreateOrUpdateParameters` was modified

* `type()` was added
* `withTime(java.lang.String)` was added
* `withType(models.IngressStartAtType)` was added
* `time()` was added
* `withLocalTimestampPropertiesLocalTimestamp(models.LocalTimestamp)` was added
* `localTimestampPropertiesLocalTimestamp()` was added

#### `models.IoTHubEventSourceCommonProperties` was modified

* `withTime(java.lang.String)` was added
* `withLocalTimestamp(models.LocalTimestamp)` was added
* `withType(models.IngressStartAtType)` was added

#### `models.Operation` was modified

* `serviceSpecification()` was added
* `origin()` was added

#### `models.IoTHubEventSourceResource` was modified

* `withLocalTimestamp(models.LocalTimestamp)` was added
* `withTypePropertiesType(models.IngressStartAtType)` was added
* `withTime(java.lang.String)` was added
* `time()` was added
* `typePropertiesType()` was added
* `localTimestamp()` was added

#### `TimeSeriesInsightsManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.IoTHubEventSourceCreateOrUpdateParameters` was modified

* `localTimestampPropertiesLocalTimestamp()` was added
* `type()` was added
* `time()` was added
* `withTime(java.lang.String)` was added
* `withLocalTimestampPropertiesLocalTimestamp(models.LocalTimestamp)` was added
* `withType(models.IngressStartAtType)` was added

#### `models.AccessPolicyResource` was modified

* `resourceGroupName()` was added

#### `models.EventSourceCommonProperties` was modified

* `localTimestamp()` was added
* `type()` was added
* `time()` was added
* `withType(models.IngressStartAtType)` was added
* `withTime(java.lang.String)` was added
* `withLocalTimestamp(models.LocalTimestamp)` was added

#### `models.EventHubEventSourceCommonProperties` was modified

* `withLocalTimestamp(models.LocalTimestamp)` was added
* `withTime(java.lang.String)` was added
* `withType(models.IngressStartAtType)` was added

## 1.0.0-beta.1 (2021-04-21)

- Azure Resource Manager TimeSeriesInsights client library for Java. This package contains Microsoft Azure SDK for TimeSeriesInsights Management SDK. Time Series Insights client. Package tag package-2020-05-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
