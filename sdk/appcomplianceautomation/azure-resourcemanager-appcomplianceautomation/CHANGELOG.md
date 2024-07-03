# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-06-27)

- Azure Resource Manager App Compliance Automation client library for Java. This package contains Microsoft Azure SDK for App Compliance Automation Management SDK. App Compliance Automation Tool for Microsoft 365 API spec. Package tag package-2024-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ControlFamilyType` was removed

* `models.ControlType` was removed

* `models.AssessmentResource` was removed

* `models.Assessment` was removed

* `models.ReportResourceList` was removed

* `models.SnapshotOperations` was removed

* `models.AssessmentSeverity` was removed

* `models.SnapshotResourceList` was removed

* `models.IsPass` was removed

* `models.ReportOperations` was removed

* `models.CategoryType` was removed

* `models.ComplianceState` was removed

#### `models.ReportResourcePatch` was modified

* `withProperties(models.ReportProperties)` was removed
* `models.ReportProperties properties()` -> `models.ReportPatchProperties properties()`

#### `models.Reports` was modified

* `list(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ControlFamily` was modified

* `familyType()` was removed
* `familyStatus()` was removed
* `familyName()` was removed

#### `models.OverviewStatus` was modified

* `withPassedCount(java.lang.Integer)` was removed
* `withManualCount(java.lang.Integer)` was removed
* `withFailedCount(java.lang.Integer)` was removed

#### `models.Control` was modified

* `controlShortName()` was removed
* `assessments()` was removed
* `controlType()` was removed

#### `models.ReportComplianceStatus` was modified

* `withM365(models.OverviewStatus)` was removed

#### `models.ResourceMetadata` was modified

* `resourceName()` was removed
* `withTags(java.util.Map)` was removed
* `withResourceName(java.lang.String)` was removed
* `tags()` was removed

#### `models.ComplianceReportItem` was modified

* `complianceState()` was removed
* `policyDisplayName()` was removed
* `policyDescription()` was removed
* `statusChangeDate()` was removed
* `policyId()` was removed
* `controlType()` was removed
* `subscriptionId()` was removed
* `resourceGroup()` was removed

#### `models.SnapshotProperties` was modified

* `id()` was removed

#### `models.Category` was modified

* `categoryType()` was removed

#### `AppComplianceAutomationManager` was modified

* `snapshotOperations()` was removed
* `reportOperations()` was removed
* `fluent.AppComplianceAutomationToolForMicrosoft365 serviceClient()` -> `fluent.AppComplianceAutomationClient serviceClient()`

#### `models.Snapshots` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ReportProperties` was modified

* `id()` was removed
* `reportName()` was removed

### Features Added

* `models.CertSyncRecord` was added

* `models.WebhookResource` was added

* `models.SendAllEvents` was added

* `models.ReportResourceListResult` was added

* `models.ResponsibilityStatus` was added

* `models.ContentType` was added

* `models.EvidenceFileDownloadResponse` was added

* `models.ReportPatchProperties` was added

* `models.EvidenceResource` was added

* `models.ResourceOrigin` was added

* `models.ReportVerificationResult` was added

* `models.GetOverviewStatusResponse` was added

* `models.ControlSyncRecord` was added

* `models.TriggerEvaluationProperty` was added

* `models.ScopingQuestions` was added

* `models.ProviderActions` was added

* `models.ResponsibilitySeverity` was added

* `models.GetOverviewStatusRequest` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.WebhookResourceListResult` was added

* `models.TriggerEvaluationRequest` was added

* `models.InputType` was added

* `models.Result` was added

* `models.ListInUseStorageAccountsRequest` was added

* `models.RecommendationSolution` was added

* `models.ScopingConfigurationResource` was added

* `models.NotificationEvent` was added

* `models.Rule` was added

* `models.GetCollectionCountResponse` was added

* `models.ResponsibilityResource` was added

* `models.ScopingAnswer` was added

* `models.StatusItem` was added

* `models.EvidenceResourceListResult` was added

* `models.ScopingConfigurationProperties` was added

* `models.ScopingConfigurationResourceListResult` was added

* `models.WebhookKeyEnabled` was added

* `models.QuickAssessment` was added

* `models.Webhooks` was added

* `models.IsRecommendSolution` was added

* `models.ResponsibilityEnvironment` was added

* `models.WebhookResourcePatch` was added

* `models.OnboardRequest` was added

* `models.DeliveryStatus` was added

* `models.ResponsibilityType` was added

* `models.SnapshotResourceListResult` was added

* `models.SyncCertRecordResponse` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.WebhookStatus` was added

* `models.CheckNameAvailabilityReason` was added

* `models.StorageInfo` was added

* `models.SyncCertRecordRequest` was added

* `models.EnableSslVerification` was added

* `models.UpdateWebhookKey` was added

* `models.EvidenceFileDownloadResponseEvidenceFile` was added

* `models.WebhookProperties` was added

* `models.TriggerEvaluationResponse` was added

* `models.ListInUseStorageAccountsResponse` was added

* `models.EvidenceProperties` was added

* `models.ScopingQuestion` was added

* `models.OnboardResponse` was added

* `models.GetCollectionCountRequest` was added

* `models.EvidenceFileDownloadRequest` was added

* `models.EvidenceType` was added

* `models.Evidences` was added

* `models.ReportFixResult` was added

* `models.Responsibility` was added

* `models.ScopingConfigurations` was added

* `models.Recommendation` was added

#### `models.ReportResourcePatch` was modified

* `withProperties(models.ReportPatchProperties)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Reports` was modified

* `delete(java.lang.String,com.azure.core.util.Context)` was added
* `syncCertRecord(java.lang.String,models.SyncCertRecordRequest)` was added
* `getScopingQuestionsWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `nestedResourceCheckNameAvailabilityWithResponse(java.lang.String,models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was added
* `update(java.lang.String,models.ReportResourcePatch)` was added
* `createOrUpdate(java.lang.String,fluent.models.ReportResourceInner)` was added
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `fix(java.lang.String,com.azure.core.util.Context)` was added
* `fix(java.lang.String)` was added
* `get(java.lang.String)` was added
* `verify(java.lang.String,com.azure.core.util.Context)` was added
* `syncCertRecord(java.lang.String,models.SyncCertRecordRequest,com.azure.core.util.Context)` was added
* `update(java.lang.String,models.ReportResourcePatch,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String)` was added
* `getScopingQuestions(java.lang.String)` was added
* `verify(java.lang.String)` was added
* `nestedResourceCheckNameAvailability(java.lang.String,models.CheckNameAvailabilityRequest)` was added
* `createOrUpdate(java.lang.String,fluent.models.ReportResourceInner,com.azure.core.util.Context)` was added

#### `models.ControlFamily` was modified

* `controlFamilyName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `controlFamilyStatus()` was added

#### `models.OverviewStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `notApplicableCount()` was added
* `pendingCount()` was added

#### `models.DownloadResponseComplianceDetailedPdfReport` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComplianceResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Control` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `responsibilities()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `controlName()` was added

#### `models.ReportComplianceStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SnapshotDownloadRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceMetadata` was modified

* `withAccountId(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `accountId()` was added
* `withResourceOrigin(models.ResourceOrigin)` was added
* `resourceOrigin()` was added

#### `models.ComplianceReportItem` was modified

* `controlFamilyName()` was added
* `responsibilityDescription()` was added
* `resourceStatusChangeDate()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `controlStatus()` was added
* `responsibilityTitle()` was added
* `resourceOrigin()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resourceStatus()` was added

#### `models.SnapshotProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Category` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DownloadResponseCompliancePdfReport` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `AppComplianceAutomationManager` was modified

* `providerActions()` was added
* `webhooks()` was added
* `scopingConfigurations()` was added
* `evidences()` was added

#### `models.Snapshots` was modified

* `download(java.lang.String,java.lang.String,models.SnapshotDownloadRequest)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `download(java.lang.String,java.lang.String,models.SnapshotDownloadRequest,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added

#### `models.ReportProperties` was modified

* `storageInfo()` was added
* `withStorageInfo(models.StorageInfo)` was added
* `certRecords()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `errors()` was added

## 1.0.0-beta.1 (2022-11-15)

- Azure Resource Manager AppComplianceAutomation client library for Java. This package contains Microsoft Azure SDK for AppComplianceAutomation Management SDK. App Compliance Automation Tool for Microsoft 365 API spec. Package tag package-2022-11-16-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
