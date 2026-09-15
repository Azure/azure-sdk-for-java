# Release History

## 1.0.0-beta.2 (2026-09-15)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-09-30-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-08-31-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.GoalTemplate` was removed

#### `models.MoboBrokerResource` was removed

#### `models.ServiceGroupMembership` was removed

#### `models.GoalTemplateProperties` was removed

#### `models.GoalType` was removed

#### `models.ManagedOnBehalfOfConfiguration` was removed

#### `models.GoalAssignmentType` was removed

#### `models.GoalTemplates` was removed

#### `models.RequirementSelected` was removed

#### `models.MembershipType` was removed

#### `models.UserConfirmationForHighAvailabilityItem` was removed

#### `models.DrillProperties` was modified

* `managedOnBehalfOfConfiguration()` was removed

#### `models.DrillRuns` was modified

* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunFailoverRequest)` was removed

#### `models.GoalAssignmentProperties` was modified

* `goalAssignmentType()` was removed
* `withGoalTemplateId(java.lang.String)` was removed
* `withGoalAssignmentType(models.GoalAssignmentType)` was removed
* `goalTemplateId()` was removed

#### `models.GoalResourceProperties` was modified

* `withHighAvailabilityGoalParticipation(models.ExclusionState)` was removed
* `withDisasterRecoveryGoalParticipation(models.ExclusionState)` was removed
* `exclusionReasonForDisasterRecoveryGoals()` was removed
* `serviceGroupMemberships()` was removed
* `highAvailabilityGoalParticipation()` was removed
* `disasterRecoveryGoalParticipation()` was removed
* `disasterRecoveryAttestationStatus()` was removed
* `highAvailabilityAttestationStatus()` was removed
* `withDisasterRecoveryAttestationStatus(models.AttestationState)` was removed
* `exclusionReasonForHighAvailabilityGoals()` was removed
* `withHighAvailabilityAttestationStatus(models.AttestationState)` was removed
* `withUserConfirmationForHighAvailability(java.util.List)` was removed
* `userConfirmationForHighAvailability()` was removed

#### `models.ServiceLevelResource` was modified

* `withServiceLevelObjectiveResourceId(java.lang.String)` was removed
* `serviceLevelObjectiveResourceId()` was removed

#### `models.DrillRunProperties` was modified

* `java.util.List supportedVerbsForStage()` -> `java.util.List supportedVerbsForStage()`
* `models.DrillAttestation attestation()` -> `models.DrillAttestation attestation()`
* `models.JobType jobType()` -> `models.JobType jobType()`
* `models.DrillMode drillMode()` -> `models.DrillMode drillMode()`
* `java.lang.String drillId()` -> `java.lang.String drillId()`
* `java.util.List notes()` -> `java.util.List notes()`
* `toJson(com.azure.json.JsonWriter)` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `java.lang.String currentActiveOperationId()` -> `java.lang.String currentActiveOperationId()`

#### `models.UsagePlanType` was modified

* `BASIC` was removed

#### `ResilienceManagementManager` was modified

* `goalTemplates()` was removed

### Features Added

* `models.DrillRunReprotectRequest` was added

* `models.ResourceServiceBusProtectionSetting` was added

* `models.DrillReportGenerationStatus` was added

* `models.ResourceAzureTemplateProtectionSetting` was added

* `models.SliTypeMatchState` was added

* `models.RegionalObjectives` was added

* `models.ReplicationMode` was added

* `models.SkuDetails` was added

* `models.ResourceNetAppFilesProtectionSetting` was added

* `models.ResourceCosmosDBProtectionSetting` was added

* `models.ResourceFeasibilityReviewType` was added

* `models.ResourceCrossZoneVmRecoveryProtectionSetting` was added

* `models.DrillReportFormat` was added

* `models.SliMonitoringProperties` was added

* `models.ReportStageStatus` was added

* `models.SliAttentionStatus` was added

* `models.DrillRunTasks` was added

* `models.ResourceInclusionDisabledReason` was added

* `models.UserConfirmationItem` was added

* `models.DrillReportSummary` was added

* `models.GoalAssignmentPropertiesOfDrill` was added

* `models.SliType` was added

* `models.ListReportDownloadUrlRequest` was added

* `models.SliSelection` was added

* `models.ResourceStorageAccountProtectionSetting` was added

* `models.ResourceFeasibilityReviewStatus` was added

* `models.ResiliencyProperties` was added

* `models.ResourceFeasibilityReview` was added

* `models.DrillReportFinalizationState` was added

* `models.HealthModelMonitoringProperties` was added

* `models.ListReportDownloadUrlResponse` was added

#### `models.RecoveryResourceProperties` was modified

* `inclusionDisabledReasons()` was added

#### `models.LastRunProperties` was modified

* `lastRunRecoveryTimeActual()` was added

#### `models.RegionalDrillProperties` was modified

* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added

#### `models.AttentionReason` was modified

* `drillRbacOnGoalAssignment()` was added
* `rbacNeededForDrillOnHealthModel()` was added
* `discoveryRuleExists()` was added
* `drillRbacOnSli()` was added
* `goalAssignment()` was added
* `healthModelExists()` was added
* `recoveryPlan()` was added
* `sliAttentionStatuses()` was added
* `rbacNeededForDrillOnGoalAssignment()` was added
* `monitoringSourceNotConfigured()` was added
* `drillRbacOnHealthModel()` was added

#### `models.ValidateForExecutionProperties` was modified

* `withOperationName(models.DrillRunTasks)` was added
* `operationName()` was added

#### `models.ProvisioningState` was modified

* `NEEDS_ATTENTION` was added

#### `models.DrillProperties` was modified

* `healthModelMonitoringProperties()` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `goalAssignmentProperties()` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `sliMonitoringProperties()` was added

#### `models.DrillRuns` was modified

* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest)` was added
* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunReprotectRequest,com.azure.core.util.Context)` was added
* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest,com.azure.core.util.Context)` was added

#### `models.DrillUpdateProperties` was modified

* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `sliMonitoringProperties()` was added
* `healthModelMonitoringProperties()` was added
* `goalAssignmentProperties()` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

#### `models.ResourceProtectionSolutionType` was modified

* `AZURE_TEMPLATE` was added
* `AZURE_SERVICE_BUS` was added
* `AZURE_NET_APP_FILES` was added
* `AZURE_STORAGE_ACCOUNT` was added
* `AZURE_COSMOS_DB` was added

#### `models.GoalAssignmentProperties` was modified

* `withRequireRegionalResiliency(java.lang.Boolean)` was added
* `withRegionalObjectives(models.RegionalObjectives)` was added
* `regionalObjectives()` was added
* `requireZonalResiliency()` was added
* `withRequireZonalResiliency(boolean)` was added
* `requireRegionalResiliency()` was added

#### `models.RecoveryJobProperties` was modified

* `duration()` was added
* `startTime()` was added
* `userComments()` was added
* `status()` was added
* `jobExtendedInfo()` was added
* `retryDetails()` was added
* `endTime()` was added
* `executionConfigurations()` was added
* `operation()` was added
* `errorDetails()` was added
* `resourceId()` was added
* `triggeredBy()` was added

#### `models.ResourceProtectionSolutionSettings` was modified

* `replicationMode()` was added

#### `models.GoalResourceProperties` was modified

* `withZonalResiliency(models.ResiliencyProperties)` was added
* `regionalResiliency()` was added
* `zonalResiliency()` was added
* `withRegionalResiliency(models.ResiliencyProperties)` was added

#### `models.DrillRunProperties` was modified

* `triggeredBy()` was added
* `innerModel()` was added
* `endTime()` was added
* `executionConfigurations()` was added
* `resourceId()` was added
* `recoveryTimeObjective()` was added
* `retryDetails()` was added
* `jobExtendedInfo()` was added
* `userComments()` was added
* `status()` was added
* `duration()` was added
* `report()` was added
* `errorDetails()` was added
* `startTime()` was added
* `operation()` was added

#### `models.OperationQualificationDetails` was modified

* `resourceFeasibilityReviews()` was added

#### `models.ZonalDrillProperties` was modified

* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added

## 1.0.0-beta.1 (2026-06-15)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-resiliencemanagement Java SDK.
