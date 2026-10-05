# Release History

## 1.0.0-beta.2 (2026-09-22)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-08-31-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MoboBrokerResource` was removed

#### `models.ManagedOnBehalfOfConfiguration` was removed

#### `models.UserConfirmationForHighAvailabilityItem` was removed

#### `models.DrillProperties` was modified

* `managedOnBehalfOfConfiguration()` was removed

#### `models.DrillRuns` was modified

* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunFailoverRequest)` was removed

#### `models.DrillRunProperties` was modified

* `java.util.List notes()` -> `java.util.List notes()`
* `models.DrillMode drillMode()` -> `models.DrillMode drillMode()`
* `java.util.List supportedVerbsForStage()` -> `java.util.List supportedVerbsForStage()`
* `models.JobType jobType()` -> `models.JobType jobType()`
* `java.lang.String drillId()` -> `java.lang.String drillId()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `models.DrillAttestation attestation()` -> `models.DrillAttestation attestation()`
* `java.lang.String currentActiveOperationId()` -> `java.lang.String currentActiveOperationId()`
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

* `models.ListReportDownloadUrlResponse` was added

#### `models.RegionalDrillProperties` was modified

* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

#### `models.AttentionReason` was modified

* `discoveryRuleExists()` was added
* `sliAttentionStatuses()` was added
* `healthModelExists()` was added
* `drillRbacOnHealthModel()` was added
* `drillRbacOnSli()` was added
* `monitoringSourceNotConfigured()` was added
* `rbacNeededForDrillOnHealthModel()` was added

#### `models.ValidateForExecutionProperties` was modified

* `operationName()` was added
* `withOperationName(models.DrillRunTasks)` was added

#### `models.ProvisioningState` was modified

* `NEEDS_ATTENTION` was added

#### `models.DrillProperties` was modified

* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `healthModelMonitoringProperties()` was added
* `sliMonitoringProperties()` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

#### `models.DrillRuns` was modified

* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `reprotect(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.DrillRunReprotectRequest,com.azure.core.util.Context)` was added
* `failOver(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest,com.azure.core.util.Context)` was added
* `generateReport(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listReportDownloadUrl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ListReportDownloadUrlRequest)` was added

#### `models.DrillUpdateProperties` was modified

* `sliMonitoringProperties()` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added
* `healthModelMonitoringProperties()` was added
* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added

#### `models.GoalAssignmentProperties` was modified

* `withRequireZonalResiliency(java.lang.Boolean)` was added
* `requireZonalResiliency()` was added

#### `models.RecoveryJobProperties` was modified

* `retryDetails()` was added
* `triggeredBy()` was added
* `duration()` was added
* `executionConfigurations()` was added
* `operation()` was added
* `userComments()` was added
* `status()` was added
* `endTime()` was added
* `errorDetails()` was added
* `startTime()` was added
* `jobExtendedInfo()` was added
* `resourceId()` was added

#### `models.GoalResourceProperties` was modified

* `zonalResiliency()` was added
* `withZonalResiliency(models.ResiliencyProperties)` was added

#### `models.DrillRunProperties` was modified

* `errorDetails()` was added
* `operation()` was added
* `jobExtendedInfo()` was added
* `resourceId()` was added
* `retryDetails()` was added
* `status()` was added
* `report()` was added
* `startTime()` was added
* `userComments()` was added
* `duration()` was added
* `executionConfigurations()` was added
* `triggeredBy()` was added
* `innerModel()` was added
* `endTime()` was added

#### `models.OperationQualificationDetails` was modified

* `resourceFeasibilityReviews()` was added

#### `models.ZonalDrillProperties` was modified

* `withHealthModelMonitoringProperties(models.HealthModelMonitoringProperties)` was added
* `withSliMonitoringProperties(models.SliMonitoringProperties)` was added

## 1.0.0-beta.1 (2026-06-15)

- Azure Resource Manager Resilience Management client library for Java. This package contains Microsoft Azure SDK for Resilience Management Management SDK.  Package api-version 2026-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-resiliencemanagement Java SDK.
