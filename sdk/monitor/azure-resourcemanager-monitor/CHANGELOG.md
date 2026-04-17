# Release History

## 2.54.0-beta.2 (2026-04-17)

### Breaking Changes

#### `models.OperationDisplay` was removed

#### `models.AlertRuleLeafCondition` was removed

#### `models.OnboardingStatus` was removed

#### `models.DataCollectionRuleAssociationProxyOnlyResourceListResult` was removed

#### `models.DataCollectionEndpointResourceListResult` was removed

#### `models.EventCategoryCollection` was removed

#### `models.ScopedResourceListResult` was removed

#### `models.DataContainer` was removed

#### `models.Operation` was removed

#### `models.MetricAlertResourceCollection` was removed

#### `models.DataStatus` was removed

#### `models.WorkspaceInfo` was removed

#### `models.DataCollectionRuleResourceListResult` was removed

#### `models.MetricDefinitionCollection` was removed

#### `models.MetricAlertStatus` was removed

#### `models.SubscriptionScopeMetricDefinitionCollection` was removed

#### `models.LogProfileCollection` was removed

#### `models.ActionGroupList` was removed

#### `models.MetricBaselinesResponse` was removed

#### `models.MetricNamespaceCollection` was removed

#### `models.AzureMonitorPrivateLinkScopeListResult` was removed

#### `models.ScheduledQueryRuleResourceCollection` was removed

#### `models.IncidentListResult` was removed

#### `models.AlertRuleList` was removed

#### `models.EventDataCollection` was removed

#### `models.AutoscaleSettingResourceCollection` was removed

#### `models.IngestionQuotas` was modified

* `models.IngestionQuotas withLogs(models.IngestionQuotasLogs)` -> `models.IngestionQuotas withLogs(models.IngestionQuotasLogs)`

#### `models.IdentityType` was modified

* `SYSTEM_ASSIGNED_1` was removed
* `SYSTEM_ASSIGNED_USER_ASSIGNED` was removed
* `NONE_1` was removed
* `USER_ASSIGNED_1` was removed

#### `models.AutoscaleNotification` was modified

* `java.lang.String operation()` -> `models.OperationType operation()`

#### `models.DataCollectionEndpointResourceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.DataCollectionRuleMetadata` was modified

* `DataCollectionRuleMetadata()` was changed to private access

#### `models.MetricValue` was modified

* `MetricValue()` was changed to private access
* `withMinimum(java.lang.Double)` was removed
* `withCount(java.lang.Double)` was removed
* `withTotal(java.lang.Double)` was removed
* `withMaximum(java.lang.Double)` was removed
* `withTimestamp(java.time.OffsetDateTime)` was removed
* `withAverage(java.lang.Double)` was removed

#### `models.DataCollectionEndpointMetadata` was modified

* `DataCollectionEndpointMetadata()` was changed to private access

#### `models.SubscriptionScopeMetricsRequestBodyParameters` was modified

* `java.time.Duration interval()` -> `java.lang.String interval()`
* `withInterval(java.time.Duration)` was removed

#### `models.IngestionQuotasLogs` was modified

* `IngestionQuotasLogs()` was changed to private access
* `withMaxSizePerMinuteInGB(java.lang.String)` was removed
* `withMaxRequestsPerMinute(java.lang.String)` was removed

#### `models.ConditionOperator` was modified

* `models.ConditionOperator[] values()` -> `java.util.Collection values()`
* `toString()` was removed
* `valueOf(java.lang.String)` was removed

#### `models.BaselineMetadata` was modified

* `BaselineMetadata()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.HttpRequestInfo` was modified

* `HttpRequestInfo()` was changed to private access
* `withUri(java.lang.String)` was removed
* `withClientRequestId(java.lang.String)` was removed
* `withMethod(java.lang.String)` was removed
* `withClientIpAddress(java.lang.String)` was removed

#### `models.DataCollectionRuleResourceIdentity` was modified

* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.SingleBaseline` was modified

* `SingleBaseline()` was changed to private access
* `withSensitivity(models.BaselineSensitivity)` was removed
* `withHighThresholds(java.util.List)` was removed
* `withLowThresholds(java.util.List)` was removed

#### `models.FailoverConfigurationSpec` was modified

* `models.FailoverConfigurationSpec withLocations(java.util.List)` -> `models.FailoverConfigurationSpec withLocations(java.util.List)`
* `models.FailoverConfigurationSpec withActiveLocation(java.lang.String)` -> `models.FailoverConfigurationSpec withActiveLocation(java.lang.String)`

#### `models.DataCollectionRuleEndpoints` was modified

* `DataCollectionRuleEndpoints()` was changed to private access

#### `models.LogsQuotaSpec` was modified

* `models.LogsQuotaSpec withMaxSizePerMinuteInGB(java.lang.String)` -> `models.LogsQuotaSpec withMaxSizePerMinuteInGB(java.lang.String)`
* `models.LogsQuotaSpec withMaxRequestsPerMinute(java.lang.String)` -> `models.LogsQuotaSpec withMaxRequestsPerMinute(java.lang.String)`

#### `models.SenderAuthorization` was modified

* `SenderAuthorization()` was changed to private access
* `withScope(java.lang.String)` was removed
* `withRole(java.lang.String)` was removed
* `withAction(java.lang.String)` was removed

#### `models.MetricAvailability` was modified

* `MetricAvailability()` was changed to private access
* `withTimeGrain(java.time.Duration)` was removed
* `withRetention(java.time.Duration)` was removed

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.TimeSeriesBaseline` was modified

* `TimeSeriesBaseline()` was changed to private access
* `withMetadataValues(java.util.List)` was removed
* `withTimestamps(java.util.List)` was removed
* `withDimensions(java.util.List)` was removed
* `withAggregation(java.lang.String)` was removed
* `withData(java.util.List)` was removed

#### `models.ActivityLogAlertLeafCondition` was modified

* `withAnyOf(java.util.List)` was removed
* `withContainsAny(java.util.List)` was removed
* `withField(java.lang.String)` was removed
* `anyOf()` was removed
* `withEquals(java.lang.String)` was removed

#### `models.DataCollectionRuleAssociationMetadata` was modified

* `DataCollectionRuleAssociationMetadata()` was changed to private access

#### `models.MetricNamespaceName` was modified

* `MetricNamespaceName()` was changed to private access
* `withMetricNamespaceName(java.lang.String)` was removed

#### `models.MetricSingleDimension` was modified

* `MetricSingleDimension()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.DataCollectionRuleIngestionQuotas` was modified

* `DataCollectionRuleIngestionQuotas()` was changed to private access
* `withLogs(models.IngestionQuotasLogs)` was removed

#### `models.PrivateLinkScopedResource` was modified

* `PrivateLinkScopedResource()` was changed to private access
* `withScopeId(java.lang.String)` was removed
* `withResourceId(java.lang.String)` was removed

#### `models.MetricAlertStatusProperties` was modified

* `MetricAlertStatusProperties()` was changed to private access
* `withTimestamp(java.time.OffsetDateTime)` was removed
* `withDimensions(java.util.Map)` was removed
* `withStatus(java.lang.String)` was removed

#### `models.PredictiveValue` was modified

* `PredictiveValue()` was changed to private access
* `withTimestamp(java.time.OffsetDateTime)` was removed
* `withValue(double)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.TimeSeriesElement` was modified

* `TimeSeriesElement()` was changed to private access
* `withData(java.util.List)` was removed
* `withMetadatavalues(java.util.List)` was removed

#### `models.ResourceForUpdateIdentity` was modified

* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `models.Context` was modified

* `Context()` was changed to private access
* `withContextType(java.lang.String)` was removed
* `withNotificationSource(java.lang.String)` was removed

#### `models.ActionDetail` was modified

* `ActionDetail()` was changed to private access
* `withDetail(java.lang.String)` was removed
* `withMechanismType(java.lang.String)` was removed
* `withSendTime(java.lang.String)` was removed
* `withSubState(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.DataCollectionEndpointFailoverConfiguration` was modified

* `DataCollectionEndpointFailoverConfiguration()` was changed to private access
* `withLocations(java.util.List)` was removed
* `withActiveLocation(java.lang.String)` was removed

#### `models.LocationSpec` was modified

* `LocationSpec()` was changed to private access
* `withLocation(java.lang.String)` was removed
* `withProvisioningStatus(models.KnownLocationSpecProvisioningStatus)` was removed

### Features Added

* `models.OperationType` was added

* `models.NetworkSecurityPerimeterConfigurationProvisioningState` was added

* `models.ServiceDiagnosticSettingsResourcePatch` was added

* `models.ScopedResourceKind` was added

* `models.NetworkSecurityPerimeterConfigurationProperties` was added

* `models.NetworkSecurityProfile` was added

* `models.AlertRuleAnyOfOrLeafCondition` was added

* `models.Severity` was added

* `models.ProvisioningIssueProperties` was added

* `models.ProvisioningIssue` was added

* `models.IncidentReceiver` was added

* `models.AccessRuleProperties` was added

* `models.AccessRulePropertiesSubscription` was added

* `models.PrivateLinkScopeProvisioningState` was added

* `models.PrivateLinkResourceProperties` was added

* `models.AccessRule` was added

* `models.AccessRuleDirection` was added

* `models.NetworkSecurityPerimeter` was added

* `models.ErrorContract` was added

* `models.ResourceAssociation` was added

* `models.IncidentManagementService` was added

* `models.ErrorContractException` was added

* `models.ResourceAssociationAccessMode` was added

* `models.IssueType` was added

* `models.IncidentServiceConnection` was added

* `models.ScopedResourceProvisioningState` was added

#### `models.AutoscaleNotification` was modified

* `withOperation(models.OperationType)` was added

#### `models.ActivityLogAlertActionGroup` was modified

* `withActionProperties(java.util.Map)` was added
* `actionProperties()` was added

#### `models.WebhookReceiver` was modified

* `managedIdentity()` was added
* `withManagedIdentity(java.lang.String)` was added

#### `models.SubscriptionScopeMetricsRequestBodyParameters` was modified

* `withInterval(java.lang.String)` was added

#### `models.IngestionQuotasLogs` was modified

* `maxRequestsPerMinute()` was added
* `maxSizePerMinuteInGB()` was added

#### `models.ConditionOperator` was modified

* `ConditionOperator()` was added
* `GREATER_OR_LESS_THAN` was added

#### `models.AutomationRunbookReceiver` was modified

* `withManagedIdentity(java.lang.String)` was added
* `managedIdentity()` was added

#### `models.AzureFunctionReceiver` was modified

* `withManagedIdentity(java.lang.String)` was added
* `managedIdentity()` was added

#### `MonitorManager` was modified

* `alertRuleClient()` was added

#### `models.NotificationRequestBody` was modified

* `withIncidentReceivers(java.util.List)` was added
* `incidentReceivers()` was added

#### `models.ActivityLogAlertLeafCondition` was modified

* `containsAny()` was added
* `equals()` was added
* `field()` was added

#### `models.Kind` was modified

* `SIMPLE_LOG_ALERT` was added

#### `models.DataCollectionRuleIngestionQuotas` was modified

* `logs()` was added

#### `models.Condition` was modified

* `withIgnoreDataBefore(java.time.OffsetDateTime)` was added
* `withMinRecurrenceCount(java.lang.Long)` was added
* `alertSensitivity()` was added
* `ignoreDataBefore()` was added
* `withAlertSensitivity(java.lang.String)` was added
* `minRecurrenceCount()` was added
* `withCriterionType(models.CriterionType)` was added
* `criterionType()` was added

#### `models.EventHubReceiver` was modified

* `withManagedIdentity(java.lang.String)` was added
* `managedIdentity()` was added

#### `models.DataCollectionEndpointFailoverConfiguration` was modified

* `activeLocation()` was added
* `locations()` was added

#### `models.LogicAppReceiver` was modified

* `managedIdentity()` was added
* `withManagedIdentity(java.lang.String)` was added

#### `models.ActionGroupPatchBody` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

## 2.53.7 (2026-03-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.53.5` to version `2.54.0`.


## 2.53.6 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.53.5 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.0-beta.1 (2025-11-06)

### Other Changes

- Updated `api-version` of metric alert to `2024-03-01-preview`.
- Updated `api-version` of metric definitions to `2021-05-01`.
- Updated `api-version` of metrics to `2021-05-01`.
- Updated `api-version` of scheduled query rule to `2023-12-01`.
- Updated `api-version` of data collection to `2024-03-11`.

## 2.53.4 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.3 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.2 (2025-08-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.1 (2025-08-05)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.0 (2025-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.52.0 (2025-06-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.47.0 (2025-01-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.46.0 (2024-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.45.0 (2024-11-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0 (2024-09-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.42.0 (2024-08-23)

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.38.0 (2024-04-16)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.31.0 (2023-09-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.30.0 (2023-08-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.29.0 (2023-07-28)

### Bugs Fixed

- Fixed a bug that methods in `MetricDefinitions` and `DiagnosticSettings`, which have `resourceId` as one of their 
  parameters, throw exception on resources whose names contain white spaces.
- Fixed `DiagnosticSettings.deleteByIds()` to make it work.

## 2.28.0 (2023-06-25)

### Bugs Fixed

- Fixed a bug in `DiagnosticSetting` initialization where category groups overwrite log settings.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0-beta.1 (2023-04-28)

Preview release for `api-version` `2022-08-01-preview`.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Bugs Fixed

- Fixed a bug that `DiagnosticSetting` initialization failed, when category group exists in `LogSettings`.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.23.0 (2023-01-27)

### Breaking Changes

- Class `LogSearchRuleResourceInner` changed to `ScheduledQueryRuleResourceInner` in `ScheduledQueryRulesClient`.
- Removed redundant classes like `ProxyResourceAutoGenerated`, `ResourceAutoGenerated`, `AzureResource`, `TrackedResource`. They are replaced by either `Resource` or `ProxyResource`.

### Bugs Fixed

- Fixed bug that client flattening of "properties" property of `DataCollectionRuleResourceInner`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `composite-v1`.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.20.0 (2022-10-26)

### Bugs Fixed

- Fixed a bug that `DiagnosticSettings.get()` throws NPE when it has no metrics or logs configured.
- Fixed a bug that `ActivityLogAlerts.list()` throws NPE when there are `ActivityLogAlert`s with condition of null `field` and `equals`.

## 2.19.0 (2022-09-23)

### Breaking Changes

- Removed unused classes.
- `enableReceiver` in `ActionGroups` will throw an exception if the receiver is already enabled.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `composite-v1`.

## 2.18.0 (2022-08-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.10.0 (2021-11-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated `api-version` to `2021-04-01`.

### Breaking Changes

- Class `AggregationType` is renamed to `AggregationTypeEnum`.
- Enum `Unit` changed to subclass of `ExpandableStringEnum`.

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
