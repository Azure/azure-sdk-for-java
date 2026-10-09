# Release History

## 1.0.0 (2026-10-09)

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

* `recoveryPhysicalZones()` was removed
* `advisorHaRecommendationId()` was removed
* `activePhysicalZones()` was removed
* `haStatus()` was removed

#### `models.DrillRuns` was modified

* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunFailoverRequest)` was removed

#### `models.GoalAssignmentProperties` was modified

* `withGoalAssignmentType(models.GoalAssignmentType)` was removed
* `goalAssignmentType()` was removed
* `goalTemplateId()` was removed
* `withGoalTemplateId(java.lang.String)` was removed

#### `models.UnifiedResilienceItemProperties` was modified

* `recommendations()` was removed

#### `models.GoalResourceProperties` was modified

* `userConfirmationForHighAvailability()` was removed
* `disasterRecoveryGoalParticipation()` was removed
* `highAvailabilityGoalParticipation()` was removed
* `exclusionReasonForDisasterRecoveryGoals()` was removed
* `withHighAvailabilityAttestationStatus(models.AttestationState)` was removed
* `highAvailabilityAttestationStatus()` was removed
* `serviceGroupMemberships()` was removed
* `withDisasterRecoveryGoalParticipation(models.ExclusionState)` was removed
* `disasterRecoveryAttestationStatus()` was removed
* `withHighAvailabilityGoalParticipation(models.ExclusionState)` was removed
* `withDisasterRecoveryAttestationStatus(models.AttestationState)` was removed
* `exclusionReasonForHighAvailabilityGoals()` was removed
* `withUserConfirmationForHighAvailability(java.util.List)` was removed

#### `models.GoalsData` was modified

* `requireHighAvailability()` was removed
* `regionalRecoveryPointObjectiveStatus()` was removed
* `regionalRecoveryTimeActualInMinutes()` was removed
* `requireDisasterRecovery()` was removed
* `regionalRecoveryPointObjectiveInMinutes()` was removed
* `regionalRecoveryTimeObjectiveInMinutes()` was removed
* `templateId()` was removed
* `regionalRecoveryTimeObjectiveStatus()` was removed
* `regionalRecoveryPointEstimatedInMinutes()` was removed

#### `models.ServiceLevelResource` was modified

* `withServiceLevelObjectiveResourceId(java.lang.String)` was removed
* `serviceLevelObjectiveResourceId()` was removed

#### `models.DrillRunProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was removed
* `java.util.List supportedVerbsForStage()` -> `java.util.List supportedVerbsForStage()`
* `models.DrillAttestation attestation()` -> `models.DrillAttestation attestation()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `java.lang.String currentActiveOperationId()` -> `java.lang.String currentActiveOperationId()`
* `java.util.List notes()` -> `java.util.List notes()`
* `java.lang.String drillId()` -> `java.lang.String drillId()`
* `models.DrillMode drillMode()` -> `models.DrillMode drillMode()`
* `models.JobType jobType()` -> `models.JobType jobType()`

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

* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

#### `models.AttentionReason` was modified

* `healthModelAssociatedWithServiceGroup()` was added
* `monitoringSourceNotConfigured()` was added
* `drillRbacOnGoalAssignment()` was added
* `recoveryPlan()` was added
* `drillRbacOnHealthModel()` was added
* `rbacNeededForDrillOnHealthModel()` was added
* `sliAttentionStatuses()` was added
* `discoveryRuleExists()` was added
* `healthModelExists()` was added
* `drillRbacOnSli()` was added
* `goalAssignment()` was added
* `rbacNeededForDrillOnGoalAssignment()` was added

#### `models.ValidateForExecutionProperties` was modified

* `operationName()` was added
* `withOperationName(models.DrillRunTasks)` was added

#### `models.ProvisioningState` was modified

* `NEEDS_ATTENTION` was added

#### `models.DrillProperties` was modified

* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `healthModelMonitoringProperties()` was added
* `sliMonitoringProperties()` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added
* `goalAssignmentProperties()` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added

#### `models.DrillResourceProperties` was modified

* `drillType()` was added

#### `models.DrillRuns` was modified

* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest,com.azure.core.util.Context)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunReprotectRequest,com.azure.core.util.Context)` was added
* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.DrillUpdateProperties` was modified

* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `healthModelMonitoringProperties()` was added
* `sliMonitoringProperties()` was added
* `goalAssignmentProperties()` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `withGoalAssignmentProperties(models.GoalAssignmentPropertiesOfDrill)` was added

#### `models.GoalAssignmentProperties` was modified

* `requireZonalResiliency()` was added
* `withRequireZonalResiliency(boolean)` was added

#### `models.RecoveryJobProperties` was modified

* `startTime()` was added
* `executionConfigurations()` was added
* `operation()` was added
* `resourceId()` was added
* `userComments()` was added
* `retryDetails()` was added
* `errorDetails()` was added
* `duration()` was added
* `endTime()` was added
* `jobExtendedInfo()` was added
* `triggeredBy()` was added
* `status()` was added

#### `models.UnifiedResilienceItemProperties` was modified

* `billingInfo()` was added
* `resiliencyPosture()` was added

#### `models.GoalResourceProperties` was modified

* `withZonalResiliency(models.ResiliencyProperties)` was added
* `zonalResiliency()` was added

#### `models.GoalsData` was modified

* `zonalResiliency()` was added

#### `models.DrillRunProperties` was modified

* `retryDetails()` was added
* `status()` was added
* `errorDetails()` was added
* `triggeredBy()` was added
* `duration()` was added
* `endTime()` was added
* `startTime()` was added
* `innerModel()` was added
* `report()` was added
* `jobExtendedInfo()` was added
* `operation()` was added
* `userComments()` was added
* `executionConfigurations()` was added
* `resourceId()` was added

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
