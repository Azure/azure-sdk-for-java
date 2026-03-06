# Release History

## 1.0.0-beta.3 (2026-03-06)

- Azure Resource Manager AlertsManagement client library for Java. This package contains Microsoft Azure SDK for AlertsManagement Management SDK. Azure Alerts Management Service provides a single pane of glass of alerts across Azure Monitor. Package api-version 2025-05-25-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PrometheusRuleGroupResource` was removed

#### `models.PrometheusRule` was removed

#### `models.AlertRuleLeafCondition` was removed

#### `models.AlertProcessingRulesUpdateResponse` was removed

#### `models.SmartGroupsGetByIdResponse` was removed

#### `models.MonthlyRecurrence` was removed

#### `models.PatchObject` was removed

#### `models.PrometheusRuleResolveConfiguration` was removed

#### `models.Field` was removed

#### `models.AlertProcessingRulesListResponse` was removed

#### `models.AlertProcessingRulesListByResourceGroupHeaders` was removed

#### `models.PrometheusRuleGroupResource$Definition` was removed

#### `models.AlertProcessingRuleProperties` was removed

#### `models.AlertRuleRecommendationResource` was removed

#### `models.ActionGroup` was removed

#### `models.AlertProcessingRulesListHeaders` was removed

#### `models.SmartGroupModificationEvent` was removed

#### `models.AlertRuleAllOfCondition` was removed

#### `models.OperationsList` was removed

#### `models.Action` was removed

#### `models.SmartGroupAggregatedProperty` was removed

#### `models.SmartGroupModificationProperties` was removed

#### `models.SmartGroupsSortByFields` was removed

#### `models.ActionList` was removed

#### `models.AlertProcessingRules` was removed

#### `models.AddActionGroups` was removed

#### `models.SmartGroupModificationItem` was removed

#### `models.AlertProcessingRulesCreateOrUpdateResponse` was removed

#### `models.Operator` was removed

#### `models.PrometheusRuleGroupResourcePatchProperties` was removed

#### `models.AlertProcessingRulesDeleteHeaders` was removed

#### `models.Recurrence` was removed

#### `models.TenantActivityLogAlerts` was removed

#### `models.RemoveAllActionGroups` was removed

#### `models.AlertProcessingRulesList` was removed

#### `models.TenantAlertRulePatchObject` was removed

#### `models.SmartGroupModification` was removed

#### `models.AlertProcessingRulesCreateOrUpdateHeaders` was removed

#### `models.AlertProcessingRulesGetByResourceGroupHeaders` was removed

#### `models.ManagedResource` was removed

#### `models.AlertProcessingRule$Definition` was removed

#### `models.TenantActivityLogAlertResource` was removed

#### `models.AlertProcessingRule$UpdateStages` was removed

#### `models.PrometheusRuleGroups` was removed

#### `models.SmartGroupsGetByIdHeaders` was removed

#### `models.SmartGroupsChangeStateResponse` was removed

#### `models.AlertProcessingRule$Update` was removed

#### `models.Schedule` was removed

#### `models.DailyRecurrence` was removed

#### `models.SmartGroupsList` was removed

#### `models.TenantAlertRuleList` was removed

#### `models.PrometheusRuleGroupResourceCollection` was removed

#### `models.SmartGroup` was removed

#### `models.PrometheusRuleGroupResourcePatch` was removed

#### `models.AlertProcessingRulesUpdateHeaders` was removed

#### `models.AlertProcessingRulesListByResourceGroupNextHeaders` was removed

#### `models.AlertsList` was removed

#### `models.AlertRuleAnyOfOrLeafCondition` was removed

#### `models.PrometheusRuleGroupAction` was removed

#### `models.RecurrenceType` was removed

#### `models.DaysOfWeek` was removed

#### `models.Condition` was removed

#### `models.AlertProcessingRulesListByResourceGroupNextResponse` was removed

#### `models.PrometheusRuleGroupResource$Update` was removed

#### `models.AlertRuleRecommendationsListResponse` was removed

#### `models.PrometheusRuleGroupResource$DefinitionStages` was removed

#### `models.State` was removed

#### `models.PrometheusRuleGroupResource$UpdateStages` was removed

#### `models.AlertProcessingRule` was removed

#### `models.AlertProcessingRulesGetByResourceGroupResponse` was removed

#### `models.AlertProcessingRulesListByResourceGroupResponse` was removed

#### `models.AlertProcessingRulesListBySubscriptionNextHeaders` was removed

#### `models.RuleArmTemplate` was removed

#### `models.WeeklyRecurrence` was removed

#### `models.AlertProcessingRulesListBySubscriptionNextResponse` was removed

#### `models.SmartGroupsChangeStateHeaders` was removed

#### `models.AlertProcessingRulesDeleteResponse` was removed

#### `models.AlertProcessingRule$DefinitionStages` was removed

#### `models.AlertRuleRecommendations` was removed

#### `models.SmartGroups` was removed

#### `models.MonitorServiceList` was modified

* `MonitorServiceList()` was changed to private access
* `withData(java.util.List)` was removed
* `validate()` was removed

#### `models.MonitorServiceDetails` was modified

* `MonitorServiceDetails()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed
* `withProvider(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.Comments` was modified

* `validate()` was removed

#### `models.Essentials` was modified

* `Essentials()` was changed to private access
* `validate()` was removed
* `withTargetResource(java.lang.String)` was removed
* `withTargetResourceType(java.lang.String)` was removed
* `withTargetResourceName(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withTargetResourceGroup(java.lang.String)` was removed
* `withActionStatus(models.ActionStatus)` was removed

#### `models.AlertsSummaryGroup` was modified

* `AlertsSummaryGroup()` was changed to private access
* `withTotal(java.lang.Long)` was removed
* `withGroupedby(java.lang.String)` was removed
* `validate()` was removed
* `withValues(java.util.List)` was removed
* `withSmartGroupsCount(java.lang.Long)` was removed

#### `models.AlertModificationItem` was modified

* `AlertModificationItem()` was changed to private access
* `withModifiedAt(java.lang.String)` was removed
* `withNewValue(java.lang.String)` was removed
* `withComments(java.lang.String)` was removed
* `withModificationEvent(models.AlertModificationEvent)` was removed
* `withOldValue(java.lang.String)` was removed
* `validate()` was removed
* `withModifiedBy(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.ActionType` was modified

* `ADD_ACTION_GROUPS` was removed
* `REMOVE_ALL_ACTION_GROUPS` was removed

#### `models.AlertModificationProperties` was modified

* `AlertModificationProperties()` was changed to private access
* `validate()` was removed
* `withModifications(java.util.List)` was removed

#### `models.Operation` was modified

* `java.lang.String origin()` -> `models.Origin origin()`

#### `models.AlertModificationEvent` was modified

* `ACTIONS_FAILED` was removed
* `ACTION_RULE_TRIGGERED` was removed
* `ACTION_RULE_SUPPRESSED` was removed

#### `models.ActionStatus` was modified

* `ActionStatus()` was changed to private access
* `validate()` was removed
* `withIsSuppressed(java.lang.Boolean)` was removed

#### `models.AlertsMetadataProperties` was modified

* `validate()` was removed

#### `models.AlertProperties` was modified

* `AlertProperties()` was changed to private access
* `validate()` was removed
* `withEssentials(models.Essentials)` was removed

#### `models.Alerts` was modified

* `changeStateWithResponse(java.lang.String,models.AlertState,models.Comments,com.azure.core.util.Context)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `metadataWithResponse(models.Identifier,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String,models.MonitorService,models.MonitorCondition,models.Severity,models.AlertState,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,java.lang.Long,models.AlertsSortByFields,models.SortOrder,java.lang.String,models.TimeRange,java.lang.String,com.azure.core.util.Context)` was removed
* `getHistoryWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `changeState(java.lang.String,models.AlertState)` was removed
* `getSummary(models.AlertsSummaryGroupByFields)` was removed
* `getSummaryWithResponse(models.AlertsSummaryGroupByFields,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,models.MonitorService,models.MonitorCondition,models.Severity,models.AlertState,java.lang.String,models.TimeRange,java.lang.String,com.azure.core.util.Context)` was removed
* `metadata(models.Identifier)` was removed
* `list()` was removed
* `getById(java.lang.String)` was removed
* `getHistory(java.lang.String)` was removed

#### `AlertsManagementManager` was modified

* `alertProcessingRules()` was removed
* `tenantActivityLogAlerts()` was removed
* `alertRuleRecommendations()` was removed
* `smartGroups()` was removed
* `prometheusRuleGroups()` was removed

#### `models.AlertsSummaryGroupItem` was modified

* `AlertsSummaryGroupItem()` was changed to private access
* `withValues(java.util.List)` was removed
* `withCount(java.lang.Long)` was removed
* `withGroupedby(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed

### Features Added

* `models.Origin` was added

* `models.PrometheusInstantQuery` was added

* `models.Type` was added

* `models.TriggeredRule` was added

* `models.RuleType` was added

* `models.AlertModificationType` was added

* `models.ActionSuppressedDetails` was added

* `models.AlertEnrichmentResponse` was added

* `models.ActionTriggeredDetails` was added

* `models.Status` was added

* `models.NotificationResult` was added

* `models.BaseDetails` was added

* `models.AlertEnrichmentItem` was added

* `models.AlertEnrichmentProperties` was added

* `models.PrometheusRangeQuery` was added

* `models.PropertyChangeDetails` was added

* `models.ResultStatus` was added

#### `models.AlertModificationItem` was modified

* `details()` was added

#### `models.ActionType` was modified

* `INTERNAL` was added

#### `models.MonitorService` was modified

* `RESOURCE_HEALTH` was added

#### `models.Operation` was modified

* `actionType()` was added
* `isDataAction()` was added

#### `models.Alert` was modified

* `systemData()` was added

#### `models.AlertProperties` was modified

* `customProperties()` was added

#### `models.Alerts` was modified

* `getHistoryTenant(java.lang.String)` was added
* `getHistoryTenantWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getEnrichments(java.lang.String,java.lang.String)` was added
* `changeState(java.lang.String,java.lang.String,models.AlertState)` was added
* `getHistory(java.lang.String,java.lang.String)` was added
* `metaDataWithResponse(models.Identifier,com.azure.core.util.Context)` was added
* `getSummary(java.lang.String,models.AlertsSummaryGroupByFields)` was added
* `getAll(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.MonitorService,models.MonitorCondition,models.Severity,models.AlertState,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,java.lang.Long,models.AlertsSortByFields,models.SortOrder,java.lang.String,models.TimeRange,java.lang.String,com.azure.core.util.Context)` was added
* `changeStateWithResponse(java.lang.String,java.lang.String,models.AlertState,models.Comments,com.azure.core.util.Context)` was added
* `getByIdTenantWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getAllTenant(java.lang.String,java.lang.String,java.lang.String,models.MonitorService,models.MonitorCondition,models.Severity,models.AlertState,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,java.lang.Long,models.AlertsSortByFields,models.SortOrder,java.lang.String,models.TimeRange,java.lang.String,com.azure.core.util.Context)` was added
* `changeStateTenant(java.lang.String,models.AlertState)` was added
* `getSummaryWithResponse(java.lang.String,models.AlertsSummaryGroupByFields,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,models.MonitorService,models.MonitorCondition,models.Severity,models.AlertState,java.lang.String,models.TimeRange,java.lang.String,com.azure.core.util.Context)` was added
* `metaData(models.Identifier)` was added
* `getByIdTenant(java.lang.String)` was added
* `getAll(java.lang.String)` was added
* `changeStateTenantWithResponse(java.lang.String,models.AlertState,models.Comments,com.azure.core.util.Context)` was added
* `getById(java.lang.String,java.lang.String)` was added
* `getAllTenant()` was added
* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getHistoryWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getEnrichments(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.2 (2024-10-10)

- Azure Resource Manager AlertsManagement client library for Java. This package contains Microsoft Azure SDK for AlertsManagement Management SDK. AlertsManagement Client. Package tag package-2021-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AlertProcessingRules` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Alerts` was modified

* `changeStateWithResponse(java.lang.String,models.AlertState,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.PrometheusRuleGroupResource` was added

* `models.PrometheusRule` was added

* `models.AlertRuleLeafCondition` was added

* `models.Comments` was added

* `models.PrometheusRuleResolveConfiguration` was added

* `models.PrometheusRuleGroupResource$Definition` was added

* `models.AlertRuleRecommendationResource` was added

* `models.ActionGroup` was added

* `models.AlertRuleAllOfCondition` was added

* `models.ActionList` was added

* `models.PrometheusRuleGroupResourcePatchProperties` was added

* `models.TenantActivityLogAlerts` was added

* `models.TenantAlertRulePatchObject` was added

* `models.TenantActivityLogAlertResource` was added

* `models.PrometheusRuleGroups` was added

* `models.TenantAlertRuleList` was added

* `models.PrometheusRuleGroupResourceCollection` was added

* `models.PrometheusRuleGroupResourcePatch` was added

* `models.AlertRuleAnyOfOrLeafCondition` was added

* `models.PrometheusRuleGroupAction` was added

* `models.PrometheusRuleGroupResource$Update` was added

* `models.AlertRuleRecommendationsListResponse` was added

* `models.PrometheusRuleGroupResource$DefinitionStages` was added

* `models.PrometheusRuleGroupResource$UpdateStages` was added

* `models.RuleArmTemplate` was added

* `models.AlertRuleRecommendations` was added

#### `models.MonitorServiceList` was modified

* `metadataIdentifier()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MonitorServiceDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonthlyRecurrence` was modified

* `recurrenceType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PatchObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Essentials` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertProcessingRuleProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationsList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Action` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `actionType()` was added

#### `models.SmartGroupAggregatedProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SmartGroupModificationProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertsSummaryGroup` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlertProcessingRules` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AddActionGroups` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `actionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SmartGroupModificationItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertModificationItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Recurrence` was modified

* `recurrenceType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RemoveAllActionGroups` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `actionType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertProcessingRulesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertModificationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `id()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Schedule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DailyRecurrence` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `recurrenceType()` was added

#### `models.SmartGroupsList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActionStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Condition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AlertsMetadataProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `metadataIdentifier()` was added

#### `models.AlertProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Alerts` was modified

* `changeStateWithResponse(java.lang.String,models.AlertState,models.Comments,com.azure.core.util.Context)` was added

#### `AlertsManagementManager` was modified

* `alertRuleRecommendations()` was added
* `prometheusRuleGroups()` was added
* `tenantActivityLogAlerts()` was added

#### `models.AlertsSummaryGroupItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WeeklyRecurrence` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `recurrenceType()` was added

## 1.0.0-beta.1 (2022-08-24)

- Azure Resource Manager AlertsManagement client library for Java. This package contains Microsoft Azure SDK for AlertsManagement Management SDK. AlertsManagement Client. Package tag package-2021-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
