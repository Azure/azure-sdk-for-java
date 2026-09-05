# Release History

## 1.0.0-beta.2 (2026-08-26)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-08-31-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MoboBrokerResource` was removed

#### `models.ManagedOnBehalfOfConfiguration` was removed

#### `models.UserConfirmationForHighAvailabilityItem` was removed

#### `models.DrillProperties` was modified

* `managedOnBehalfOfConfiguration()` was removed

#### `models.DrillRuns` was modified

* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunFailoverRequest)` was removed
* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.DrillRunProperties` was modified

* `java.util.List notes()` -> `java.util.List notes()`
* `models.JobType jobType()` -> `models.JobType jobType()`
* `java.lang.String currentActiveOperationId()` -> `java.lang.String currentActiveOperationId()`
* `java.util.List supportedVerbsForStage()` -> `java.util.List supportedVerbsForStage()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `models.DrillMode drillMode()` -> `models.DrillMode drillMode()`
* `models.DrillAttestation attestation()` -> `models.DrillAttestation attestation()`
* `java.lang.String drillId()` -> `java.lang.String drillId()`
* `toJson(com.azure.json.JsonWriter)` was removed

### Features Added

* `models.DrillRunReprotectRequest` was added

* `models.DrillReportGenerationStatus` was added

* `models.SliTypeMatchState` was added

* `models.SkuDetails` was added

* `models.ResourceFeasibilityReviewType` was added

* `models.ResourceCrossZoneVmRecoveryProtectionSetting` was added

* `models.DrillReportFormat` was added

* `models.SliMonitoringProperties` was added

* `models.ReportStageStatus` was added

* `models.SliAttentionStatus` was added

* `models.DrillRunTasks` was added

* `models.UserConfirmationItem` was added

* `models.DrillReportSummary` was added

* `models.SliType` was added

* `models.ListReportDownloadUrlRequest` was added

* `models.SliSelection` was added

* `models.ResourceFeasibilityReviewStatus` was added

* `models.ResiliencyProperties` was added

* `models.ResourceFeasibilityReview` was added

* `models.DrillReportFinalizationState` was added

* `models.HealthModelMonitoringProperties` was added

#### `models.RegionalDrillProperties` was modified

* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

#### `models.AttentionReason` was modified

* `drillRbacOnHealthModel()` was added
* `sliAttentionStatuses()` was added
* `rbacNeededForDrillOnHealthModel()` was added
* `drillRbacOnSli()` was added
* `discoveryRuleExists()` was added
* `healthModelExists()` was added
* `monitoringSourceNotConfigured()` was added

#### `models.ValidateForExecutionProperties` was modified

* `operationName()` was added
* `withOperationName(models.DrillRunTasks)` was added

#### `models.ProvisioningState` was modified

* `NEEDS_ATTENTION` was added

#### `models.DrillProperties` was modified

* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `sliMonitoringProperties()` was added
* `healthModelMonitoringProperties()` was added

#### `models.DrillRuns` was modified

* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest,com.azure.core.util.Context)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunReprotectRequest,com.azure.core.util.Context)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.DrillUpdateProperties` was modified

* `healthModelMonitoringProperties()` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `sliMonitoringProperties()` was added

#### `models.GoalAssignmentProperties` was modified

* `requireZonalResiliency()` was added
* `withRequireZonalResiliency(java.lang.Boolean)` was added

#### `models.RecoveryJobProperties` was modified

* `operation()` was added
* `resourceId()` was added
* `startTime()` was added
* `endTime()` was added
* `status()` was added
* `errorDetails()` was added
* `jobExtendedInfo()` was added
* `triggeredBy()` was added
* `retryDetails()` was added
* `userComments()` was added
* `duration()` was added
* `executionConfigurations()` was added

#### `models.GoalResourceProperties` was modified

* `withZonalResiliency(models.ResiliencyProperties)` was added
* `zonalResiliency()` was added

#### `models.DrillRunProperties` was modified

* `report()` was added
* `userComments()` was added
* `jobExtendedInfo()` was added
* `startTime()` was added
* `operation()` was added
* `endTime()` was added
* `innerModel()` was added
* `status()` was added
* `executionConfigurations()` was added
* `resourceId()` was added
* `duration()` was added
* `triggeredBy()` was added
* `retryDetails()` was added
* `errorDetails()` was added

#### `models.OperationQualificationDetails` was modified

* `resourceFeasibilityReviews()` was added

#### `models.ZonalDrillProperties` was modified

* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added

## 1.0.0-beta.1 (2026-06-15)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-resiliencemanagement Java SDK.
