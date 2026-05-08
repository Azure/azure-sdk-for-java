# Release History

## 1.1.0-beta.1 (2026-05-08)

- Azure Resource Manager Security Insights client library for Java. This package contains Microsoft Azure SDK for Security Insights Management SDK. API spec for Microsoft.SecurityInsights (Azure Security Insights) resource provider. Package api-version 2025-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AlertRuleTemplatesList` was removed

#### `models.ActionsList` was removed

#### `models.WatchlistList` was removed

#### `models.WatchlistItemList` was removed

#### `models.IncidentCommentList` was removed

#### `models.AlertRulesList` was removed

#### `models.RelationList` was removed

#### `models.IncidentList` was removed

#### `models.SecurityMLAnalyticsSettingsList` was removed

#### `models.Source` was removed

#### `models.OperationsList` was removed

#### `models.ThreatIntelligenceInformationList` was removed

#### `models.AutomationRulesList` was removed

#### `models.BookmarkList` was removed

#### `models.DataConnectorList` was removed

#### `models.Relation$DefinitionStages` was modified

* `withExistingIncident(java.lang.String,java.lang.String,java.lang.String)` was removed in stage 1

#### `models.AutomationRuleCondition` was modified

* `validate()` was removed

#### `models.IncidentAdditionalData` was modified

* `IncidentAdditionalData()` was changed to private access
* `validate()` was removed

#### `models.TIDataConnector` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.OfficeDataConnector` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.AzureResourceEntity` was modified

* `AzureResourceEntity()` was changed to private access
* `validate()` was removed

#### `models.ScheduledAlertRuleTemplate` was modified

* `ScheduledAlertRuleTemplate()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withTechniques(java.util.List)` was removed
* `withTriggerOperator(models.TriggerOperator)` was removed
* `withEntityMappings(java.util.List)` was removed
* `withCustomDetails(java.util.Map)` was removed
* `withAlertDetailsOverride(models.AlertDetailsOverride)` was removed
* `withQueryFrequency(java.time.Duration)` was removed
* `withEventGroupingSettings(models.EventGroupingSettings)` was removed
* `withSeverity(models.AlertSeverity)` was removed
* `withQueryPeriod(java.time.Duration)` was removed
* `withTriggerThreshold(java.lang.Integer)` was removed
* `validate()` was removed
* `withVersion(java.lang.String)` was removed
* `withStatus(models.TemplateStatus)` was removed
* `withTactics(java.util.List)` was removed
* `withQuery(java.lang.String)` was removed
* `withRequiredDataConnectors(java.util.List)` was removed
* `withDescription(java.lang.String)` was removed
* `withAlertRulesCreatedByTemplateCount(java.lang.Integer)` was removed

#### `models.ActionPropertiesBase` was modified

* `validate()` was removed

#### `models.PropertyArrayChangedConditionProperties` was modified

* `validate()` was removed

#### `models.FusionAlertRuleTemplate` was modified

* `FusionAlertRuleTemplate()` was changed to private access
* `withRequiredDataConnectors(java.util.List)` was removed
* `withTechniques(java.util.List)` was removed
* `withSeverity(models.AlertSeverity)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withTactics(java.util.List)` was removed
* `withAlertRulesCreatedByTemplateCount(java.lang.Integer)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withStatus(models.TemplateStatus)` was removed

#### `models.Watchlist$Update` was modified

* `withSource(models.Source)` was removed

#### `models.IoTDeviceEntity` was modified

* `IoTDeviceEntity()` was changed to private access
* `java.util.UUID iotSecurityAgentId()` -> `java.lang.String iotSecurityAgentId()`
* `validate()` was removed

#### `models.GeoLocation` was modified

* `GeoLocation()` was changed to private access
* `validate()` was removed

#### `models.Watchlist$Definition` was modified

* `withSource(models.Source)` was removed

#### `models.PropertyConditionProperties` was modified

* `validate()` was removed

#### `models.ClientInfo` was modified

* `java.util.UUID objectId()` -> `java.lang.String objectId()`
* `validate()` was removed
* `withObjectId(java.util.UUID)` was removed

#### `models.PlaybookActionProperties` was modified

* `withTenantId(java.util.UUID)` was removed
* `validate()` was removed
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.AccountEntity` was modified

* `AccountEntity()` was changed to private access
* `java.util.UUID objectGuid()` -> `java.lang.String objectGuid()`
* `validate()` was removed

#### `models.IncidentPropertiesAction` was modified

* `validate()` was removed

#### `models.EntityCommonProperties` was modified

* `validate()` was removed

#### `models.IncidentOwnerInfo` was modified

* `withObjectId(java.util.UUID)` was removed
* `java.util.UUID objectId()` -> `java.lang.String objectId()`
* `validate()` was removed

#### `models.RegistryKeyEntity` was modified

* `RegistryKeyEntity()` was changed to private access
* `validate()` was removed

#### `models.AwsCloudTrailDataConnector` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AutomationRulePropertyValuesCondition` was modified

* `validate()` was removed

#### `models.AutomationRuleTriggeringLogic` was modified

* `validate()` was removed

#### `models.Relation$Definition` was modified

* `withExistingIncident(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.GroupingConfiguration` was modified

* `validate()` was removed

#### `models.ScheduledAlertRuleCommonProperties` was modified

* `validate()` was removed

#### `models.ThreatIntelligenceMetric` was modified

* `ThreatIntelligenceMetric()` was changed to private access
* `validate()` was removed
* `withThreatTypeMetrics(java.util.List)` was removed
* `withPatternTypeMetrics(java.util.List)` was removed
* `withSourceMetrics(java.util.List)` was removed
* `withLastUpdatedTimeUtc(java.lang.String)` was removed

#### `models.OfficeDataConnectorDataTypes` was modified

* `validate()` was removed

#### `models.ScheduledAlertRule` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PropertyChangedConditionProperties` was modified

* `validate()` was removed

#### `models.FileHashEntity` was modified

* `FileHashEntity()` was changed to private access
* `validate()` was removed

#### `models.AlertDetailsOverride` was modified

* `validate()` was removed

#### `models.AlertRuleTemplateDataSource` was modified

* `AlertRuleTemplateDataSource()` was changed to private access
* `withConnectorId(java.lang.String)` was removed
* `withDataTypes(java.util.List)` was removed
* `validate()` was removed

#### `models.EventGroupingSettings` was modified

* `validate()` was removed

#### `models.MdatpDataConnector` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `validate()` was removed

#### `models.MicrosoftSecurityIncidentCreationAlertRule` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.Entity` was modified

* `validate()` was removed

#### `models.OfficeDataConnectorDataTypesTeams` was modified

* `validate()` was removed

#### `models.OfficeDataConnectorDataTypesExchange` was modified

* `validate()` was removed

#### `models.IncidentLabel` was modified

* `validate()` was removed

#### `models.SubmissionMailEntity` was modified

* `SubmissionMailEntity()` was changed to private access
* `validate()` was removed
* `java.util.UUID submissionId()` -> `java.lang.String submissionId()`
* `java.util.UUID networkMessageId()` -> `java.lang.String networkMessageId()`

#### `models.AadDataConnector` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ResourceWithEtag` was modified

* `validate()` was removed

#### `models.DataConnectorWithAlertsProperties` was modified

* `validate()` was removed

#### `models.MalwareEntity` was modified

* `MalwareEntity()` was changed to private access
* `validate()` was removed

#### `models.Watchlist` was modified

* `models.Source source()` -> `java.lang.String source()`

#### `models.ThreatIntelligenceSortingCriteria` was modified

* `validate()` was removed

#### `models.SecurityAlert` was modified

* `SecurityAlert()` was changed to private access
* `validate()` was removed
* `withSeverity(models.AlertSeverity)` was removed

#### `models.CloudApplicationEntity` was modified

* `CloudApplicationEntity()` was changed to private access
* `validate()` was removed

#### `models.AscDataConnector` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ThreatIntelligenceGranularMarkingModel` was modified

* `validate()` was removed

#### `models.McasDataConnector` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.IncidentConfiguration` was modified

* `validate()` was removed

#### `models.ThreatIntelligenceKillChainPhase` was modified

* `validate()` was removed

#### `models.SecurityMLAnalyticsSettingsDataSource` was modified

* `validate()` was removed

#### `models.ThreatIntelligenceMetricEntity` was modified

* `ThreatIntelligenceMetricEntity()` was changed to private access
* `withMetricName(java.lang.String)` was removed
* `validate()` was removed
* `withMetricValue(java.lang.Integer)` was removed

#### `models.MailboxEntity` was modified

* `MailboxEntity()` was changed to private access
* `java.util.UUID externalDirectoryObjectId()` -> `java.lang.String externalDirectoryObjectId()`
* `validate()` was removed

#### `SecurityInsightsManager` was modified

* `fluent.SecurityInsights serviceClient()` -> `fluent.SecurityInsightsManagementClient serviceClient()`

#### `models.HostEntity` was modified

* `HostEntity()` was changed to private access
* `validate()` was removed
* `withOsFamily(models.OSFamily)` was removed

#### `models.AlertsDataTypeOfDataConnector` was modified

* `validate()` was removed

#### `models.RegistryValueEntity` was modified

* `RegistryValueEntity()` was changed to private access
* `validate()` was removed

#### `models.AwsCloudTrailDataConnectorDataTypesLogs` was modified

* `validate()` was removed

#### `models.ThreatIntelligenceExternalReference` was modified

* `validate()` was removed

#### `models.AutomationRuleAction` was modified

* `validate()` was removed

#### `models.AutomationRulePropertyValuesChangedCondition` was modified

* `validate()` was removed

#### `models.ThreatIntelligenceFilteringCriteria` was modified

* `validate()` was removed

#### `models.ThreatIntelligenceMetrics` was modified

* `ThreatIntelligenceMetrics()` was changed to private access
* `withProperties(models.ThreatIntelligenceMetric)` was removed
* `validate()` was removed

#### `models.SecurityGroupEntity` was modified

* `SecurityGroupEntity()` was changed to private access
* `validate()` was removed
* `java.util.UUID objectGuid()` -> `java.lang.String objectGuid()`

#### `models.ActionRequest` was modified

* `validate()` was removed

#### `models.McasDataConnectorDataTypes` was modified

* `validate()` was removed

#### `models.AatpDataConnector` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AnomalySecurityMLAnalyticsSettings` was modified

* `withSettingsDefinitionId(java.util.UUID)` was removed
* `java.util.UUID settingsDefinitionId()` -> `java.lang.String settingsDefinitionId()`
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AwsCloudTrailDataConnectorDataTypes` was modified

* `validate()` was removed

#### `models.MicrosoftSecurityIncidentCreationAlertRuleCommonProperties` was modified

* `validate()` was removed

#### `models.ThreatIntelligenceIndicatorModel` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.EntityMapping` was modified

* `validate()` was removed

#### `models.SecurityAlertPropertiesConfidenceReasonsItem` was modified

* `SecurityAlertPropertiesConfidenceReasonsItem()` was changed to private access
* `validate()` was removed

#### `models.MicrosoftSecurityIncidentCreationAlertRuleTemplate` was modified

* `MicrosoftSecurityIncidentCreationAlertRuleTemplate()` was changed to private access
* `validate()` was removed
* `withDisplayNamesFilter(java.util.List)` was removed
* `withSeveritiesFilter(java.util.List)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withDisplayNamesExcludeFilter(java.util.List)` was removed
* `withDescription(java.lang.String)` was removed
* `withProductFilter(models.MicrosoftSecurityProductName)` was removed
* `withStatus(models.TemplateStatus)` was removed
* `withAlertRulesCreatedByTemplateCount(java.lang.Integer)` was removed
* `withRequiredDataConnectors(java.util.List)` was removed

#### `models.ThreatIntelligenceParsedPatternTypeValue` was modified

* `validate()` was removed

#### `models.AutomationRuleModifyPropertiesAction` was modified

* `validate()` was removed

#### `models.AutomationRulePropertyArrayChangedValuesCondition` was modified

* `validate()` was removed

#### `models.DnsEntity` was modified

* `DnsEntity()` was changed to private access
* `validate()` was removed

#### `models.Watchlists` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.FileEntity` was modified

* `FileEntity()` was changed to private access
* `validate()` was removed

#### `models.HuntingBookmark` was modified

* `HuntingBookmark()` was changed to private access
* `withEventTime(java.time.OffsetDateTime)` was removed
* `withQuery(java.lang.String)` was removed
* `withIncidentInfo(models.IncidentInfo)` was removed
* `withQueryResult(java.lang.String)` was removed
* `validate()` was removed
* `withNotes(java.lang.String)` was removed
* `withCreatedBy(models.UserInfo)` was removed
* `withCreated(java.time.OffsetDateTime)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withLabels(java.util.List)` was removed
* `withUpdatedBy(models.UserInfo)` was removed
* `withUpdated(java.time.OffsetDateTime)` was removed

#### `models.ThreatIntelligenceAppendTags` was modified

* `validate()` was removed

#### `models.MailClusterEntity` was modified

* `MailClusterEntity()` was changed to private access
* `validate()` was removed

#### `models.IncidentEntitiesResultsMetadata` was modified

* `IncidentEntitiesResultsMetadata()` was changed to private access
* `withEntityKind(models.EntityKindEnum)` was removed
* `validate()` was removed
* `withCount(int)` was removed

#### `models.FusionAlertRule` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.IncidentRelations` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `define(java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed

#### `models.TIDataConnectorDataTypes` was modified

* `validate()` was removed

#### `models.DataConnectorDataTypeCommon` was modified

* `validate()` was removed

#### `models.AutomationRuleRunPlaybookAction` was modified

* `validate()` was removed

#### `models.UserInfo` was modified

* `withObjectId(java.util.UUID)` was removed
* `java.util.UUID objectId()` -> `java.lang.String objectId()`
* `validate()` was removed

#### `models.MailMessageEntity` was modified

* `MailMessageEntity()` was changed to private access
* `withBodyFingerprintBin1(java.lang.Integer)` was removed
* `java.util.UUID networkMessageId()` -> `java.lang.String networkMessageId()`
* `validate()` was removed
* `withBodyFingerprintBin2(java.lang.Integer)` was removed
* `withAntispamDirection(models.AntispamMailDirection)` was removed
* `withBodyFingerprintBin3(java.lang.Integer)` was removed
* `withBodyFingerprintBin5(java.lang.Integer)` was removed
* `withBodyFingerprintBin4(java.lang.Integer)` was removed
* `withDeliveryAction(models.DeliveryAction)` was removed
* `withDeliveryLocation(models.DeliveryLocation)` was removed

#### `models.ThreatIntelligenceParsedPattern` was modified

* `validate()` was removed

#### `models.UrlEntity` was modified

* `UrlEntity()` was changed to private access
* `validate()` was removed

#### `models.FieldMapping` was modified

* `validate()` was removed

#### `models.ThreatIntelligence` was modified

* `ThreatIntelligence()` was changed to private access
* `validate()` was removed

#### `models.OfficeDataConnectorDataTypesSharePoint` was modified

* `validate()` was removed

#### `models.IpEntity` was modified

* `IpEntity()` was changed to private access
* `validate()` was removed

#### `models.ProcessEntity` was modified

* `ProcessEntity()` was changed to private access
* `validate()` was removed
* `withElevationToken(models.ElevationToken)` was removed

#### `models.IncidentInfo` was modified

* `validate()` was removed

#### `models.TIDataConnectorDataTypesIndicators` was modified

* `validate()` was removed

### Features Added

* `models.InsightQueryItemPropertiesReferenceTimeRange` was added

* `models.FusionSourceSettings` was added

* `models.AwsS3CheckRequirements` was added

* `models.AATPCheckRequirements` was added

* `models.ExpansionResultsMetadata` was added

* `models.Metadatas` was added

* `models.DeploymentState` was added

* `models.ExpansionEntityQuery` was added

* `models.WorkspaceManagerGroups` was added

* `models.ConnectorDefinitionsAvailability` was added

* `models.EnrichmentDomainWhoisContact` was added

* `models.EntityQueries` was added

* `models.SourceKind` was added

* `models.ThreatIntelligenceAlertRuleTemplate` was added

* `models.EntityQueryItemProperties` was added

* `models.ThreatIntelligenceAlertRule` was added

* `models.Flag` was added

* `models.Anomalies` was added

* `models.IoTCheckRequirements` was added

* `models.Hunt$Update` was added

* `models.TimelineError` was added

* `models.EntityTimelineParameters` was added

* `models.DeploymentFetchStatus` was added

* `models.AlertPropertyMapping` was added

* `models.NicEntity` was added

* `models.TiTaxiiCheckRequirements` was added

* `models.WorkspaceManagerMember` was added

* `models.Hunt$DefinitionStages` was added

* `models.CodelessUiConnectorConfigPropertiesConnectivityCriteriaItem` was added

* `models.HuntRelation$Update` was added

* `models.ThreatIntelligences` was added

* `models.GitHubResourceInfo` was added

* `models.ApiKeyAuthModel` was added

* `models.MetadataDependencies` was added

* `models.RestApiPollerDataConnector` was added

* `models.ActivityEntityQueriesPropertiesQueryDefinitions` was added

* `models.IncidentTask$DefinitionStages` was added

* `models.BookmarkEntityMappings` was added

* `models.ResourceProviders` was added

* `models.EntityGetInsightsResponse` was added

* `models.RelationshipHint` was added

* `models.TimelineAggregation` was added

* `models.EyesOn` was added

* `models.PropertyArrayConditionProperties` was added

* `models.ConnectorDefinitionsPermissions` was added

* `models.FusionSubTypeSeverityFilter` was added

* `models.Error` was added

* `models.AutomationRulePropertyArrayConditionSupportedArrayConditionType` was added

* `models.ProductPackagesOperations` was added

* `models.ContentType` was added

* `models.DeviceImportance` was added

* `models.FusionScenarioExclusionPattern` was added

* `models.OfficeIRMDataConnector` was added

* `models.PackageModel$DefinitionStages` was added

* `models.Office365ProjectCheckRequirements` was added

* `models.EntityExpandParameters` was added

* `models.MDATPCheckRequirements` was added

* `models.SessionAuthModel` was added

* `models.DataConnectorAuthorizationState` was added

* `models.PurviewAuditCheckRequirements` was added

* `models.IndicatorObservablesItem` was added

* `models.AlertProperty` was added

* `models.QuerySortBy` was added

* `models.WorkspaceManagerAssignments` was added

* `models.InsightsTableResultColumnsItem` was added

* `models.MTPDataConnector` was added

* `models.SourceControl` was added

* `models.ContentPackages` was added

* `models.EntitiesGetTimelines` was added

* `models.TiTaxiiDataConnectorDataTypesTaxiiClient` was added

* `models.ActivityEntityQueryTemplatePropertiesQueryDefinitions` was added

* `models.ConnectedEntity` was added

* `models.ConditionProperties` was added

* `models.PermissionsCustomsItem` was added

* `models.DataConnectorConnectBody` was added

* `models.RestApiPollerRequestConfig` was added

* `models.AutomationRuleAddIncidentTaskAction` was added

* `models.GitHubAuthModel` was added

* `models.FusionTemplateSourceSubType` was added

* `models.ConnectorInstructionModelBase` was added

* `models.WorkspaceManagerMember$Definition` was added

* `models.HuntComment$Definition` was added

* `models.ConnectivityCriteria` was added

* `models.RepositoryAccessKind` was added

* `models.WorkspaceManagerGroup$UpdateStages` was added

* `models.MSTIDataConnectorDataTypes` was added

* `models.EntityInsightItem` was added

* `models.MetadataAuthor` was added

* `models.CcpAuthConfig` was added

* `models.FusionSubTypeSeverityFiltersItem` was added

* `models.CustomPermissionDetails` was added

* `models.CodelessConnectorPollingResponseProperties` was added

* `models.RecommendationPatchProperties` was added

* `models.AwsS3DataConnectorDataTypesLogs` was added

* `models.EntityItemQueryKind` was added

* `models.QueryCondition` was added

* `models.EntityType` was added

* `models.DataConnectorRequirementsState` was added

* `models.TemplateModel$Definition` was added

* `models.HuntComment` was added

* `models.WorkspaceManagerMember$UpdateStages` was added

* `models.EntityTimelineResponse` was added

* `models.AutomationRulePropertyArrayValuesCondition` was added

* `models.AlertRuleTemplatePropertiesBase` was added

* `models.GCPAuthProperties` was added

* `models.GetInsightsErrorKind` was added

* `models.WorkspaceManagerGroup$Definition` was added

* `models.ProductTemplatesOperations` was added

* `models.WorkspaceManagerAssignment$UpdateStages` was added

* `models.EntityInsightItemQueryTimeInterval` was added

* `models.Connective` was added

* `models.OfficeATPDataConnector` was added

* `models.TIObjectKind` was added

* `models.TemplateModel$UpdateStages` was added

* `models.ProductTemplates` was added

* `models.HuntComment$UpdateStages` was added

* `models.ProvisioningState` was added

* `models.HuntComment$Update` was added

* `models.SortingDirection` was added

* `models.BookmarkExpandResponse` was added

* `models.AlertRuleTemplateWithMitreProperties` was added

* `models.CustomEntityQueryKind` was added

* `models.Warning` was added

* `models.ResourceProvider` was added

* `models.MicrosoftPurviewInformationProtectionDataConnector` was added

* `models.MicrosoftPurviewInformationProtectionConnectorDataTypesLogs` was added

* `models.Indicator` was added

* `models.MetadataModel$UpdateStages` was added

* `models.PackageModel` was added

* `models.MetadataSupport` was added

* `models.PackageModel$Update` was added

* `models.PurviewAuditConnectorDataTypesLogs` was added

* `models.WorkspaceManagerConfiguration$Definition` was added

* `models.ExpansionResultAggregation` was added

* `models.WorkspaceManagerAssignment` was added

* `models.ProviderPermissionsScope` was added

* `models.BookmarkOperations` was added

* `models.AssignmentItem` was added

* `models.NrtAlertRule` was added

* `models.Query` was added

* `models.WorkspaceManagerConfiguration$Update` was added

* `models.ActivityTimelineItem` was added

* `models.TriggeredAnalyticsRuleRun` was added

* `models.CustomsPermission` was added

* `models.RecommendationPatch` was added

* `models.CodelessUiConnectorConfigPropertiesSampleQueriesItem` was added

* `models.Dynamics365DataConnectorDataTypes` was added

* `models.ConnectorDataType` was added

* `models.NoneAuthModel` was added

* `models.WorkspaceManagerGroup$Update` was added

* `models.Mode` was added

* `models.MetadataSource` was added

* `models.TemplateModel$Update` was added

* `models.RequiredPermissions` was added

* `models.Deployment` was added

* `models.Hunt$Definition` was added

* `models.MtpProvider` was added

* `models.BillingStatisticKind` was added

* `models.MicrosoftPurviewInformationProtectionConnectorDataTypes` was added

* `models.ContentTemplates` was added

* `models.AddIncidentTaskActionProperties` was added

* `models.ReevaluateResponse` was added

* `models.CodelessConnectorPollingPagingProperties` was added

* `models.Recommendation` was added

* `models.InsightQueryItem` was added

* `models.SentinelEntityMapping` was added

* `models.TiTaxiiDataConnector` was added

* `models.Hunt$UpdateStages` was added

* `models.MetadataModel$DefinitionStages` was added

* `models.TimelineResultsMetadata` was added

* `models.AwsCloudTrailCheckRequirements` was added

* `models.EntityQuery` was added

* `models.PackageModel$UpdateStages` was added

* `models.SecurityAlertTimelineItem` was added

* `models.AwsS3DataConnector` was added

* `models.MLBehaviorAnalyticsAlertRule` was added

* `models.PackageKind` was added

* `models.Permissions` was added

* `models.CodelessUiDataConnector` was added

* `models.AzureDevOpsResourceInfo` was added

* `models.CustomEntityQuery` was added

* `models.SupportTier` was added

* `models.MSTIDataConnector` was added

* `models.OfficeATPCheckRequirements` was added

* `models.BillingStatistics` was added

* `models.Version` was added

* `models.LastDataReceivedDataType` was added

* `models.WorkspaceManagerMember$Update` was added

* `models.MetadataPatch` was added

* `models.GetTriggeredAnalyticsRuleRuns` was added

* `models.ActivityEntityQueryTemplate` was added

* `models.Office365ProjectConnectorDataTypesLogs` was added

* `models.RepositoryResourceInfo` was added

* `models.ValidationError` was added

* `models.CodelessUiConnectorConfigPropertiesGraphQueriesItem` was added

* `models.MSTICheckRequirements` was added

* `models.RepoType` was added

* `models.TemplateModel$DefinitionStages` was added

* `models.OfficePowerBIConnectorDataTypes` was added

* `models.OutputType` was added

* `models.ConnectivityCriterion` was added

* `models.SettingKind` was added

* `models.OfficePowerBICheckRequirements` was added

* `models.WarningCode` was added

* `models.Dynamics365CheckRequirements` was added

* `models.SourceControl$DefinitionStages` was added

* `models.CodelessUiConnectorConfigProperties` was added

* `models.PollingFrequency` was added

* `models.GraphQuery` was added

* `models.Repository` was added

* `models.EntityQueryTemplate` was added

* `models.WorkspaceManagerAssignment$DefinitionStages` was added

* `models.InstructionStepsInstructionsItem` was added

* `models.EntityEdges` was added

* `models.EntityFieldMapping` was added

* `models.InsightQueryItemPropertiesTableQueryColumnsDefinitionsItem` was added

* `models.WorkloadIdentityFederation` was added

* `models.MetadataModel$Definition` was added

* `models.WorkspaceManagerConfiguration` was added

* `models.DeleteStatus` was added

* `models.PremiumMdtiDataConnectorDataTypesConnector` was added

* `models.IncidentTask$Update` was added

* `models.AutomationRuleBooleanCondition` was added

* `models.DataConnectorsCheckRequirements` was added

* `models.CodelessConnectorPollingConfigProperties` was added

* `models.ActivityEntityQuery` was added

* `models.DeploymentInfo` was added

* `models.InsightQueryItemPropertiesTableQuery` was added

* `models.AutomationRulePropertyArrayConditionSupportedArrayType` was added

* `models.InsightQueryItemPropertiesDefaultTimeRange` was added

* `models.Updates` was added

* `models.InsightQueryItemProperties` was added

* `models.OfficeIRMCheckRequirements` was added

* `models.AlertRuleOperations` was added

* `models.RestApiPollerRequestPagingKind` was added

* `models.EntityAnalytics` was added

* `models.ProductPackageModel` was added

* `models.IncidentTaskStatus` was added

* `models.CodelessUiConnectorConfigPropertiesDataTypesItem` was added

* `models.EntityExpandResponse` was added

* `models.IncidentTask$Definition` was added

* `models.SourceType` was added

* `models.MLBehaviorAnalyticsAlertRuleTemplate` was added

* `models.HuntComment$DefinitionStages` was added

* `models.Relationship` was added

* `models.TIObject` was added

* `models.GetRecommendations` was added

* `models.MicrosoftPurviewInformationProtectionCheckRequirements` was added

* `models.CustomizableConnectorDefinition` was added

* `models.BasicAuthModel` was added

* `models.HttpMethodVerb` was added

* `models.Repo` was added

* `models.EntityGetInsightsParameters` was added

* `models.CustomizableConnectionsConfig` was added

* `models.TriggeredAnalyticsRuleRunOperations` was added

* `models.IncidentTask$UpdateStages` was added

* `models.EntityTimelineKind` was added

* `models.NrtAlertRuleTemplate` was added

* `models.AwsS3DataConnectorDataTypes` was added

* `models.Hunt` was added

* `models.ProviderName` was added

* `models.PremiumMdtiDataConnectorDataTypes` was added

* `models.PermissionsResourceProviderItem` was added

* `models.PackageBaseProperties` was added

* `models.BookmarkTimelineItem` was added

* `models.SourceControls` was added

* `models.ServicePrincipal` was added

* `models.PullRequest` was added

* `models.DataConnectorDefinitions` was added

* `models.PackageModel$Definition` was added

* `models.Kind` was added

* `models.HuntRelation$DefinitionStages` was added

* `models.InstructionSteps` was added

* `models.IncidentTasks` was added

* `models.IncidentTask` was added

* `models.IngestionMode` was added

* `models.CodelessConnectorPollingAuthProperties` was added

* `models.BookmarkExpandResponseValue` was added

* `models.DataTypeDefinitions` was added

* `models.FileImport$DefinitionStages` was added

* `models.GetInsightsError` was added

* `models.EntityQueryKind` was added

* `models.GetInsightsResultsMetadata` was added

* `models.Operator` was added

* `models.AWSAuthModel` was added

* `models.MtpCheckRequirements` was added

* `models.MetadataModel` was added

* `models.Gets` was added

* `models.HuntOwner` was added

* `models.EntityTimelineItem` was added

* `models.ManualTriggerRequestBody` was added

* `models.AnomalyTimelineItem` was added

* `models.DCRConfiguration` was added

* `models.MtpFilteredProviders` was added

* `models.HypothesisStatus` was added

* `models.ContentTemplatesOperations` was added

* `models.Entities` was added

* `models.ConnectivityType` was added

* `models.JwtAuthModel` was added

* `models.DataConnectorTenantId` was added

* `models.EnrichmentDomainBody` was added

* `models.SourceControlOperations` was added

* `models.ProductTemplateModel` was added

* `models.PermissionProviderScope` was added

* `models.PurviewAuditDataConnector` was added

* `models.OracleAuthModel` was added

* `models.MetadataModel$Update` was added

* `models.InsightQueryItemPropertiesAdditionalQuery` was added

* `models.Job` was added

* `models.EntityManualTriggerRequestBody` was added

* `models.SourceControl$Definition` was added

* `models.DataConnectorLicenseState` was added

* `models.EntityQueryItem` was added

* `models.Hunts` was added

* `models.ThreatActor` was added

* `models.MCASCheckRequirements` was added

* `models.ProductPackages` was added

* `models.ResourceProviderRequiredPermissions` was added

* `models.SapSolutionUsageStatistic` was added

* `models.InsightQueryItemPropertiesTableQueryQueriesDefinitionsItem` was added

* `models.CustomizableConnectorUiConfig` was added

* `models.WorkspaceManagerConfiguration$UpdateStages` was added

* `models.Settings` was added

* `models.Dynamics365DataConnectorDataTypesDynamics365CdsActivities` was added

* `models.EntityExpandResponseValue` was added

* `models.MetadataCategories` was added

* `models.EntityProviders` was added

* `models.HuntRelations` was added

* `models.ConnectorDefinitionsResourceProvider` was added

* `models.EntitiesRelations` was added

* `models.CodelessConnectorPollingRequestProperties` was added

* `models.FileImportState` was added

* `models.GCPAuthModel` was added

* `models.ConnectAuthKind` was added

* `models.PurviewAuditConnectorDataTypes` was added

* `models.EntityQueryTemplateKind` was added

* `models.ThreatIntelligenceCount` was added

* `models.Office365ProjectConnectorDataTypes` was added

* `models.FileMetadata` was added

* `models.EnrichmentIpAddressBody` was added

* `models.FileFormat` was added

* `models.CcpAuthType` was added

* `models.CcpResponseConfig` was added

* `models.FusionSourceSubTypeSetting` was added

* `models.DataConnectorDefinition` was added

* `models.EnrichmentDomainWhoisContacts` was added

* `models.FileImport` was added

* `models.EntityRelations` was added

* `models.FileImportContentType` was added

* `models.AnalyticsRuleRunTrigger` was added

* `models.ContentPackageOperations` was added

* `models.WarningBody` was added

* `models.Office365ProjectDataConnector` was added

* `models.RepositoryAccessProperties` was added

* `models.EnrichmentDomainWhoisDetails` was added

* `models.EnrichmentIpGeodata` was added

* `models.TICheckRequirements` was added

* `models.WorkspaceManagerMembers` was added

* `models.ProductSettings` was added

* `models.HuntComments` was added

* `models.EntityQueryItemPropertiesDataTypesItem` was added

* `models.AttackPattern` was added

* `models.IoTDataConnector` was added

* `models.WorkspaceManagerConfiguration$DefinitionStages` was added

* `models.State` was added

* `models.TeamInformation` was added

* `models.OfficePowerBIConnectorDataTypesLogs` was added

* `models.TiType` was added

* `models.Reevaluates` was added

* `models.WorkspaceManagerGroup` was added

* `models.FileImports` was added

* `models.TiTaxiiDataConnectorDataTypes` was added

* `models.MTPDataConnectorDataTypesAlerts` was added

* `models.DataConnectorDefinitionKind` was added

* `models.TemplateModel` was added

* `models.ConditionClause` was added

* `models.Identity` was added

* `models.WorkspaceManagerAssignment$Update` was added

* `models.MTPDataConnectorDataTypesIncidents` was added

* `models.CodelessUiConnectorConfigPropertiesInstructionStepsItem` was added

* `models.CodelessApiPollingDataConnector` was added

* `models.ASCCheckRequirements` was added

* `models.Webhook` was added

* `models.WorkspaceManagerConfigurations` was added

* `models.InstructionStep` was added

* `models.PremiumMicrosoftDefenderForThreatIntelligence` was added

* `models.DeploymentResult` was added

* `models.OfficeConsent` was added

* `models.BooleanConditionProperties` was added

* `models.AvailabilityStatus` was added

* `models.ActivityCustomEntityQuery` was added

* `models.CountQuery` was added

* `models.RestApiPollerRequestPagingConfig` was added

* `models.HuntRelation` was added

* `models.WorkspaceManagerAssignment$Definition` was added

* `models.WorkspaceManagerAssignmentJobs` was added

* `models.SettingType` was added

* `models.InsightsTableResult` was added

* `models.GraphQueries` was added

* `models.Status` was added

* `models.OfficeConsents` was added

* `models.DataConnectorsCheckRequirementsOperations` was added

* `models.EnrichmentDomainWhois` was added

* `models.Dynamics365DataConnector` was added

* `models.BillingStatistic` was added

* `models.GCPRequestProperties` was added

* `models.OfficePowerBIDataConnector` was added

* `models.EnrichmentDomainWhoisRegistrarDetails` was added

* `models.OAuthModel` was added

* `models.BookmarkExpandParameters` was added

* `models.FileImport$Definition` was added

* `models.BookmarkRelations` was added

* `models.GCPDataConnector` was added

* `models.JobItem` was added

* `models.WorkspaceManagerMember$DefinitionStages` was added

* `models.EnrichmentType` was added

* `models.AutomationRuleBooleanConditionSupportedOperator` was added

* `models.SampleQueries` was added

* `models.UebaDataSources` was added

* `models.HuntRelation$UpdateStages` was added

* `models.FusionTemplateSubTypeSeverityFilter` was added

* `models.RecommendedSuggestion` was added

* `models.InsightQueryItemPropertiesTableQueryQueriesDefinitionsPropertiesItemsItem` was added

* `models.AADCheckRequirements` was added

* `models.HuntRelation$Definition` was added

* `models.Customs` was added

* `models.Ueba` was added

* `models.MTPDataConnectorDataTypes` was added

* `models.WorkspaceManagerGroup$DefinitionStages` was added

* `models.GenericBlobSbsAuthModel` was added

* `models.Availability` was added

* `models.MstiDataConnectorDataTypesMicrosoftEmergingThreatFeed` was added

* `models.EntityQueryTemplates` was added

* `models.FusionTemplateSourceSetting` was added

* `models.InstructionStepDetails` was added

#### `models.IncidentAdditionalData` was modified

* `techniques()` was added
* `mergedIncidentNumber()` was added
* `mergedIncidentUrl()` was added
* `providerIncidentUrl()` was added

#### `models.ScheduledAlertRuleTemplate` was modified

* `sentinelEntitiesMappings()` was added
* `subTechniques()` was added

#### `models.FusionAlertRuleTemplate` was modified

* `subTechniques()` was added
* `sourceSettings()` was added

#### `models.Watchlist$Update` was modified

* `withSource(java.lang.String)` was added
* `withSourceType(models.SourceType)` was added

#### `models.IoTDeviceEntity` was modified

* `deviceSubType()` was added
* `sensor()` was added
* `purdueLayer()` was added
* `zone()` was added
* `owners()` was added
* `importance()` was added
* `isAuthorized()` was added
* `nicEntityIds()` was added
* `isProgramming()` was added
* `site()` was added
* `isScanner()` was added

#### `models.Watchlist$Definition` was modified

* `withSourceType(models.SourceType)` was added
* `withSource(java.lang.String)` was added

#### `models.Bookmark$Update` was modified

* `withEntityMappings(java.util.List)` was added
* `withTactics(java.util.List)` was added
* `withTechniques(java.util.List)` was added

#### `models.ConditionType` was modified

* `PROPERTY_ARRAY` was added
* `BOOLEAN` was added

#### `models.ClientInfo` was modified

* `withObjectId(java.lang.String)` was added

#### `models.PlaybookActionProperties` was modified

* `withTenantId(java.lang.String)` was added

#### `models.MicrosoftSecurityProductName` was modified

* `OFFICE_365_ADVANCED_THREAT_PROTECTION` was added
* `MICROSOFT_DEFENDER_ADVANCED_THREAT_PROTECTION` was added

#### `models.IncidentOwnerInfo` was modified

* `withObjectId(java.lang.String)` was added

#### `models.AutomationRulePropertyConditionSupportedProperty` was modified

* `INCIDENT_CUSTOM_DETAILS_KEY` was added
* `INCIDENT_CUSTOM_DETAILS_VALUE` was added
* `INCIDENT_ALERT_TITLE` was added
* `INCIDENT_CUSTOM_DETECTION_RULE_IDS` was added

#### `models.DataConnectorKind` was modified

* `MICROSOFT_THREAT_PROTECTION` was added
* `GENERIC_UI` was added
* `OFFICE_ATP` was added
* `PREMIUM_MICROSOFT_DEFENDER_FOR_THREAT_INTELLIGENCE` was added
* `MICROSOFT_THREAT_INTELLIGENCE` was added
* `AMAZON_WEB_SERVICES_S3` was added
* `OFFICE_IRM` was added
* `MICROSOFT_PURVIEW_INFORMATION_PROTECTION` was added
* `DYNAMICS365` was added
* `PURVIEW_AUDIT` was added
* `IOT` was added
* `OFFICE365PROJECT` was added
* `OFFICE_POWER_BI` was added
* `GCP` was added
* `APIPOLLING` was added
* `REST_API_POLLER` was added
* `THREAT_INTELLIGENCE_TAXII` was added

#### `models.Relation$Definition` was modified

* `withExistingBookmark(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.Incident$Definition` was modified

* `withTeamInformation(models.TeamInformation)` was added

#### `models.ScheduledAlertRuleCommonProperties` was modified

* `withSentinelEntitiesMappings(java.util.List)` was added
* `sentinelEntitiesMappings()` was added

#### `models.ScheduledAlertRule` was modified

* `withSentinelEntitiesMappings(java.util.List)` was added
* `sentinelEntitiesMappings()` was added
* `subTechniques()` was added
* `withSubTechniques(java.util.List)` was added

#### `models.Bookmark$Definition` was modified

* `withTactics(java.util.List)` was added
* `withTechniques(java.util.List)` was added
* `withEntityMappings(java.util.List)` was added

#### `models.AlertDetailsOverride` was modified

* `withAlertDynamicProperties(java.util.List)` was added
* `alertDynamicProperties()` was added

#### `models.Watchlist` was modified

* `provisioningState()` was added
* `sourceType()` was added

#### `models.ActionType` was modified

* `ADD_INCIDENT_TASK` was added

#### `models.Bookmark` was modified

* `techniques()` was added
* `tactics()` was added
* `entityMappings()` was added

#### `models.AlertRuleKind` was modified

* `THREAT_INTELLIGENCE` was added
* `NRT` was added
* `MLBEHAVIOR_ANALYTICS` was added

#### `models.DataConnectors` was modified

* `disconnect(java.lang.String,java.lang.String,java.lang.String)` was added
* `connect(java.lang.String,java.lang.String,java.lang.String,models.DataConnectorConnectBody)` was added
* `connectWithResponse(java.lang.String,java.lang.String,java.lang.String,models.DataConnectorConnectBody,com.azure.core.util.Context)` was added
* `disconnectWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Incidents` was modified

* `runPlaybook(java.lang.String,java.lang.String,java.lang.String)` was added
* `runPlaybookWithResponse(java.lang.String,java.lang.String,java.lang.String,models.ManualTriggerRequestBody,com.azure.core.util.Context)` was added

#### `models.Incident` was modified

* `providerName()` was added
* `teamInformation()` was added
* `providerIncidentId()` was added

#### `SecurityInsightsManager` was modified

* `productPackagesOperations()` was added
* `getRecommendations()` was added
* `hunts()` was added
* `officeConsents()` was added
* `productTemplates()` was added
* `workspaceManagerAssignments()` was added
* `entitiesRelations()` was added
* `productSettings()` was added
* `threatIntelligences()` was added
* `entityQueries()` was added
* `reevaluates()` was added
* `workspaceManagerMembers()` was added
* `huntComments()` was added
* `productTemplatesOperations()` was added
* `gets()` was added
* `metadatas()` was added
* `dataConnectorDefinitions()` was added
* `resourceProviders()` was added
* `updates()` was added
* `getTriggeredAnalyticsRuleRuns()` was added
* `workspaceManagerGroups()` was added
* `huntRelations()` was added
* `triggeredAnalyticsRuleRunOperations()` was added
* `entityRelations()` was added
* `contentTemplatesOperations()` was added
* `entities()` was added
* `entitiesGetTimelines()` was added
* `contentPackageOperations()` was added
* `workspaceManagerConfigurations()` was added
* `alertRuleOperations()` was added
* `dataConnectorsCheckRequirementsOperations()` was added
* `sourceControlOperations()` was added
* `fileImports()` was added
* `sourceControls()` was added
* `contentTemplates()` was added
* `bookmarkRelations()` was added
* `entityQueryTemplates()` was added
* `billingStatistics()` was added
* `workspaceManagerAssignmentJobs()` was added
* `contentPackages()` was added
* `incidentTasks()` was added
* `productPackages()` was added
* `bookmarkOperations()` was added

#### `models.EntityKindEnum` was modified

* `NIC` was added

#### `models.AnomalySecurityMLAnalyticsSettings` was modified

* `withSettingsDefinitionId(java.lang.String)` was added

#### `models.Watchlists` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.FusionAlertRule` was modified

* `withScenarioExclusionPatterns(java.util.List)` was added
* `sourceSettings()` was added
* `subTechniques()` was added
* `withSourceSettings(java.util.List)` was added
* `scenarioExclusionPatterns()` was added

#### `models.IncidentRelations` was modified

* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.RelationInner,com.azure.core.util.Context)` was added
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.RelationInner)` was added

#### `models.UserInfo` was modified

* `withObjectId(java.lang.String)` was added

#### `models.Incident$Update` was modified

* `withTeamInformation(models.TeamInformation)` was added

## 1.0.0 (2025-01-03)

- Azure Resource Manager SecurityInsights client library for Java. This package contains Microsoft Azure SDK for SecurityInsights Management SDK. API spec for Microsoft.SecurityInsights (Azure Security Insights) resource provider. Package tag package-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager SecurityInsights client library for Java.

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
