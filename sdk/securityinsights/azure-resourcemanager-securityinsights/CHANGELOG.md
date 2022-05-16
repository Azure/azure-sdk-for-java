# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
