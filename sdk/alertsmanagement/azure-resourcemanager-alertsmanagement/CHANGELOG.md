# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
