# Release History

## 1.0.0 (2025-04-04)

This release is the first stable release of the Azure Compute Batch client library.

### Breaking Changes

Most of these changes are minor name/casing changes.

- Changed capitalization of the methods in some models
  - On `BatchJobNetworkConfiguration`, `isSkipWithdrawFromVNet()` is now `isSkipWithdrawFromVnet()`.
  - On `BatchJobScheduleStatistics`, `getReadIOps()` is now `getReadIops()` and `getWriteIOps()` is now `getWriteIops()`.
  - On `BatchJobStatistics`, `getReadIOps()` is now `getReadIops()` and `getWriteIOps()` is now `getWriteIops()`.
  - On `BatchPoolResourceStatistics`, `getDiskReadIOps()` is now `getDiskReadIops()` and `getDiskWriteIOps()` is now `getDiskWriteIops()`.
  - On `BatchTaskStatistics`, `getReadIOps()` is now `getReadIops()` and `getWriteIOps()` is now `getWriteIops()`.
  - On `NetworkConfiguration`, `getDynamicVNetAssignmentScope()` is now `getDynamicVnetAssignmentScope()` and `setDynamicVNetAssignmentScope(DynamicVNetAssignmentScope dynamicVNetAssignmentScope)` is now `setDynamicVnetAssignmentScope(DynamicVNetAssignmentScope dynamicVnetAssignmentScope)`.

- Changed casing of the methods in some models
  - On `BatchJobStatistics`, `getReadIOGiB()` is now `getReadIoGiB()` and `getWriteIOGiB()` is now `getWriteIoGiB()`.
  - On `BatchJobScheduleStatistics`, `getReadIOGiB()` is now `getReadIoGiB()` and `getWriteIOGiB()` is now `getWriteIoGiB()`.
  - On `BatchTaskStatistics`, `getReadIOGiB()` is now `getReadIoGiB()` and `getWriteIOGiB()` is now `getWriteIoGiB()`.
  - `VMDiskSecurityProfile` has now been changed to `VmDiskSecurityProfile`.

- Changed names of some model properties
  - On `BatchJobStatistics`, `getNumSucceededTasks()` is now `getSucceededTasksCount()`, `getNumFailedTasks()` is now `getFailedTasksCount()`, and `getNumTaskRetries()` is now `getTaskRetriesCount()`.
  - On `BatchJobScheduleStatistics`, `getNumSucceededTasks()` is now `getSucceededTasksCount()`, `getNumFailedTasks()` is now `getFailedTasksCount()`, and `getNumTaskRetries()` is now `getTaskRetriesCount()`.
  - On `BatchClientParallelOptions`, `getMaxDegreeOfParallelism()` is now `getMaxConcurrency()`.
  - On the `BatchClient` (synchronous client), `getNodeFileProperties` and `getTaskFileProperties` now return `BatchFileProperties` instead of `FileResponseHeaderProperties`. On `BatchAsyncClient` (asynchronous methods), `getNodeFileProperties` and `getTaskFileProperties` now return `Mono<BatchFileProperties>` instead of `Mono<FileResponseHeaderProperties>`.

- Changed the names of some models
  - `AccessScope` is now `BatchAccessScope`. On `AuthenticationTokenSettings`, `getAccess()` now returns `List<BatchAccessScope>` instead of `List<AccessScope>` and `setAccess` takes in a parameter of type `List<BatchAccessScope>` rather than `List<AccessScope>`.
  - `AffinityInfo` is now `BatchAffinityInfo`. On `BatchTask` and `BatchTaskCreateContent`, `getAffinityInfo()` now returns `BatchAffinityInfo` instead of `AffinityInfo`.

- On the `BatchCertificate` model, the return type of `getData()` is now `byte[]` instead of `String`.

  - `GetBatchApplicationOptions` is now `BatchApplicationGetOptions`.
  - `ListBatchApplicationsOptions` is now `BatchApplicationsListOptions`.
  - `ListBatchPoolUsageMetricsOptions` is now `BatchPoolUsageMetricsListOptions`.
  - `CreateBatchPoolOptions` is now `BatchPoolCreateOptions`.
  - `ListBatchPoolsOptions` is now `BatchPoolsListOptions`.
  - `DeleteBatchPoolOptions` is now `BatchPoolDeleteOptions`.
  - `GetBatchPoolOptions` is now `BatchPoolGetOptions`.
  - `UpdateBatchPoolOptions` is now `BatchPoolUpdateOptions`.
  - `DisableBatchPoolAutoScaleOptions` is now `BatchPoolAutoScaleDisableOptions`.
  - `EnableBatchPoolAutoScaleOptions` is now `BatchPoolAutoScaleEnableOptions`.
  - `EvaluateBatchPoolAutoScaleOptions` is now `BatchPoolAutoScaleEvaluateOptions`.
  - `ResizeBatchPoolOptions` is now `BatchPoolResizeOptions`.
  - `StopBatchPoolResizeOptions` is now `BatchPoolResizeStopOptions`.
  - `ReplaceBatchPoolPropertiesOptions` is now `BatchPoolPropertiesReplaceOptions`.
  - `RemoveBatchNodesOptions` is now `BatchNodesRemoveOptions`.
  - `ListSupportedBatchImagesOptions` is now `SupportedBatchImagesListOptions`.
  - `ListBatchPoolNodeCountsOptions` is now `BatchPoolNodeCountsListOptions`.
  - `DeleteBatchJobOptions` is now `BatchJobDeleteOptions`.
  - `GetBatchJobOptions` is now `BatchJobGetOptions`.
  - `UpdateBatchJobOptions` is now `BatchJobUpdateOptions`.
  - `ReplaceBatchJobOptions` is now `BatchJobReplaceOptions`.
  - `DisableBatchJobOptions` is now `BatchJobDisableOptions`.
  - `EnableBatchJobOptions` is now `BatchJobEnableOptions`.
  - `TerminateBatchJobOptions` is now `BatchJobTerminateOptions`.
  - `CreateBatchJobOptions` is now `BatchJobCreateOptions`.
  - `ListBatchJobsOptions` is now `BatchJobsListOptions`.
  - `ListBatchJobsFromScheduleOptions` is now `BatchJobsFromScheduleListOptions`.
  - `ListBatchJobPreparationAndReleaseTaskStatusOptions` is now `BatchJobPreparationAndReleaseTaskStatusListOptions`.
  - `GetBatchJobTaskCountsOptions` is now `BatchJobTaskCountsGetOptions`.
  - `CreateBatchCertificateOptions` is now `BatchCertificateCreateOptions`.
  - `ListBatchCertificatesOptions` is now `BatchCertificatesListOptions`.
  - `CancelBatchCertificateDeletionOptions` is now `BatchCertificateCancelDeletionOptions`.
  - `DeleteBatchCertificateOptions` is now `BatchCertificateDeleteOptions`.
  - `GetBatchCertificateOptions` is now `BatchCertificateGetOptions`.
  - `DeleteBatchJobScheduleOptions` is now `BatchJobScheduleDeleteOptions`.
  - `GetBatchJobScheduleOptions` is now `BatchJobScheduleGetOptions`.
  - `UpdateBatchJobScheduleOptions` is now `BatchJobScheduleUpdateOptions`.
  - `ReplaceBatchJobScheduleOptions` is now `BatchJobScheduleReplaceOptions`.
  - `DisableBatchJobScheduleOptions` is now `BatchJobScheduleDisableOptions`.
  - `EnableBatchJobScheduleOptions` is now `BatchJobScheduleEnableOptions`.
  - `TerminateBatchJobScheduleOptions` is now `BatchJobScheduleTerminateOptions`.
  - `CreateBatchJobScheduleOptions` is now `BatchJobScheduleCreateOptions`.
  - `ListBatchJobSchedulesOptions` is now `BatchJobSchedulesListOptions`.
  - `CreateBatchTaskOptions` is now `BatchTaskCreateOptions`.
  - `ListBatchTasksOptions` is now `BatchTasksListOptions`.
  - `CreateBatchTaskCollectionOptions` is now `BatchTaskCollectionCreateOptions`.
  - `DeleteBatchTaskOptions` is now `BatchTaskDeleteOptions.`
  - `GetBatchTaskOptions` is now `BatchTaskGetOptions`.
  - `ReplaceBatchTaskOptions` is now `BatchTaskReplaceOptions`.
  - `ListBatchSubTasksOptions` is now `BatchSubTasksListOptions`.
  - `TerminateBatchTaskOptions` is now `BatchTaskTerminateOptions`.
  - `ReactivateBatchTaskOptions` is now `BatchTaskReactivateOptions`.
  - `DeleteBatchTaskFileOptions` is now `BatchTaskFileDeleteOptions`.
  - `GetBatchTaskFileOptions` is now `BatchTaskFileGetOptions`.
  - `GetBatchTaskFilePropertiesOptions` is now `BatchTaskFilePropertiesGetOptions`.
  - `ListBatchTaskFilesOptions` is now `BatchTaskFilesListOptions`.
  - `CreateBatchNodeUserOptions` is now `BatchNodeUserCreateOptions`.
  - `DeleteBatchNodeUserOptions` is now `BatchNodeUserDeleteOptions`.
  - `ReplaceBatchNodeUserOptions` is now `BatchNodeUserReplaceOptions`.
  - `GetBatchNodeOptions` is now `BatchNodeGetOptions`.
  - `RebootBatchNodeOptions` is now `BatchNodeRebootOptions`.
  - `StartBatchNodeOptions` is now `BatchNodeStartOptions`.
  - `DeallocateBatchNodeOptions` is now `BatchNodeDeallocateOptions`.
  - `ReimageBatchNodeOptions` is now `BatchNodeReimageOptions`.
  - `DisableBatchNodeSchedulingOptions` is now `BatchNodeSchedulingDisableOptions`.
  - `EnableBatchNodeSchedulingOptions` is now `BatchNodeSchedulingEnableOptions`.
  - `GetBatchNodeRemoteLoginSettingsOptions` is now `BatchNodeRemoteLoginSettingsGetOptions`.
  - `UploadBatchNodeLogsOptions` is now `BatchNodeLogsUploadOptions`.
  - `ListBatchNodesOptions` is now `BatchNodesListOptions`.
  - `GetBatchNodeExtensionOptions` is now `BatchNodeExtensionGetOptions`.
  - `ListBatchNodeExtensionsOptions` is now `BatchNodeExtensionsListOptions`.
  - `DeleteBatchNodeFileOptions` is now `BatchNodeFileDeleteOptions`.
  - `GetBatchNodeFileOptions` is now `BatchNodeFileGetOptions`.
  - `GetBatchNodeFilePropertiesOptions` is now `BatchNodeFilePropertiesGetOptions`.
  - `ListBatchNodeFilesOptions` is now `BatchNodeFilesListOptions`.

## 1.0.0-beta.4 (2025-03-24)

### Features Added

- Re-added support for certificates:
  - Added `createCertificate(BatchCertificate certificate)`, `listCertificates()`, `cancelCertificateDeletion(String thumbprintAlgorithm, String thumbprint)`, `deleteCertificate(String thumbprintAlgorithm, String thumbprint)`, and `getCertificate(String thumbprintAlgorithm, String thumbprint)` methods to `BatchClient` and `BatchAsyncClient`.

### Breaking Changes

- Renamed `GetApplicationOptions` to `GetBatchApplicationOptions` (the name of the class of optional parameters for the `getApplication(String applicationId)` method).

- For several methods in `BatchClient` and `BatchAsyncClient` with an optional parameter group, the `requestConditions` parameter was moved out of the optional parameter group and is now a separate parameter.
  - Affected methods: `deletePool`, `poolExists`, `getPool`, `updatePool`, `enablePoolAutoScale`, `resizePool`, `stopPoolResize`, `removeNodes`, `deleteJob`, `getJob`, `updateJob`, `replaceJob`, `disableJob`, `enableJob`, `terminateJob`, `jobScheduleExists`, `deleteJobSchedule`, `getJobSchedule`, `updateJobSchedule`, `replaceJobSchedule`, `disableJobSchedule`, `enableJobSchedule`, `terminateJobSchedule`, `deleteTask`, `getTask`, `replaceTask`, `terminateTask`, and `reactivateTask`.

## 1.0.0-beta.3 (2024-10-31)

### Features Added

- Force delete/terminate job or job schedule:
  - Added `force` parameter of type Boolean to `DeleteBatchJobOptions`, `TerminateBatchJobOptions`, `DeleteBatchJobScheduleOptions`, and `TerminateBatchJobScheduleOptions`.

- Support for compute node start/deallocate operations:
  - Added `startNode(poolId, nodeId)` and `deallocateNode(poolId, nodeId)` methods to `BatchClient` and `BatchAsyncClient`, along with the options for those methods: `StartBatchNodeOptions` and `DeallocateBatchNodeOptions`.

- Container task data mount isolation:
  - Added `containerHostBatchBindMounts` of type `List<ContainerHostBatchBindMountEntry>` to `BatchTaskContainerSettings`.

- Patch improvements for pool and job:
  - Added `displayName`, `vmSize`, `taskSlotsPerNode`, `taskSchedulingPolicy`, `enableInterNodeCommunication`, `virtualMachineConfiguration`, `networkConfiguration`, `userAccounts`, `mountConfiguration`, `upgradePolicy`, and `resourceTags` to `BatchPoolUpdateContent`.
  - Added `networkConfiguration` to `BatchJobUpdateContent`.

- Confidential VM support:
  - Added `confidentialVM` to `SecurityTypes`.
  - Added `securityProfile` of type `VMDiskSecurityProfile` to `ManagedDisk`.

- Support for shared and community gallery images:
  - Added `sharedGalleryImageId` and `communityGalleryImageId` to `ImageReference`.

### Breaking Changes

- Removed `getNodeRemoteDesktop(poolId, nodeId)` method from `BatchClient` and `BatchAsyncClient`. Use `getNodeRemoteLoginSettings(poolId, nodeId)` instead to remotely login to a compute node.
- Removed `CloudServiceConfiguration` from pool models and operations. Use `VirtualMachineConfiguration` when creating pools.
- Removed `ApplicationLicenses` from pool models and operations.

## 1.0.0-beta.2 (2024-05-22)

### Bugs Fixed

- Fixed a bug that caused `long` properties on models to be deserialized incorrectly.

## 1.0.0-beta.1 (2024-05-16)

- Azure Batch client library for Java. This package contains the Microsoft Azure Batch client library.

### Features Added

- Unified Clients: Consolidated multiple smaller clients into two clients: `BatchClient` for synchronous methods and `BatchAsyncClient` for asynchronous methods.
- Refactored Options: Instead of listing each optional parameter separately in method signatures, a single options object is now used. This object encapsulates all optional parameters.
- Bulk Task Creation: Added `createTasks` method for bulk task creation (adding multiple tasks to a job at once) to both clients.

### Bugs Fixed

- Fixed various typos, misspellings, and unclear operation names for improved clarity and consistency.

### Other Changes

- Removal of Ocp Date Header: The `ocp-date` header has been removed from all SDK operations.
