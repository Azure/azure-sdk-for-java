# Release History

## 1.0.0-beta.2 (2024-06-27)

- Azure Resource Manager App Compliance Automation client library for Java. This package contains Microsoft Azure SDK for App Compliance Automation Management SDK. App Compliance Automation Tool for Microsoft 365 API spec. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ControlFamilyType` was removed

* `models.OperationListResult` was removed

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
* `list()` was removed

#### `models.ControlFamily` was modified

* `familyName()` was removed
* `familyStatus()` was removed
* `familyType()` was removed

#### `models.OverviewStatus` was modified

* `withFailedCount(java.lang.Integer)` was removed
* `withManualCount(java.lang.Integer)` was removed
* `withPassedCount(java.lang.Integer)` was removed

#### `models.Control` was modified

* `assessments()` was removed
* `controlType()` was removed
* `controlShortName()` was removed

#### `models.ReportComplianceStatus` was modified

* `withM365(models.OverviewStatus)` was removed

#### `models.ResourceMetadata` was modified

* `withResourceName(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `tags()` was removed
* `resourceName()` was removed

#### `models.ComplianceReportItem` was modified

* `policyDescription()` was removed
* `policyId()` was removed
* `controlType()` was removed
* `policyDisplayName()` was removed
* `complianceState()` was removed
* `statusChangeDate()` was removed
* `resourceGroup()` was removed
* `subscriptionId()` was removed

#### `models.SnapshotProperties` was modified

* `id()` was removed

#### `models.Category` was modified

* `categoryType()` was removed

#### `AppComplianceAutomationManager` was modified

* `fluent.AppComplianceAutomationToolForMicrosoft365 serviceClient()` -> `fluent.AppComplianceAutomationClient serviceClient()`
* `snapshotOperations()` was removed
* `reportOperations()` was removed

#### `models.Snapshots` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String)` was removed

#### `models.ReportProperties` was modified

* `reportName()` was removed
* `id()` was removed

### Features Added

* `models.CertSyncRecord` was added

* `models.WebhookResource` was added

* `models.SendAllEvents` was added

* `models.ResponsibilityStatus` was added

* `implementation.models.EvidenceResourceListResult` was added

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

* `models.TriggerEvaluationRequest` was added

* `models.InputType` was added

* `models.Result` was added

* `models.ListInUseStorageAccountsRequest` was added

* `models.RecommendationSolution` was added

* `implementation.models.ReportResourceListResult` was added

* `implementation.models.ScopingConfigurationResourceListResult` was added

* `implementation.models.WebhookResourceListResult` was added

* `models.ScopingConfigurationResource` was added

* `models.NotificationEvent` was added

* `models.Rule` was added

* `models.GetCollectionCountResponse` was added

* `models.ResponsibilityResource` was added

* `models.ScopingAnswer` was added

* `models.StatusItem` was added

* `models.ScopingConfigurationProperties` was added

* `models.WebhookKeyEnabled` was added

* `models.QuickAssessment` was added

* `models.Webhooks` was added

* `models.IsRecommendSolution` was added

* `models.ResponsibilityEnvironment` was added

* `models.WebhookResourcePatch` was added

* `models.OnboardRequest` was added

* `models.DeliveryStatus` was added

* `models.ResponsibilityType` was added

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

* `implementation.models.SnapshotResourceListResult` was added

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

* `implementation.models.OperationListResult` was added

#### `models.ReportResourcePatch` was modified

* `withProperties(models.ReportPatchProperties)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Reports` was modified

* `getScopingQuestions(java.lang.String)` was added
* `listByTenant(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `syncCertRecord(java.lang.String,models.SyncCertRecordRequest,com.azure.core.util.Context)` was added
* `delete(java.lang.String,com.azure.core.util.Context)` was added
* `fix(java.lang.String,com.azure.core.util.Context)` was added
* `syncCertRecord(java.lang.String,models.SyncCertRecordRequest)` was added
* `checkNameAvailability(java.lang.String,models.CheckNameAvailabilityRequest)` was added
* `fix(java.lang.String)` was added
* `checkNameAvailabilityWithResponse(java.lang.String,models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was added
* `update(java.lang.String,models.ReportResourcePatch)` was added
* `delete(java.lang.String)` was added
* `getScopingQuestionsWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `verify(java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdate(java.lang.String,fluent.models.ReportResourceInner,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listByTenant()` was added
* `createOrUpdate(java.lang.String,fluent.models.ReportResourceInner)` was added
* `get(java.lang.String)` was added
* `update(java.lang.String,models.ReportResourcePatch,com.azure.core.util.Context)` was added
* `verify(java.lang.String)` was added

#### `models.ControlFamily` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `controlFamilyName()` was added
* `controlFamilyStatus()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OverviewStatus` was modified

* `notApplicableCount()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `pendingCount()` was added

#### `models.DownloadResponseComplianceDetailedPdfReport` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComplianceResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Control` was modified

* `responsibilities()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `controlName()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReportComplianceStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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

* `resourceOrigin()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `accountId()` was added
* `withAccountId(java.lang.String)` was added
* `withResourceOrigin(models.ResourceOrigin)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComplianceReportItem` was modified

* `resourceStatus()` was added
* `resourceStatusChangeDate()` was added
* `resourceOrigin()` was added
* `controlFamilyName()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `responsibilityDescription()` was added
* `controlStatus()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `responsibilityTitle()` was added

#### `models.SnapshotProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Category` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DownloadResponseCompliancePdfReport` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `AppComplianceAutomationManager` was modified

* `providerActions()` was added
* `webhooks()` was added
* `evidences()` was added
* `scopingConfigurations()` was added

#### `models.Snapshots` was modified

* `download(java.lang.String,java.lang.String,models.SnapshotDownloadRequest,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added
* `listByReportResource(java.lang.String)` was added
* `download(java.lang.String,java.lang.String,models.SnapshotDownloadRequest)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByReportResource(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ReportProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `certRecords()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withStorageInfo(models.StorageInfo)` was added
* `errors()` was added
* `storageInfo()` was added

## 1.0.0-beta.1 (2022-11-15)

- Azure Resource Manager AppComplianceAutomation client library for Java. This package contains Microsoft Azure SDK for AppComplianceAutomation Management SDK. App Compliance Automation Tool for Microsoft 365 API spec. Package tag package-2022-11-16-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
