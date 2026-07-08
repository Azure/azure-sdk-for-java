# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-04-08)

- Azure Resource Manager CloudHealth client library for Java. This package contains Microsoft Azure SDK for CloudHealth Management SDK.  Package api-version 2026-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ThresholdRule` was removed

#### `models.ModelDiscoverySettings` was removed

#### `models.SignalGroup` was removed

#### `models.AzureResourceSignalGroup` was removed

#### `models.DependenciesSignalGroup` was removed

#### `models.DynamicDetectionRule` was removed

#### `models.DynamicThresholdDirection` was removed

#### `models.AzureMonitorWorkspaceSignalGroup` was removed

#### `models.LogAnalyticsSignalGroup` was removed

#### `models.HealthModelUpdateProperties` was removed

#### `models.SignalAssignment` was removed

#### `models.DynamicThresholdModel` was removed

#### `models.AuthenticationSettingProperties` was modified

* `validate()` was removed

#### `models.HealthModelUpdate` was modified

* `withProperties(models.HealthModelUpdateProperties)` was removed
* `validate()` was removed
* `properties()` was removed

#### `models.DiscoveryRules` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.HealthModelProperties` was modified

* `withDiscovery(models.ModelDiscoverySettings)` was removed
* `dataplaneEndpoint()` was removed
* `validate()` was removed
* `discovery()` was removed

#### `models.SignalOperator` was modified

* `LOWER_THAN` was removed
* `GREATER_OR_EQUALS` was removed
* `LOWER_OR_EQUALS` was removed
* `EQUALS` was removed

#### `models.DependenciesAggregationType` was modified

* `THRESHOLDS` was removed

#### `models.UserAssignedIdentity` was modified

* `validate()` was removed

#### `models.HealthState` was modified

* `ERROR` was removed

#### `models.ManagedIdentityAuthenticationSettingProperties` was modified

* `validate()` was removed

#### `models.EntityCoordinates` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.HealthModel$Update` was modified

* `withProperties(models.HealthModelUpdateProperties)` was removed

#### `models.RelationshipProperties` was modified

* `deletionDate()` was removed
* `withLabels(java.util.Map)` was removed
* `validate()` was removed
* `labels()` was removed

#### `models.DiscoveryRuleProperties` was modified

* `numberOfDiscoveredEntities()` was removed
* `errorMessage()` was removed
* `withResourceGraphQuery(java.lang.String)` was removed
* `resourceGraphQuery()` was removed
* `deletionDate()` was removed
* `validate()` was removed

#### `models.LogAnalyticsQuerySignalDefinitionProperties` was modified

* `withLabels(java.util.Map)` was removed
* `validate()` was removed

#### `models.IconDefinition` was modified

* `validate()` was removed

#### `models.AuthenticationSettings` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.SignalDefinitions` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.EntityProperties` was modified

* `signals()` was removed
* `kind()` was removed
* `withSignals(models.SignalGroup)` was removed
* `withKind(java.lang.String)` was removed
* `withLabels(java.util.Map)` was removed
* `validate()` was removed
* `labels()` was removed
* `deletionDate()` was removed

#### `models.Entities` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.SignalDefinitionProperties` was modified

* `withLabels(java.util.Map)` was removed
* `validate()` was removed
* `deletionDate()` was removed
* `labels()` was removed

#### `models.EvaluationRule` was modified

* `withDegradedRule(models.ThresholdRule)` was removed
* `models.ThresholdRule degradedRule()` -> `models.ThresholdRuleV2 degradedRule()`
* `withDynamicDetectionRule(models.DynamicDetectionRule)` was removed
* `validate()` was removed
* `withUnhealthyRule(models.ThresholdRule)` was removed
* `dynamicDetectionRule()` was removed
* `models.ThresholdRule unhealthyRule()` -> `models.ThresholdRuleV2 unhealthyRule()`

#### `models.ManagedServiceIdentity` was modified

* `validate()` was removed

#### `models.Relationships` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.EntityAlerts` was modified

* `validate()` was removed

#### `models.PrometheusMetricsSignalDefinitionProperties` was modified

* `validate()` was removed
* `withLabels(java.util.Map)` was removed

#### `models.ResourceMetricSignalDefinitionProperties` was modified

* `withLabels(java.util.Map)` was removed
* `validate()` was removed

#### `models.AlertConfiguration` was modified

* `validate()` was removed

### Features Added

* `models.HealthStateTransition` was added

* `models.AzureResourceSignals` was added

* `models.SignalStatus` was added

* `models.PrometheusMetricsSignal` was added

* `models.EntityHistoryRequest` was added

* `models.LogAnalyticsSignal` was added

* `models.ExternalSignal` was added

* `models.AzureResourceSignal` was added

* `models.DiscoveryRuleKind` was added

* `models.DiscoveryError` was added

* `models.SignalGroups` was added

* `models.ThresholdRuleV2` was added

* `models.DiscoveryRuleSpecification` was added

* `models.EntityHistoryResponse` was added

* `models.SignalInstanceProperties` was added

* `models.ResourceGraphQuerySpecification` was added

* `models.HealthReportEvaluationRule` was added

* `models.SignalHistoryRequest` was added

* `models.DependenciesSignalGroupV2` was added

* `models.ApplicationInsightsTopologySpecification` was added

* `models.AzureMonitorWorkspaceSignals` was added

* `models.SignalHistoryDataPoint` was added

* `models.HealthReportRequest` was added

* `models.SignalHistoryResponse` was added

* `models.DependenciesAggregationUnit` was added

* `models.ExternalSignalGroup` was added

* `models.LogAnalyticsSignals` was added

#### `models.DiscoveryRules` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SignalOperator` was modified

* `GREATER_THAN_OR_EQUAL` was added
* `EQUAL` was added
* `NOT_EQUAL` was added
* `LESS_THAN_OR_EQUAL` was added
* `LESS_THAN` was added

#### `models.DependenciesAggregationType` was modified

* `MIN_HEALTHY` was added
* `MAX_NOT_HEALTHY` was added

#### `models.HealthState` was modified

* `UNHEALTHY` was added

#### `models.RelationshipProperties` was modified

* `withTags(java.util.Map)` was added
* `tags()` was added

#### `models.DiscoveryRuleProperties` was modified

* `withSpecification(models.DiscoveryRuleSpecification)` was added
* `specification()` was added
* `error()` was added

#### `models.LogAnalyticsQuerySignalDefinitionProperties` was modified

* `withTags(java.util.Map)` was added

#### `models.AuthenticationSettings` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SignalDefinitions` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.EntityProperties` was modified

* `tags()` was added
* `withTags(java.util.Map)` was added
* `signalGroups()` was added
* `withSignalGroups(models.SignalGroups)` was added

#### `models.Entities` was modified

* `getSignalHistory(java.lang.String,java.lang.String,java.lang.String,models.SignalHistoryRequest)` was added
* `getSignalHistoryWithResponse(java.lang.String,java.lang.String,java.lang.String,models.SignalHistoryRequest,com.azure.core.util.Context)` was added
* `getHistory(java.lang.String,java.lang.String,java.lang.String,models.EntityHistoryRequest)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `ingestHealthReportWithResponse(java.lang.String,java.lang.String,java.lang.String,models.HealthReportRequest,com.azure.core.util.Context)` was added
* `ingestHealthReport(java.lang.String,java.lang.String,java.lang.String,models.HealthReportRequest)` was added
* `getHistoryWithResponse(java.lang.String,java.lang.String,java.lang.String,models.EntityHistoryRequest,com.azure.core.util.Context)` was added

#### `models.SignalDefinitionProperties` was modified

* `withTags(java.util.Map)` was added
* `tags()` was added

#### `models.EvaluationRule` was modified

* `withDegradedRule(models.ThresholdRuleV2)` was added
* `withUnhealthyRule(models.ThresholdRuleV2)` was added

#### `models.Relationships` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PrometheusMetricsSignalDefinitionProperties` was modified

* `withTags(java.util.Map)` was added

#### `models.ResourceMetricSignalDefinitionProperties` was modified

* `withTags(java.util.Map)` was added

#### `models.Entity` was modified

* `getHistory(models.EntityHistoryRequest)` was added
* `getSignalHistory(models.SignalHistoryRequest)` was added
* `getSignalHistoryWithResponse(models.SignalHistoryRequest,com.azure.core.util.Context)` was added
* `ingestHealthReport(models.HealthReportRequest)` was added
* `getHistoryWithResponse(models.EntityHistoryRequest,com.azure.core.util.Context)` was added
* `ingestHealthReportWithResponse(models.HealthReportRequest,com.azure.core.util.Context)` was added

#### `models.SignalKind` was modified

* `EXTERNAL_SIGNAL` was added

## 1.0.0-beta.1 (2025-06-04)

- Azure Resource Manager CloudHealth client library for Java. This package contains Microsoft Azure SDK for CloudHealth Management SDK.  Package api-version 2025-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-cloudhealth Java SDK.
