# Release History

## 1.0.0 (2026-10-07)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.GoalTemplate` was removed

#### `models.MoboBrokerResource` was removed

#### `models.ServiceGroupMembership` was removed

#### `models.GoalTemplateProperties` was removed

#### `models.UnifiedResilienceItemRequirementSelected` was removed

#### `models.GoalType` was removed

#### `models.IsoDuration` was removed

#### `models.ManagedOnBehalfOfConfiguration` was removed

#### `models.GoalAssignmentType` was removed

#### `models.GoalTemplates` was removed

#### `models.RequirementSelected` was removed

#### `models.MembershipType` was removed

#### `models.RecommendationsData` was removed

#### `models.ResilienceHealthStatus` was removed

#### `models.UserConfirmationForHighAvailabilityItem` was removed

#### `models.RecommendationsHighAvailabilityData` was removed

#### `models.DrillProperties` was modified

* `managedOnBehalfOfConfiguration()` was removed

#### `models.DrillResourceProperties` was modified

* `activePhysicalZones()` was removed
* `advisorHaRecommendationId()` was removed
* `haStatus()` was removed
* `recoveryPhysicalZones()` was removed

#### `models.DrillRuns` was modified

* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunFailoverRequest)` was removed
* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.GoalAssignmentProperties` was modified

* `withGoalAssignmentType(models.GoalAssignmentType)` was removed
* `goalTemplateId()` was removed
* `withGoalTemplateId(java.lang.String)` was removed
* `goalAssignmentType()` was removed

#### `models.UnifiedResilienceItemProperties` was modified

* `recommendations()` was removed

#### `models.GoalResourceProperties` was modified

* `highAvailabilityGoalParticipation()` was removed
* `serviceGroupMemberships()` was removed
* `exclusionReasonForHighAvailabilityGoals()` was removed
* `withDisasterRecoveryAttestationStatus(models.AttestationState)` was removed
* `disasterRecoveryAttestationStatus()` was removed
* `withHighAvailabilityGoalParticipation(models.ExclusionState)` was removed
* `exclusionReasonForDisasterRecoveryGoals()` was removed
* `highAvailabilityAttestationStatus()` was removed
* `withDisasterRecoveryGoalParticipation(models.ExclusionState)` was removed
* `userConfirmationForHighAvailability()` was removed
* `withHighAvailabilityAttestationStatus(models.AttestationState)` was removed
* `disasterRecoveryGoalParticipation()` was removed
* `withUserConfirmationForHighAvailability(java.util.List)` was removed

#### `models.GoalsData` was modified

* `requireHighAvailability()` was removed
* `templateId()` was removed
* `regionalRecoveryTimeObjectiveStatus()` was removed
* `regionalRecoveryPointEstimatedInMinutes()` was removed
* `regionalRecoveryTimeActualInMinutes()` was removed
* `regionalRecoveryPointObjectiveInMinutes()` was removed
* `requireDisasterRecovery()` was removed
* `regionalRecoveryPointObjectiveStatus()` was removed
* `regionalRecoveryTimeObjectiveInMinutes()` was removed

#### `models.ServiceLevelResource` was modified

* `serviceLevelObjectiveResourceId()` was removed
* `withServiceLevelObjectiveResourceId(java.lang.String)` was removed

#### `models.DrillRunProperties` was modified

* `models.DrillMode drillMode()` -> `models.DrillMode drillMode()`
* `java.util.List supportedVerbsForStage()` -> `java.util.List supportedVerbsForStage()`
* `java.lang.String drillId()` -> `java.lang.String drillId()`
* `models.JobType jobType()` -> `models.JobType jobType()`
* `toJson(com.azure.json.JsonWriter)` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `models.DrillAttestation attestation()` -> `models.DrillAttestation attestation()`
* `java.util.List notes()` -> `java.util.List notes()`
* `java.lang.String currentActiveOperationId()` -> `java.lang.String currentActiveOperationId()`

#### `models.UsagePlanType` was modified

* `BASIC` was removed

#### `ResilienceManagementManager` was modified

* `goalTemplates()` was removed

### Features Added

* `models.DrillRunReprotectRequest` was added

* `models.UnifiedResilienceItemGoalRequirement` was added

* `models.DrillReportGenerationStatus` was added

* `models.UnifiedResilienceItemBillingInfo` was added

* `models.SliTypeMatchState` was added

* `models.SkuDetails` was added

* `models.ResourceFeasibilityReviewType` was added

* `models.ResourceCrossZoneVmRecoveryProtectionSetting` was added

* `models.DrillReportFormat` was added

* `models.ZonalDrillResourceProperties` was added

* `models.SliMonitoringProperties` was added

* `models.ReportStageStatus` was added

* `models.SliAttentionStatus` was added

* `models.DrillRunTasks` was added

* `models.UserConfirmationItem` was added

* `models.DrillReportSummary` was added

* `models.GoalAssignmentPropertiesOfDrill` was added

* `models.SliType` was added

* `models.ListReportDownloadUrlRequest` was added

* `models.SliSelection` was added

* `models.RegionalDrillResourceProperties` was added

* `models.UnifiedResilienceItemResiliencyPosture` was added

* `models.ResourceFeasibilityReviewStatus` was added

* `models.UnifiedResilienceItemZonalResiliencyPosture` was added

* `models.ResiliencyProperties` was added

* `models.ResourceFeasibilityReview` was added

* `models.DrillReportFinalizationState` was added

* `models.HealthModelMonitoringProperties` was added

* `models.ListReportDownloadUrlResponse` was added

#### `models.RegionalDrillProperties` was modified

* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added

#### `models.AttentionReason` was modified

* `rbacNeededForDrillOnGoalAssignment()` was added
* `discoveryRuleExists()` was added
* `drillRbacOnSli()` was added
* `drillRbacOnGoalAssignment()` was added
* `recoveryPlan()` was added
* `healthModelAssociatedWithServiceGroup()` was added
* `monitoringSourceNotConfigured()` was added
* `drillRbacOnHealthModel()` was added
* `rbacNeededForDrillOnHealthModel()` was added
* `healthModelExists()` was added
* `sliAttentionStatuses()` was added
* `goalAssignment()` was added

#### `models.ValidateForExecutionProperties` was modified

* `operationName()` was added
* `withOperationName(models.DrillRunTasks)` was added

#### `models.ProvisioningState` was modified

* `NEEDS_ATTENTION` was added

#### `models.DrillProperties` was modified

* `healthModelMonitoringProperties()` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `goalAssignmentProperties()` was added
* `sliMonitoringProperties()` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

#### `models.DrillResourceProperties` was modified

* `drillType()` was added

#### `models.DrillRuns` was modified

* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunReprotectRequest,com.azure.core.util.Context)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest,com.azure.core.util.Context)` was added

#### `models.DrillUpdateProperties` was modified

* `sliMonitoringProperties()` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `healthModelMonitoringProperties()` was added
* `goalAssignmentProperties()` was added

#### `models.GoalAssignmentProperties` was modified

* `requireZonalResiliency()` was added
* `withRequireZonalResiliency(boolean)` was added

#### `models.RecoveryJobProperties` was modified

* `errorDetails()` was added
* `executionConfigurations()` was added
* `status()` was added
* `userComments()` was added
* `startTime()` was added
* `retryDetails()` was added
* `triggeredBy()` was added
* `operation()` was added
* `duration()` was added
* `endTime()` was added
* `jobExtendedInfo()` was added
* `resourceId()` was added

#### `models.UnifiedResilienceItemProperties` was modified

* `billingInfo()` was added
* `resiliencyPosture()` was added

#### `models.GoalResourceProperties` was modified

* `zonalResiliency()` was added
* `withZonalResiliency(models.ResiliencyProperties)` was added

#### `models.GoalsData` was modified

* `zonalResiliency()` was added

#### `models.DrillRunProperties` was modified

* `resourceId()` was added
* `triggeredBy()` was added
* `endTime()` was added
* `executionConfigurations()` was added
* `report()` was added
* `jobExtendedInfo()` was added
* `startTime()` was added
* `userComments()` was added
* `innerModel()` was added
* `duration()` was added
* `status()` was added
* `errorDetails()` was added
* `operation()` was added
* `retryDetails()` was added

#### `models.OperationQualificationDetails` was modified

* `resourceFeasibilityReviews()` was added

#### `models.ZonalDrillProperties` was modified

* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

## 1.0.0-beta.1 (2026-06-15)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-resiliencemanagement Java SDK.
