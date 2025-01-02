# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.5 (2024-10-14)

- Azure Resource Manager SecurityInsights client library for Java. This package contains Microsoft Azure SDK for SecurityInsights Management SDK. API spec for Microsoft.SecurityInsights (Azure Security Insights) resource provider. Package tag package-preview-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.InsightQueryItemPropertiesReferenceTimeRange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InsightQueryItemPropertiesTableQuery` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FusionSourceSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutomationRuleCondition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `conditionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AwsS3CheckRequirements` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InsightQueryItemPropertiesDefaultTimeRange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InsightQueryItemProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExpansionResultsMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContentPathMap` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityAnalytics` was modified

* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `id()` was added
* `systemData()` was added

#### `models.MetadataList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MdatpCheckRequirements` was modified

* `kind()` was added
* `tenantId()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withTenantId(java.lang.String)` was added

#### `models.ThreatIntelligenceSortingCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodelessUiConnectorConfigPropertiesDataTypesItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecurityMLAnalyticsSettingsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AatpCheckRequirements` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withTenantId(java.lang.String)` was added
* `tenantId()` was added
* `kind()` was added

#### `models.IncidentAdditionalData` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MLBehaviorAnalyticsAlertRuleTemplate` was modified

* `name()` was added
* `type()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExpansionEntityQuery` was modified

* `type()` was added
* `kind()` was added
* `id()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TIDataConnector` was modified

* `type()` was added
* `id()` was added
* `systemData()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added

#### `models.MstiDataConnectorDataTypesBingSafetyPhishingUrl` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EnrichmentDomainWhoisContact` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfficeDataConnector` was modified

* `kind()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `systemData()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreatIntelligenceAlertRuleTemplate` was modified

* `kind()` was added
* `name()` was added
* `type()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.AzureResourceEntity` was modified

* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `kind()` was added

#### `models.ScheduledAlertRuleTemplate` was modified

* `id()` was added
* `kind()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActionPropertiesBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityQueryItemProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityGetInsightsParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreatIntelligenceAlertRule` was modified

* `systemData()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added

#### `models.PropertyArrayChangedConditionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `conditionType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Anomalies` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `name()` was added
* `kind()` was added

#### `models.IoTCheckRequirements` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TimelineError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlertRuleTemplatesList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityTimelineParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecurityAlert` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added
* `type()` was added
* `kind()` was added

#### `models.NicEntity` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `name()` was added
* `systemData()` was added
* `id()` was added

#### `models.NrtAlertRuleTemplate` was modified

* `type()` was added
* `id()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `name()` was added

#### `models.AwsS3DataConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TiTaxiiCheckRequirements` was modified

* `tenantId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withTenantId(java.lang.String)` was added

#### `models.CodelessUiConnectorConfigPropertiesConnectivityCriteriaItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TeamProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PermissionsResourceProviderItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GitHubResourceInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MetadataDependencies` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BookmarkTimelineItem` was modified

* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ActivityEntityQueriesPropertiesQueryDefinitions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FusionAlertRuleTemplate` was modified

* `systemData()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `type()` was added
* `name()` was added

#### `models.IoTDeviceEntity` was modified

* `kind()` was added
* `name()` was added
* `type()` was added
* `systemData()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GeoLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BookmarkEntityMappings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfficeConsentList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InstructionSteps` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PropertyConditionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `conditionType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodelessConnectorPollingAuthProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TimelineAggregation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EyesOn` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `type()` was added
* `systemData()` was added
* `name()` was added

#### `models.BookmarkExpandResponseValue` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PropertyArrayConditionProperties` was modified

* `conditionType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FusionSubTypeSeverityFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataTypeDefinitions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfficeIrmCheckRequirements` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `tenantId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withTenantId(java.lang.String)` was added

#### `models.FusionScenarioExclusionPattern` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetInsightsResultsMetadata` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudApplicationEntity` was modified

* `name()` was added
* `type()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `systemData()` was added

#### `models.MtpCheckRequirements` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withTenantId(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `tenantId()` was added
* `kind()` was added

#### `models.Office365ProjectCheckRequirements` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `tenantId()` was added
* `withTenantId(java.lang.String)` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityTimelineItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added

#### `models.ClientInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManualTriggerRequestBody` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityExpandParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AnomalyTimelineItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AscDataConnector` was modified

* `type()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added

#### `models.ThreatIntelligenceGranularMarkingModel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PlaybookActionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataConnectorTenantId` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InsightsTableResultColumnsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.McasDataConnector` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `type()` was added
* `systemData()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added

#### `models.IncidentConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TiTaxiiDataConnectorDataTypesTaxiiClient` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivityEntityQueryTemplatePropertiesQueryDefinitions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MstiDataConnectorDataTypes` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InsightQueryItemPropertiesAdditionalQuery` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountEntity` was modified

* `systemData()` was added
* `kind()` was added
* `name()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.ConnectedEntity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreatIntelligenceKillChainPhase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PermissionsCustomsItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataConnectorConnectBody` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityQueryItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added

#### `models.SecurityMLAnalyticsSettingsDataSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MtpDataConnectorDataTypes` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreatIntelligenceMetricEntity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IncidentPropertiesAction` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FusionTemplateSourceSubType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectorInstructionModelBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectivityCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecurityMLAnalyticsSetting` was modified

* `kind()` was added

#### `models.MailboxEntity` was modified

* `systemData()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `type()` was added
* `id()` was added

#### `models.SourceControlList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityInsightItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InsightQueryItemPropertiesTableQueryQueriesDefinitionsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.McasCheckRequirements` was modified

* `kind()` was added
* `tenantId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withTenantId(java.lang.String)` was added

#### `models.EntityCommonProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MetadataAuthor` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertRuleTemplate` was modified

* `kind()` was added

#### `models.FusionSubTypeSeverityFiltersItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RepoList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Settings` was modified

* `kind()` was added

#### `models.Dynamics365DataConnectorDataTypesDynamics365CdsActivities` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityExpandResponseValue` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetadataCategories` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IncidentOwnerInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodelessConnectorPollingResponseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AwsS3DataConnectorDataTypesLogs` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HostEntity` was modified

* `id()` was added
* `type()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `kind()` was added

#### `models.AlertsDataTypeOfDataConnector` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MtpDataConnectorDataTypesIncidents` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CodelessConnectorPollingRequestProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegistryValueEntity` was modified

* `systemData()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added

#### `models.RegistryKeyEntity` was modified

* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `kind()` was added
* `type()` was added
* `name()` was added

#### `models.AwsCloudTrailDataConnectorDataTypesLogs` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutomationRulePropertyArrayValuesCondition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlertRuleTemplatePropertiesBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GetInsightsErrorKind` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AwsCloudTrailDataConnector` was modified

* `kind()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `systemData()` was added

#### `models.Office365ProjectConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityInsightItemQueryTimeInterval` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutomationRulePropertyValuesCondition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreatIntelligenceExternalReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutomationRuleTriggeringLogic` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FusionSourceSubTypeSetting` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutomationRuleAction` was modified

* `actionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActionsList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AscCheckRequirements` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added

#### `models.AutomationRulePropertyValuesChangedCondition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnrichmentDomainWhoisContacts` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreatIntelligenceInformationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreatIntelligenceFilteringCriteria` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlertRuleTemplateWithMitreProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `lastUpdatedDateUtc()` was added
* `createdDateUtc()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceProvider` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GroupingConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AadCheckRequirements` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withTenantId(java.lang.String)` was added
* `tenantId()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreatIntelligenceMetrics` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Office365ProjectDataConnector` was modified

* `id()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.SecurityGroupEntity` was modified

* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added
* `type()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added

#### `models.EnrichmentDomainWhoisDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetadataSupport` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActionRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `systemData()` was added
* `id()` was added

#### `models.ExpansionResultAggregation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TICheckRequirements` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `tenantId()` was added
* `withTenantId(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduledAlertRuleCommonProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MtpDataConnector` was modified

* `type()` was added
* `name()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.EntityQueryItemPropertiesDataTypesItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NrtAlertRule` was modified

* `name()` was added
* `id()` was added
* `kind()` was added
* `systemData()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.McasDataConnectorDataTypes` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreatIntelligenceMetric` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ActivityTimelineItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IoTDataConnector` was modified

* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `type()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.WatchlistList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OfficeDataConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AatpDataConnector` was modified

* `systemData()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.CustomsPermission` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.QueryBasedAlertRuleTemplateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutomationRulesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfficePowerBIConnectorDataTypesLogs` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WatchlistItemList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TiTaxiiDataConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CodelessUiConnectorConfigPropertiesSampleQueriesItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduledAlertRule` was modified

* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `kind()` was added

#### `models.AnomalySecurityMLAnalyticsSettings` was modified

* `kind()` was added
* `id()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.Dynamics365DataConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AwsCloudTrailDataConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MetadataSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MicrosoftSecurityIncidentCreationAlertRuleCommonProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PropertyChangedConditionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `conditionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RequiredPermissions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileHashEntity` was modified

* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `kind()` was added
* `id()` was added
* `name()` was added

#### `models.Deployment` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ThreatIntelligenceIndicatorModel` was modified

* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `id()` was added
* `type()` was added
* `systemData()` was added

#### `models.IncidentCommentList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityMapping` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecurityAlertPropertiesConfidenceReasonsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CodelessConnectorPollingPagingProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodelessUiConnectorConfigPropertiesInstructionStepsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InsightQueryItem` was modified

* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MicrosoftSecurityIncidentCreationAlertRuleTemplate` was modified

* `kind()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `id()` was added

#### `models.ThreatIntelligenceParsedPatternTypeValue` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodelessApiPollingDataConnector` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added
* `name()` was added
* `id()` was added
* `type()` was added

#### `models.ThreatIntelligenceInformation` was modified

* `kind()` was added

#### `models.Webhook` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertDetailsOverride` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutomationRuleModifyPropertiesAction` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `actionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TiTaxiiDataConnector` was modified

* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added
* `type()` was added
* `kind()` was added

#### `models.BookmarkList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertRule` was modified

* `kind()` was added

#### `models.AutomationRulePropertyArrayChangedValuesCondition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TimelineResultsMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlertRuleTemplateDataSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MstiDataConnector` was modified

* `kind()` was added
* `id()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AwsCloudTrailCheckRequirements` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added

#### `models.EntityQueryTemplateList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityQuery` was modified

* `kind()` was added

#### `models.AlertRulesList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BooleanConditionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `conditionType()` was added

#### `models.SecurityAlertTimelineItem` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DnsEntity` was modified

* `systemData()` was added
* `kind()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `type()` was added

#### `models.ActivityCustomEntityQuery` was modified

* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `kind()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.EventGroupingSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AwsS3DataConnector` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `kind()` was added
* `id()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.MLBehaviorAnalyticsAlertRule` was modified

* `id()` was added
* `name()` was added
* `systemData()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.DataConnectorList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Permissions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileEntity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `type()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `kind()` was added

#### `models.CodelessUiDataConnector` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added
* `systemData()` was added
* `type()` was added

#### `models.HuntingBookmark` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `type()` was added
* `id()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InsightsTableResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDevOpsResourceInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomEntityQuery` was modified

* `name()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `systemData()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GraphQueries` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RelationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreatIntelligenceAppendTags` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MdatpDataConnector` was modified

* `kind()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.OfficeAtpDataConnector` was modified

* `name()` was added
* `kind()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MailClusterEntity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `type()` was added
* `kind()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.LastDataReceivedDataType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IncidentEntitiesResultsMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FusionAlertRule` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `kind()` was added
* `name()` was added
* `systemData()` was added

#### `models.MetadataPatch` was modified

* `systemData()` was added
* `type()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.Dynamics365DataConnector` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `name()` was added
* `systemData()` was added
* `kind()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MicrosoftSecurityIncidentCreationAlertRule` was modified

* `id()` was added
* `name()` was added
* `type()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TIDataConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataConnectorDataTypeCommon` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivityEntityQueryTemplate` was modified

* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `id()` was added
* `type()` was added

#### `models.Office365ProjectConnectorDataTypesLogs` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RepositoryResourceInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfficePowerBIDataConnector` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `id()` was added
* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `name()` was added

#### `models.EnrichmentDomainWhoisRegistrarDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValidationError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Entity` was modified

* `kind()` was added

#### `models.BookmarkExpandParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CodelessUiConnectorConfigPropertiesGraphQueriesItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutomationRuleRunPlaybookAction` was modified

* `actionType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OfficePowerBIConnectorDataTypes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MailMessageEntity` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `name()` was added

#### `models.ThreatIntelligenceParsedPattern` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OfficePowerBICheckRequirements` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `tenantId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withTenantId(java.lang.String)` was added

#### `models.UrlEntity` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `systemData()` was added
* `id()` was added

#### `models.OfficeDataConnectorDataTypesTeams` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Dynamics365CheckRequirements` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `tenantId()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withTenantId(java.lang.String)` was added

#### `models.SampleQueries` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MstiCheckRequirements` was modified

* `withTenantId(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `tenantId()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CodelessUiConnectorConfigProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfficeDataConnectorDataTypesExchange` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FusionTemplateSubTypeSeverityFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Repository` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityQueryTemplate` was modified

* `kind()` was added

#### `models.IncidentLabel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InstructionStepsInstructionsItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InsightQueryItemPropertiesTableQueryQueriesDefinitionsPropertiesItemsItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityEdges` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityFieldMapping` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InsightQueryItemPropertiesTableQueryColumnsDefinitionsItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataConnector` was modified

* `kind()` was added

#### `models.FieldMapping` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ThreatIntelligence` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileImportList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfficeDataConnectorDataTypesSharePoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IpEntity` was modified

* `name()` was added
* `id()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added
* `kind()` was added

#### `models.OfficeAtpCheckRequirements` was modified

* `tenantId()` was added
* `withTenantId(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Customs` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Ueba` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `systemData()` was added
* `type()` was added

#### `models.OfficeIrmDataConnector` was modified

* `systemData()` was added
* `kind()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `id()` was added

#### `models.SubmissionMailEntity` was modified

* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `systemData()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `type()` was added

#### `models.AadDataConnector` was modified

* `systemData()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `type()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added

#### `models.Availability` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IncidentList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ProcessEntity` was modified

* `id()` was added
* `systemData()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.AutomationRuleBooleanCondition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceWithEtag` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MstiDataConnectorDataTypesMicrosoftEmergingThreatFeed` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IncidentInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataConnectorWithAlertsProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FusionTemplateSourceSetting` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TIDataConnectorDataTypesIndicators` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataConnectorsCheckRequirements` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CodelessConnectorPollingConfigProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityQueryList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MalwareEntity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `type()` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `systemData()` was added

#### `models.ActivityEntityQuery` was modified

* `type()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `systemData()` was added
* `kind()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.4 (2022-09-26)

- Azure Resource Manager SecurityInsights client library for Java. This package contains Microsoft Azure SDK for SecurityInsights Management SDK. API spec for Microsoft.SecurityInsights (Azure Security Insights) resource provider. Package tag package-preview-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Constant74` was removed

* `models.ProvisioningState` was removed

#### `models.Watchlist` was modified

* `sasUri()` was removed
* `provisioningState()` was removed

#### `models.EntityAnalytics` was modified

* `isEnabled()` was removed

#### `models.Watchlist$Update` was modified

* `withSasUri(java.lang.String)` was removed

#### `models.Watchlist$Definition` was modified

* `withSasUri(java.lang.String)` was removed

#### `models.GetInsightsError` was modified

* `withErrorMessage(java.lang.String)` was removed
* `withQueryId(java.lang.String)` was removed
* `errorMessage()` was removed
* `queryId()` was removed
* `validate()` was removed
* `withKind(java.lang.String)` was removed
* `kind()` was removed

#### `models.EntityQueryTemplates` was modified

* `list(java.lang.String,java.lang.String,models.Constant74,com.azure.core.util.Context)` was removed

### Features Added

* `models.AutomationRulePropertyArrayConditionSupportedArrayType` was added

* `models.SecurityMLAnalyticsSettingsList` was added

* `models.PropertyArrayChangedConditionProperties` was added

* `models.NicEntity` was added

* `models.IngestionMode` was added

* `models.PropertyArrayConditionProperties` was added

* `models.AutomationRulePropertyArrayConditionSupportedArrayConditionType` was added

* `models.FileImport$DefinitionStages` was added

* `models.DeviceImportance` was added

* `models.AutomationRulePropertyArrayChangedConditionSupportedArrayType` was added

* `models.AnomalyTimelineItem` was added

* `models.SecurityMLAnalyticsSettingsKind` was added

* `models.SecurityMLAnalyticsSettingsDataSource` was added

* `models.SecurityMLAnalyticsSetting` was added

* `models.Constant88` was added

* `models.EntityProviders` was added

* `models.FileImportState` was added

* `models.AutomationRulePropertyArrayValuesCondition` was added

* `models.GetInsightsErrorKind` was added

* `models.FileMetadata` was added

* `models.FileFormat` was added

* `models.SecurityMLAnalyticsSettings` was added

* `models.AutomationRulePropertyValuesChangedCondition` was added

* `models.FileImport` was added

* `models.FileImportContentType` was added

* `models.FileImports` was added

* `models.AnomalySecurityMLAnalyticsSettings` was added

* `models.PropertyChangedConditionProperties` was added

* `models.AutomationRulePropertyArrayChangedValuesCondition` was added

* `models.AutomationRulePropertyChangedConditionSupportedChangedType` was added

* `models.BooleanConditionProperties` was added

* `models.ValidationError` was added

* `models.FileImport$Definition` was added

* `models.AutomationRulePropertyArrayChangedConditionSupportedChangeType` was added

* `models.AutomationRuleBooleanConditionSupportedOperator` was added

* `models.SettingsStatus` was added

* `models.FileImportList` was added

* `models.DeleteStatus` was added

* `models.AutomationRuleBooleanCondition` was added

* `models.AutomationRulePropertyChangedConditionSupportedPropertyType` was added

#### `models.EntityAnalytics` was modified

* `entityProviders()` was added
* `withEntityProviders(java.util.List)` was added

#### `models.NrtAlertRuleTemplate` was modified

* `withEventGroupingSettings(models.EventGroupingSettings)` was added
* `eventGroupingSettings()` was added

#### `models.IoTDeviceEntity` was modified

* `isScanner()` was added
* `purdueLayer()` was added
* `zone()` was added
* `withImportance(models.DeviceImportance)` was added
* `site()` was added
* `owners()` was added
* `isAuthorized()` was added
* `deviceSubType()` was added
* `isProgramming()` was added
* `nicEntityIds()` was added
* `importance()` was added
* `sensor()` was added

#### `models.GetInsightsError` was modified

* `fromString(java.lang.String)` was added
* `values()` was added

#### `models.DataConnectorConnectBody` was modified

* `withOutputStream(java.lang.String)` was added
* `withDataCollectionEndpoint(java.lang.String)` was added
* `outputStream()` was added
* `withDataCollectionRuleImmutableId(java.lang.String)` was added
* `dataCollectionRuleImmutableId()` was added
* `dataCollectionEndpoint()` was added

#### `models.IncidentOwnerInfo` was modified

* `withOwnerType(models.OwnerType)` was added

#### `SecurityInsightsManager` was modified

* `fileImports()` was added
* `securityMLAnalyticsSettings()` was added

#### `models.NrtAlertRule` was modified

* `withEventGroupingSettings(models.EventGroupingSettings)` was added
* `eventGroupingSettings()` was added

#### `models.QueryBasedAlertRuleTemplateProperties` was modified

* `eventGroupingSettings()` was added
* `withEventGroupingSettings(models.EventGroupingSettings)` was added

#### `models.EntityQueryTemplates` was modified

* `list(java.lang.String,java.lang.String,models.Constant88,com.azure.core.util.Context)` was added

## 1.0.0-beta.3 (2022-05-16)

- Azure Resource Manager SecurityInsights client library for Java. This package contains Microsoft Azure SDK for SecurityInsights Management SDK. API spec for Microsoft.SecurityInsights (Azure Security Insights) resource provider. Package tag package-preview-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.Watchlist` was modified

* `resourceGroupName()` was added

#### `SecurityInsightsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.MetadataModel` was modified

* `resourceGroupName()` was added

#### `models.Bookmark` was modified

* `resourceGroupName()` was added

#### `models.Relation` was modified

* `resourceGroupName()` was added

#### `models.Incident` was modified

* `resourceGroupName()` was added

#### `SecurityInsightsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.WatchlistItem` was modified

* `resourceGroupName()` was added

#### `models.AutomationRule` was modified

* `resourceGroupName()` was added

#### `models.IncidentComment` was modified

* `resourceGroupName()` was added

#### `models.ActionResponse` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.2 (2022-03-23)

- Azure Resource Manager SecurityInsights client library for Java. This package contains Microsoft Azure SDK for SecurityInsights Management SDK. API spec for Microsoft.SecurityInsights (Azure Security Insights) resource provider. Package tag package-preview-2022-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AutomationRuleModifyPropertiesActionConfiguration` was removed

* `models.QueryBasedAlertRuleProperties` was removed

* `models.AutomationRuleConditionType` was removed

* `models.Source` was removed

* `models.AutomationRuleActionType` was removed

* `models.AutomationRulePropertyValuesConditionProperties` was removed

* `models.AutomationRuleRunPlaybookActionConfiguration` was removed

* `models.Constant69` was removed

* `models.ThreatIntelligenceResourceKind` was removed

* `models.ThreatIntelligenceIndicatorModelForRequestBody` was removed

#### `models.AutomationRule$DefinitionStages` was modified

* Stage 2, 3, 4, 5 was added

#### `models.Watchlist` was modified

* `watchlistItemsCount()` was removed
* `models.Source source()` -> `java.lang.String source()`

#### `models.AutomationRules` was modified

* `void deleteById(java.lang.String)` -> `java.lang.Object deleteById(java.lang.String)`
* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `java.lang.Object delete(java.lang.String,java.lang.String,java.lang.String)`

#### `models.Watchlist$Update` was modified

* `withSource(models.Source)` was removed
* `withWatchlistItemsCount(java.lang.Integer)` was removed

#### `models.Watchlist$Definition` was modified

* `withSource(models.Source)` was removed
* `withWatchlistItemsCount(java.lang.Integer)` was removed

#### `models.ThreatIntelligenceIndicators` was modified

* `replaceTagsWithResponse(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModelForRequestBody,com.azure.core.util.Context)` was removed
* `replaceTags(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModelForRequestBody)` was removed
* `createIndicatorWithResponse(java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModelForRequestBody,com.azure.core.util.Context)` was removed
* `create(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModelForRequestBody)` was removed
* `createWithResponse(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModelForRequestBody,com.azure.core.util.Context)` was removed
* `createIndicator(java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModelForRequestBody)` was removed

#### `models.WatchlistItems` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WatchlistItem$Definition` was modified

* `withItemsKeyValue(java.lang.Object)` was removed
* `withEntityMapping(java.lang.Object)` was removed

#### `models.WatchlistItem` was modified

* `java.lang.Object itemsKeyValue()` -> `java.util.Map itemsKeyValue()`
* `java.lang.Object entityMapping()` -> `java.util.Map entityMapping()`

#### `models.AutomationRulePropertyValuesCondition` was modified

* `conditionProperties()` was removed
* `withConditionProperties(models.AutomationRulePropertyValuesConditionProperties)` was removed

#### `models.QueryBasedAlertRuleTemplateProperties` was modified

* `tactics()` was removed
* `withTactics(java.util.List)` was removed

#### `models.WatchlistItem$Update` was modified

* `withEntityMapping(java.lang.Object)` was removed
* `withItemsKeyValue(java.lang.Object)` was removed

#### `models.AutomationRuleModifyPropertiesAction` was modified

* `models.AutomationRuleModifyPropertiesActionConfiguration actionConfiguration()` -> `models.IncidentPropertiesAction actionConfiguration()`
* `withActionConfiguration(models.AutomationRuleModifyPropertiesActionConfiguration)` was removed

#### `models.Watchlists` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.WatchlistsDeleteResponse deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `com.azure.core.http.rest.Response deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.WatchlistsDeleteResponse deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AutomationRuleRunPlaybookAction` was modified

* `withActionConfiguration(models.AutomationRuleRunPlaybookActionConfiguration)` was removed
* `models.AutomationRuleRunPlaybookActionConfiguration actionConfiguration()` -> `models.PlaybookActionProperties actionConfiguration()`

#### `models.EntityQueryTemplates` was modified

* `list(java.lang.String,java.lang.String,models.Constant69,com.azure.core.util.Context)` was removed

### Features Added

* `models.DeploymentInfo` was added

* `models.FusionSourceSettings` was added

* `models.DeploymentState` was added

* `models.SourceType` was added

* `models.ActionType` was added

* `models.IoTCheckRequirements` was added

* `models.DeploymentFetchStatus` was added

* `models.WatchlistsCreateOrUpdateResponse` was added

* `models.GitHubResourceInfo` was added

* `models.BookmarkEntityMappings` was added

* `models.PropertyConditionProperties` was added

* `models.FusionSubTypeSeverityFilter` was added

* `models.FusionScenarioExclusionPattern` was added

* `models.ConditionType` was added

* `models.Office365ProjectCheckRequirements` was added

* `models.ManualTriggerRequestBody` was added

* `models.PlaybookActionProperties` was added

* `models.IncidentPropertiesAction` was added

* `models.FusionTemplateSourceSubType` was added

* `models.FusionSubTypeSeverityFiltersItem` was added

* `models.Constant74` was added

* `models.Office365ProjectConnectorDataTypes` was added

* `models.FusionSourceSubTypeSetting` was added

* `models.ProvisioningState` was added

* `models.AlertRuleTemplateWithMitreProperties` was added

* `models.Office365ProjectDataConnector` was added

* `models.IoTDataConnector` was added

* `models.OfficePowerBIConnectorDataTypesLogs` was added

* `models.Deployment` was added

* `models.WatchlistsDeleteResponse` was added

* `models.Webhook` was added

* `models.DeploymentResult` was added

* `models.WatchlistsDeleteHeaders` was added

* `models.AzureDevOpsResourceInfo` was added

* `models.Version` was added

* `models.WatchlistsCreateOrUpdateHeaders` was added

* `models.Office365ProjectConnectorDataTypesLogs` was added

* `models.RepositoryResourceInfo` was added

* `models.OfficePowerBIDataConnector` was added

* `models.OfficePowerBIConnectorDataTypes` was added

* `models.OfficePowerBICheckRequirements` was added

* `models.FusionTemplateSubTypeSeverityFilter` was added

* `models.EntityFieldMapping` was added

* `models.FusionTemplateSourceSetting` was added

#### `models.Watchlist` was modified

* `provisioningState()` was added
* `sasUri()` was added
* `sourceType()` was added

#### `models.IncidentAdditionalData` was modified

* `techniques()` was added
* `providerIncidentUrl()` was added

#### `models.MLBehaviorAnalyticsAlertRuleTemplate` was modified

* `withTechniques(java.util.List)` was added
* `techniques()` was added

#### `models.ThreatIntelligenceAlertRuleTemplate` was modified

* `techniques()` was added
* `withTechniques(java.util.List)` was added

#### `models.ScheduledAlertRuleTemplate` was modified

* `withTechniques(java.util.List)` was added
* `techniques()` was added

#### `models.ThreatIntelligenceAlertRule` was modified

* `techniques()` was added

#### `models.NrtAlertRuleTemplate` was modified

* `withTechniques(java.util.List)` was added
* `techniques()` was added

#### `models.FusionAlertRuleTemplate` was modified

* `withTechniques(java.util.List)` was added
* `techniques()` was added
* `withSourceSettings(java.util.List)` was added
* `sourceSettings()` was added

#### `models.Watchlist$Update` was modified

* `withSasUri(java.lang.String)` was added
* `withSourceType(models.SourceType)` was added
* `withSource(java.lang.String)` was added

#### `models.Watchlist$Definition` was modified

* `withSasUri(java.lang.String)` was added
* `withSource(java.lang.String)` was added
* `withSourceType(models.SourceType)` was added

#### `models.Bookmark$Update` was modified

* `withTactics(java.util.List)` was added
* `withEntityMappings(java.util.List)` was added
* `withTechniques(java.util.List)` was added

#### `models.ThreatIntelligenceIndicators` was modified

* `replaceTags(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModel)` was added
* `createIndicatorWithResponse(java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModel,com.azure.core.util.Context)` was added
* `replaceTagsWithResponse(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModel,com.azure.core.util.Context)` was added
* `create(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModel)` was added
* `createIndicator(java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModel)` was added
* `createWithResponse(java.lang.String,java.lang.String,java.lang.String,models.ThreatIntelligenceIndicatorModel,com.azure.core.util.Context)` was added

#### `models.MetadataModel` was modified

* `threatAnalysisTactics()` was added
* `previewImagesDark()` was added
* `contentSchemaVersion()` was added
* `customVersion()` was added
* `icon()` was added
* `threatAnalysisTechniques()` was added
* `previewImages()` was added

#### `models.Bookmark` was modified

* `entityMappings()` was added
* `tactics()` was added
* `techniques()` was added

#### `models.WatchlistItems` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SourceControl` was modified

* `lastDeploymentInfo()` was added
* `version()` was added
* `repositoryResourceInfo()` was added

#### `models.MetadataModel$Update` was modified

* `withContentSchemaVersion(java.lang.String)` was added
* `withThreatAnalysisTechniques(java.util.List)` was added
* `withPreviewImages(java.util.List)` was added
* `withIcon(java.lang.String)` was added
* `withCustomVersion(java.lang.String)` was added
* `withPreviewImagesDark(java.util.List)` was added
* `withThreatAnalysisTactics(java.util.List)` was added

#### `models.SourceControl$Definition` was modified

* `withLastDeploymentInfo(models.DeploymentInfo)` was added
* `withVersion(models.Version)` was added
* `withRepositoryResourceInfo(models.RepositoryResourceInfo)` was added

#### `models.Incidents` was modified

* `runPlaybook(java.lang.String,java.lang.String,java.lang.String)` was added
* `runPlaybookWithResponse(java.lang.String,java.lang.String,java.lang.String,models.ManualTriggerRequestBody,com.azure.core.util.Context)` was added

#### `models.WatchlistItem$Definition` was modified

* `withItemsKeyValue(java.util.Map)` was added
* `withEntityMapping(java.util.Map)` was added

#### `models.AutomationRulePropertyValuesCondition` was modified

* `operator()` was added
* `propertyName()` was added
* `withPropertyValues(java.util.List)` was added
* `withPropertyName(models.AutomationRulePropertyConditionSupportedProperty)` was added
* `withOperator(models.AutomationRulePropertyConditionSupportedOperator)` was added
* `propertyValues()` was added

#### `models.ScheduledAlertRuleCommonProperties` was modified

* `alertDetailsOverride()` was added
* `severity()` was added
* `withEntityMappings(java.util.List)` was added
* `customDetails()` was added
* `entityMappings()` was added
* `withQuery(java.lang.String)` was added
* `withCustomDetails(java.util.Map)` was added
* `withSeverity(models.AlertSeverity)` was added
* `query()` was added
* `withAlertDetailsOverride(models.AlertDetailsOverride)` was added

#### `models.NrtAlertRule` was modified

* `severity()` was added
* `entityMappings()` was added
* `withSuppressionEnabled(java.lang.Boolean)` was added
* `description()` was added
* `templateVersion()` was added
* `withTemplateVersion(java.lang.String)` was added
* `lastModifiedUtc()` was added
* `suppressionEnabled()` was added
* `withDisplayName(java.lang.String)` was added
* `withEnabled(java.lang.Boolean)` was added
* `enabled()` was added
* `withSeverity(models.AlertSeverity)` was added
* `withSuppressionDuration(java.time.Duration)` was added
* `withEntityMappings(java.util.List)` was added
* `tactics()` was added
* `withIncidentConfiguration(models.IncidentConfiguration)` was added
* `withDescription(java.lang.String)` was added
* `displayName()` was added
* `withTechniques(java.util.List)` was added
* `query()` was added
* `suppressionDuration()` was added
* `customDetails()` was added
* `withQuery(java.lang.String)` was added
* `alertDetailsOverride()` was added
* `alertRuleTemplateName()` was added
* `techniques()` was added
* `withAlertRuleTemplateName(java.lang.String)` was added
* `withTactics(java.util.List)` was added
* `withCustomDetails(java.util.Map)` was added
* `withAlertDetailsOverride(models.AlertDetailsOverride)` was added
* `incidentConfiguration()` was added

#### `models.AutomationRulesList` was modified

* `withNextLink(java.lang.String)` was added

#### `models.ScheduledAlertRule` was modified

* `techniques()` was added
* `withTechniques(java.util.List)` was added

#### `models.WatchlistItem$Update` was modified

* `withEntityMapping(java.util.Map)` was added
* `withItemsKeyValue(java.util.Map)` was added

#### `models.Bookmark$Definition` was modified

* `withTactics(java.util.List)` was added
* `withTechniques(java.util.List)` was added
* `withEntityMappings(java.util.List)` was added

#### `models.AutomationRuleModifyPropertiesAction` was modified

* `withActionConfiguration(models.IncidentPropertiesAction)` was added

#### `models.MLBehaviorAnalyticsAlertRule` was modified

* `techniques()` was added

#### `models.Watchlists` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.FusionAlertRule` was modified

* `withSourceSettings(java.util.List)` was added
* `techniques()` was added
* `withScenarioExclusionPatterns(java.util.List)` was added
* `sourceSettings()` was added
* `scenarioExclusionPatterns()` was added

#### `models.MetadataPatch` was modified

* `customVersion()` was added
* `withThreatAnalysisTactics(java.util.List)` was added
* `threatAnalysisTechniques()` was added
* `withIcon(java.lang.String)` was added
* `withThreatAnalysisTechniques(java.util.List)` was added
* `withCustomVersion(java.lang.String)` was added
* `previewImages()` was added
* `contentSchemaVersion()` was added
* `icon()` was added
* `previewImagesDark()` was added
* `withContentSchemaVersion(java.lang.String)` was added
* `withPreviewImages(java.util.List)` was added
* `threatAnalysisTactics()` was added
* `withPreviewImagesDark(java.util.List)` was added

#### `models.AutomationRuleRunPlaybookAction` was modified

* `withActionConfiguration(models.PlaybookActionProperties)` was added

#### `models.MetadataModel$Definition` was modified

* `withContentSchemaVersion(java.lang.String)` was added
* `withPreviewImagesDark(java.util.List)` was added
* `withCustomVersion(java.lang.String)` was added
* `withThreatAnalysisTactics(java.util.List)` was added
* `withThreatAnalysisTechniques(java.util.List)` was added
* `withPreviewImages(java.util.List)` was added
* `withIcon(java.lang.String)` was added

#### `models.EntityQueryTemplates` was modified

* `list(java.lang.String,java.lang.String,models.Constant74,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2022-01-24)

- Azure Resource Manager SecurityInsights client library for Java. This package contains Microsoft Azure SDK for SecurityInsights Management SDK. API spec for Microsoft.SecurityInsights (Azure Security Insights) resource provider. Package tag package-preview-2021-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
