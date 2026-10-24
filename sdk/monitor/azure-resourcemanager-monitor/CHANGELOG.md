# Release History

## 2.54.0-beta.2 (2026-10-23)

- Package api-version DataCollectionApi: 2024-03-11, PrivateLinkScopesApi: 2023-06-01-preview, AutoScaleApi: 2025-09-01-preview, ActivityLogsApi: 2015-04-01, MetricsApi: 2024-02-01, ServiceDiagnosticsSettingsApi: 2016-09-01, AlertRulesIncidentsApi: 2016-03-01, LogProfilesApi: 2016-03-01, ActivityLogAlertsApi: 2023-01-01-preview, MetricAlertApi: 2026-01-01, ScheduledQueryRuleApi: 2025-01-01-preview, MetricBaselinesApi: 2019-03-01, ActionGroupsApi: 2024-10-01-preview, NetworkSecurityPerimeterApi: 2021-10-01, TenantActionGroups: 2023-05-01-preview, VmInsightsOnboarding: 2018-11-27-preview, DiagnosticsSettings: 2021-05-01-preview.

### Breaking Changes

#### `models.OperationDisplay` was removed

#### `models.DataCollectionRuleAssociationProxyOnlyResourceListResult` was removed

#### `models.DataCollectionEndpointResourceListResult` was removed

#### `models.EventCategoryCollection` was removed

#### `models.ScopedResourceListResult` was removed

#### `models.Operation` was removed

#### `models.MetricAlertResourceCollection` was removed

#### `models.DataCollectionRuleResourceListResult` was removed

#### `models.MetricDefinitionCollection` was removed

#### `models.MetricAlertStatus` was removed

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

#### `models.AutoscaleNotification` was modified

* `withOperation(java.lang.String)` was removed

#### `models.DataCollectionRuleMetadata` was modified

* `DataCollectionRuleMetadata()` was changed to private access

#### `models.MetricValue` was modified

* `MetricValue()` was changed to private access
* `withTotal(java.lang.Double)` was removed
* `withMaximum(java.lang.Double)` was removed
* `withCount(java.lang.Double)` was removed
* `withTimestamp(java.time.OffsetDateTime)` was removed
* `withMinimum(java.lang.Double)` was removed
* `withAverage(java.lang.Double)` was removed

#### `models.DataContainer` was modified

* `DataContainer()` was changed to private access
* `withWorkspace(models.WorkspaceInfo)` was removed

#### `models.CategoryType` was modified

* `models.CategoryType[] values()` -> `java.util.Collection values()`
* `toString()` was removed
* `valueOf(java.lang.String)` was removed

#### `models.ConditionOperator` was modified

* `valueOf(java.lang.String)` was removed
* `models.ConditionOperator[] values()` -> `java.util.Collection values()`
* `toString()` was removed

#### `models.BaselineMetadata` was modified

* `BaselineMetadata()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.HttpRequestInfo` was modified

* `HttpRequestInfo()` was changed to private access
* `withMethod(java.lang.String)` was removed
* `withClientRequestId(java.lang.String)` was removed
* `withClientIpAddress(java.lang.String)` was removed
* `withUri(java.lang.String)` was removed

#### `models.SingleBaseline` was modified

* `SingleBaseline()` was changed to private access
* `withHighThresholds(java.util.List)` was removed
* `withSensitivity(models.BaselineSensitivity)` was removed
* `withLowThresholds(java.util.List)` was removed

#### `models.WorkspaceInfo` was modified

* `WorkspaceInfo()` was changed to private access
* `withCustomerId(java.lang.String)` was removed
* `withLocation(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.SenderAuthorization` was modified

* `SenderAuthorization()` was changed to private access
* `withAction(java.lang.String)` was removed
* `withRole(java.lang.String)` was removed
* `withScope(java.lang.String)` was removed

#### `models.MetricAvailability` was modified

* `MetricAvailability()` was changed to private access
* `withTimeGrain(java.time.Duration)` was removed
* `withRetention(java.time.Duration)` was removed

#### `models.TimeSeriesBaseline` was modified

* `TimeSeriesBaseline()` was changed to private access
* `withMetadataValues(java.util.List)` was removed
* `withAggregation(java.lang.String)` was removed
* `withData(java.util.List)` was removed
* `withTimestamps(java.util.List)` was removed
* `withDimensions(java.util.List)` was removed

#### `models.DataCollectionRuleAssociationMetadata` was modified

* `DataCollectionRuleAssociationMetadata()` was changed to private access

#### `models.MetricNamespaceName` was modified

* `MetricNamespaceName()` was changed to private access
* `withMetricNamespaceName(java.lang.String)` was removed

#### `models.MetricSingleDimension` was modified

* `MetricSingleDimension()` was changed to private access
* `withName(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed

#### `models.MetricAlertStatusProperties` was modified

* `MetricAlertStatusProperties()` was changed to private access
* `withStatus(java.lang.String)` was removed
* `withTimestamp(java.time.OffsetDateTime)` was removed
* `withDimensions(java.util.Map)` was removed

#### `models.PredictiveValue` was modified

* `PredictiveValue()` was changed to private access
* `withTimestamp(java.time.OffsetDateTime)` was removed
* `withValue(double)` was removed

#### `models.TimeSeriesElement` was modified

* `TimeSeriesElement()` was changed to private access
* `withData(java.util.List)` was removed
* `withMetadatavalues(java.util.List)` was removed

#### `models.Context` was modified

* `Context()` was changed to private access
* `withNotificationSource(java.lang.String)` was removed
* `withContextType(java.lang.String)` was removed

#### `models.AlertSeverity` was modified

* `AlertSeverity()` was removed
* `fromLong(long)` was removed

#### `models.ActionDetail` was modified

* `ActionDetail()` was changed to private access
* `withSubState(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withSendTime(java.lang.String)` was removed
* `withMechanismType(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withDetail(java.lang.String)` was removed

### Features Added

* `models.PromQLCriteria` was added

* `models.IngestionQuotas` was added

* `models.EtwProviderDataSource` was added

* `models.IdentityType` was added

* `models.DirectDataSourcesSpec` was added

* `models.WindowsFirewallLogsDataSource` was added

* `models.NetworkSecurityPerimeterConfigurationProvisioningState` was added

* `models.KnownOtelTracesDirectDataSourceStreams` was added

* `models.OtelTracesDirectDataSource` was added

* `models.UserIdentityProperties` was added

* `models.DataCollectionEndpointResourceIdentity` was added

* `models.ServiceDiagnosticSettingsResourcePatch` was added

* `models.QueryFailingPeriods` was added

* `models.KnownAgentSettingName` was added

* `models.ScopedResourceKind` was added

* `models.NetworkSecurityPerimeterConfigurationProperties` was added

* `models.MetricResultType` was added

* `models.DynamicPromQLCriteria` was added

* `models.OtelLogsDirectDataSource` was added

* `models.DataCollectionEndpointMetadata` was added

* `models.DataSourcesSpecDataImports` was added

* `models.NetworkSecurityProfile` was added

* `models.KnownOtelLogsDirectDataSourceStreams` was added

* `models.ReferencesSpec` was added

* `models.Severity` was added

* `models.TenantActionGroupActionDetail` was added

* `models.KnownWindowsFirewallLogsDataSourceProfileFilter` was added

* `models.SubscriptionScopeMetricsRequestBodyParameters` was added

* `models.IngestionQuotasLogs` was added

* `models.RuleResolveConfiguration` was added

* `models.KnownOtelTracesDataSourceStreams` was added

* `models.DataCollectionRuleResourceSku` was added

* `models.DataCollectionRuleResourceIdentity` was added

* `models.AgentSetting` was added

* `models.KnownStorageBlobLookupType` was added

* `models.TenantActionGroupWebhookReceiver` was added

* `models.FailoverConfigurationSpec` was added

* `models.OtelTracesDataSourceResourceAttributeRouting` was added

* `models.StaticPromQLCriteria` was added

* `models.DataCollectionEndpointMetricsIngestion` was added

* `models.PlatformTelemetryDataSource` was added

* `models.ProvisioningIssueProperties` was added

* `models.DataImportSourcesEventHub` was added

* `models.DataCollectionRuleReferences` was added

* `models.OtelTracesDataSource` was added

* `models.EventHubDirectDestination` was added

* `models.DataCollectionRuleEndpoints` was added

* `models.ProvisioningIssue` was added

* `models.EventHubDestination` was added

* `models.LogsQuotaSpec` was added

* `models.IncidentReceiver` was added

* `models.OtelMetricsDirectDataSource` was added

* `models.OtelMetricsDataSourceResourceAttributeRouting` was added

* `models.AccessRuleProperties` was added

* `models.DataCollectionEndpointResourceSku` was added

* `models.ManagedServiceIdentityType` was added

* `models.DataCollectionRuleAgentSettings` was added

* `models.AccessRulePropertiesSubscription` was added

* `models.OtelLogsDataSourceResourceAttributeRouting` was added

* `models.MicrosoftFabricDestination` was added

* `models.PrivateLinkScopeProvisioningState` was added

* `models.PrometheusForwarderDataSource` was added

* `models.StorageBlobDestination` was added

* `models.PrivateLinkResourceProperties` was added

* `models.Sku` was added

* `models.ManagedServiceIdentity` was added

* `models.KnownPerformanceCountersOTelDataSourceStreams` was added

* `models.ApplicationInsights` was added

* `models.AccessRule` was added

* `models.KnownOtelLogsDataSourceStreams` was added

* `models.AccessRuleDirection` was added

* `models.NetworkSecurityPerimeter` was added

* `models.TenantNotificationRequestBody` was added

* `models.MetricAggregationType` was added

* `models.Identity` was added

* `models.MetricsIngestionEndpointSpec` was added

* `models.SkuTier` was added

* `models.OtelDataSourceResourceAttributeRouting` was added

* `models.PerformanceCountersOTelDataSource` was added

* `models.StorageBlob` was added

* `models.KnownLocationSpecProvisioningStatus` was added

* `models.OtelMetricsDataSource` was added

* `models.KnownPrometheusForwarderDataSourceStreams` was added

* `models.DataImportSources` was added

* `models.OtelLogsDataSource` was added

* `models.DataCollectionRuleIngestionQuotas` was added

* `models.ResourceAssociation` was added

* `models.ReferencesSpecEnrichmentData` was added

* `models.StorageTableDestination` was added

* `models.KnownEtwProviderDataSourceLogLevel` was added

* `models.PrivateLinkScopedResource` was added

* `models.AdxDestination` was added

* `models.EnrichmentData` was added

* `models.ResolveConfiguration` was added

* `models.MonitoringAccountDestination` was added

* `models.DataCollectionRuleDirectDataSources` was added

* `models.MultiPromQLCriteria` was added

* `models.EndpointsSpec` was added

* `models.IncidentManagementService` was added

* `models.UserAssignedIdentity` was added

* `models.TenantActionGroupPatchBody` was added

* `models.AgentSettingsSpec` was added

* `models.EventHubDataSource` was added

* `models.ResourceAssociationAccessMode` was added

* `models.ResourceForUpdateIdentity` was added

* `models.IssueType` was added

* `models.KnownEtwProviderType` was added

* `models.DataCollectionEndpointFailoverConfiguration` was added

* `models.LocationSpec` was added

* `models.IncidentServiceConnection` was added

* `models.ScopedResourceProvisioningState` was added

#### `models.DataCollectionRuleDataSources` was modified

* `withPrometheusForwarder(java.util.List)` was added
* `withDataImports(models.DataSourcesSpecDataImports)` was added
* `withPlatformTelemetry(java.util.List)` was added
* `withOtelLogs(java.util.List)` was added
* `withPerformanceCountersOTel(java.util.List)` was added
* `withEtwProviders(java.util.List)` was added
* `withOtelTraces(java.util.List)` was added
* `withWindowsFirewallLogs(java.util.List)` was added
* `withOtelMetrics(java.util.List)` was added

#### `models.MetricAlertResourcePatch` was modified

* `identity()` was added
* `customProperties()` was added
* `actionProperties()` was added
* `withCustomProperties(java.util.Map)` was added
* `withResolveConfiguration(models.ResolveConfiguration)` was added
* `withActionProperties(java.util.Map)` was added
* `resolveConfiguration()` was added
* `withIdentity(models.Identity)` was added

#### `models.ActivityLogAlertActionGroup` was modified

* `actionProperties()` was added
* `withActionProperties(java.util.Map)` was added

#### `models.Odatatype` was modified

* `MICROSOFT_AZURE_MONITOR_PROM_QLCRITERIA` was added

#### `models.DataCollectionEndpoint` was modified

* `privateLinkScopedResources()` was added
* `failoverConfiguration()` was added
* `metadata()` was added
* `withMetricsIngestion(models.DataCollectionEndpointMetricsIngestion)` was added
* `metricsIngestion()` was added

#### `models.Actions` was modified

* `withActionProperties(java.util.Map)` was added
* `actionProperties()` was added

#### `models.DataCollectionRuleMetadata` was modified

* `provisionedByResourceId()` was added
* `provisionedByImmutableId()` was added

#### `models.SyslogDataSource` was modified

* `transformKql()` was added
* `withTransformKql(java.lang.String)` was added

#### `models.WebhookReceiver` was modified

* `withManagedIdentity(java.lang.String)` was added
* `managedIdentity()` was added

#### `models.CategoryType` was modified

* `CategoryType()` was added

#### `models.ConditionOperator` was modified

* `ConditionOperator()` was added
* `GREATER_OR_LESS_THAN` was added

#### `models.LogFilesDataSource` was modified

* `transformKql()` was added
* `withTransformKql(java.lang.String)` was added

#### `models.DataSourcesSpec` was modified

* `otelTraces()` was added
* `withPerformanceCountersOTel(java.util.List)` was added
* `otelLogs()` was added
* `withDataImports(models.DataSourcesSpecDataImports)` was added
* `platformTelemetry()` was added
* `performanceCountersOTel()` was added
* `withOtelTraces(java.util.List)` was added
* `withPlatformTelemetry(java.util.List)` was added
* `etwProviders()` was added
* `withOtelLogs(java.util.List)` was added
* `withWindowsFirewallLogs(java.util.List)` was added
* `prometheusForwarder()` was added
* `otelMetrics()` was added
* `windowsFirewallLogs()` was added
* `withOtelMetrics(java.util.List)` was added
* `dataImports()` was added
* `withEtwProviders(java.util.List)` was added
* `withPrometheusForwarder(java.util.List)` was added

#### `models.Metadata` was modified

* `provisionedByImmutableId()` was added
* `provisionedByResourceId()` was added

#### `models.AutomationRunbookReceiver` was modified

* `managedIdentity()` was added
* `withManagedIdentity(java.lang.String)` was added

#### `models.AzureFunctionReceiver` was modified

* `withManagedIdentity(java.lang.String)` was added
* `managedIdentity()` was added

#### `MonitorManager` was modified

* `alertRuleClient()` was added

#### `models.IisLogsDataSource` was modified

* `transformKql()` was added
* `withTransformKql(java.lang.String)` was added

#### `models.DestinationsSpec` was modified

* `withMicrosoftFabric(java.util.List)` was added
* `withStorageAccounts(java.util.List)` was added
* `storageTablesDirect()` was added
* `storageAccounts()` was added
* `storageBlobsDirect()` was added
* `withEventHubsDirect(java.util.List)` was added
* `withEventHubs(java.util.List)` was added
* `withAzureDataExplorer(java.util.List)` was added
* `azureDataExplorer()` was added
* `eventHubs()` was added
* `microsoftFabric()` was added
* `withMonitoringAccounts(java.util.List)` was added
* `monitoringAccounts()` was added
* `withStorageBlobsDirect(java.util.List)` was added
* `eventHubsDirect()` was added
* `withStorageTablesDirect(java.util.List)` was added

#### `models.KnownDataCollectionRuleProvisioningState` was modified

* `CANCELED` was added

#### `models.NotificationRequestBody` was modified

* `incidentReceivers()` was added
* `withIncidentReceivers(java.util.List)` was added

#### `models.KnownDataCollectionEndpointProvisioningState` was modified

* `CANCELED` was added

#### `models.ScheduledQueryRuleResourcePatch` was modified

* `withResolveConfiguration(models.RuleResolveConfiguration)` was added
* `resolveConfiguration()` was added
* `withIdentity(models.Identity)` was added
* `identity()` was added

#### `models.KnownPublicNetworkAccessOptions` was modified

* `SECURED_BY_PERIMETER` was added

#### `models.DataCollectionRule` was modified

* `withDirectDataSources(models.DataCollectionRuleDirectDataSources)` was added
* `references()` was added
* `withReferences(models.DataCollectionRuleReferences)` was added
* `endpoints()` was added
* `directDataSources()` was added
* `withAgentSettings(models.DataCollectionRuleAgentSettings)` was added
* `ingestionQuotas()` was added
* `agentSettings()` was added

#### `models.DataFlow` was modified

* `builtInTransform()` was added
* `withCaptureOverflow(java.lang.Boolean)` was added
* `withBuiltInTransform(java.lang.String)` was added
* `captureOverflow()` was added

#### `models.DataCollectionRuleDestinations` was modified

* `withMonitoringAccounts(java.util.List)` was added
* `withAzureDataExplorer(java.util.List)` was added
* `withMicrosoftFabric(java.util.List)` was added
* `withStorageTablesDirect(java.util.List)` was added
* `withEventHubs(java.util.List)` was added
* `withEventHubsDirect(java.util.List)` was added
* `withStorageAccounts(java.util.List)` was added
* `withStorageBlobsDirect(java.util.List)` was added

#### `models.DataCollectionRuleAssociationMetadata` was modified

* `provisionedByResourceId()` was added
* `provisionedByImmutableId()` was added

#### `models.WindowsEventLogDataSource` was modified

* `transformKql()` was added
* `withTransformKql(java.lang.String)` was added

#### `models.PerfCounterDataSource` was modified

* `withTransformKql(java.lang.String)` was added
* `transformKql()` was added

#### `models.ResourceForUpdate` was modified

* `identity()` was added
* `withIdentity(models.ResourceForUpdateIdentity)` was added

#### `models.Kind` was modified

* `SIMPLE_LOG_ALERT` was added

#### `models.KnownSyslogDataSourceFacilityNames` was modified

* `AUDIT` was added
* `FTP` was added
* `ALERT` was added
* `NTP` was added
* `NOPRI` was added
* `CLOCK` was added

#### `models.KnownDataCollectionRuleAssociationProvisioningState` was modified

* `CANCELED` was added

#### `models.AutoscaleSettingResourcePatch` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Condition` was modified

* `withIgnoreDataBefore(java.time.OffsetDateTime)` was added
* `criterionType()` was added
* `withCriterionType(models.CriterionType)` was added
* `ignoreDataBefore()` was added
* `withAlertSensitivity(java.lang.String)` was added
* `withMinRecurrenceCount(java.lang.Long)` was added
* `minRecurrenceCount()` was added
* `alertSensitivity()` was added

#### `models.EventHubReceiver` was modified

* `withManagedIdentity(java.lang.String)` was added
* `managedIdentity()` was added

#### `models.KnownLogFilesDataSourceFormat` was modified

* `JSON` was added

#### `models.AlertSeverity` was modified

* `getValue()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `fromValue(java.lang.Long)` was added
* `equals(java.lang.Object)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `hashCode()` was added
* `toString()` was added

#### `models.LogicAppReceiver` was modified

* `withManagedIdentity(java.lang.String)` was added
* `managedIdentity()` was added

#### `models.ActionGroupPatchBody` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

## 2.53.12 (2026-10-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.4` to version `2.55.0`.

## 2.53.11 (2026-10-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.3` to version `2.54.4`.

## 2.53.10 (2026-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.2` to version `2.54.3`.

## 2.53.9 (2026-07-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.1` to version `2.54.2`.


## 2.53.8 (2026-05-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.0` to version `2.54.1`.

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
