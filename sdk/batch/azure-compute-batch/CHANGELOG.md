# Release History

## 1.0.0-beta.5 (2025-09-15)

### Breaking Changes

- Renamed every model with a suffix of `-Content` to have a suffix of `-Parameters` instead.
  - Examples:
    - `BatchJobCreateContent` is now `BatchJobCreateParameters`.
    - `BatchNodeDisableSchedulingContent` is now `BatchNodeDisableSchedulingParameters`.
    - `BatchPoolResizeContent` is now `BatchPoolResizeParameters`.
This change affects all models previously suffixed with `-Content`.

- Made miscellaneous model name changes:
  - `BatchJobAction` is now `BatchJobActionKind`.
  - `BatchNodeRebootOption` is now `BatchNodeRebootKind`.
  - `ContainerConfiguration` is now `BatchContainerConfiguration`.
  - `ErrorCategory` is now `BatchErrorSourceCategory`.
  - `ImageReference` is now `BatchVmImageReference`.
  - `MetadataItem` is now `BatchMetadataItem`.
  - `OnAllBatchTasksComplete` is now `BatchAllTasksCompleteMode`.
  - `OnBatchTaskFailure` is now `BatchTaskFailureMode`.
  - `UserAssignedIdentity` is now `BatchUserAssignedIdentity`.
  - `DiffDiskSettings` is now `BatchDiffDiskSettings`.
  - `InboundNatPool` is now `BatchInboundNatPool`.
  - `OSDisk` is now `BatchOsDisk`.
  - `PublicIpAddressConfiguration` is now `BatchPublicIpAddressConfiguration`.
  - `UefiSettings` is now `BatchUefiSettings`.
  - `AccessScope` is now `BatchAccessScope`.
  - `AffinityInfo` is now `BatchAffinityInfo`.
  - `HttpHeader` is now `OutputFileUploadHeader`.
  - `BatchClientParallelOptions` is now `BatchTaskBulkCreateOptions`

- Made many small name/casing changes to names of models, properties, and methods for clarity and consistency.

  - On `BatchJobNetworkConfiguration`, `isSkipWithdrawFromVNet()` is now `isSkipWithdrawFromVnet()`.
  - On `BatchPoolResourceStatistics`, `getDiskReadIOps()` is now `getDiskReadIops()` and `getDiskWriteIOps()` is now `getDiskWriteIops()`.
  - On `NetworkConfiguration`, `getDynamicVNetAssignmentScope()` is now `getDynamicVnetAssignmentScope()` and `setDynamicVNetAssignmentScope` is now `setDynamicVnetAssignmentScope`.
  - On `BatchTaskStatistics`, `getReadIOps()` is now `getReadIops()`, `getWriteIOps()` is now `getWriteIops()`, `getReadIOGiB()` is now `getReadIoGiB()`, and `getWriteIOGiB()` is now `getWriteIoGiB()`.
  - `VMDiskSecurityProfile` has now been changed to `BatchVmDiskSecurityProfile`.
  - `DeleteBatchCertificateError` has now been changed to `BatchCertificateDeleteError`.
  - On `BatchJobStatistics` and `BatchJobScheduleStatistics`, `getNumSucceededTasks()` is now `getSucceededTasksCount()`, `getNumFailedTasks()` is now `getFailedTasksCount()`, `getNumTaskRetries()` is now `getTaskRetriesCount()`, `getReadIOps()` is now `getReadIops()`, `getWriteIOps()` is now `getWriteIops()`, `getReadIOGiB()` is now `getReadIoGiB()`, and `getWriteIOGiB()` is now `getWriteIoGiB()`
  - On `BatchTaskBulkCreateOptions`, `getMaxDegreeOfParallelism()` is now `getMaxConcurrency()`.
  - On the `BatchClient` (synchronous client), `getNodeFileProperties` and `getTaskFileProperties` now return `BatchFileProperties` instead of `FileResponseHeaderProperties`. On `BatchAsyncClient` (asynchronous methods), `getNodeFileProperties` and `getTaskFileProperties` now return `Mono<BatchFileProperties>` instead of `Mono<FileResponseHeaderProperties>`.
  - On `BatchCreateTaskCollectionResult` and `BatchTaskGroup`, `getValue()` is now `getValues()`.
  - On `BatchJob`, `getOnAllTasksComplete()` is now named `getAllTasksCompleteMode()`. `setOnAllTasksComplete` is now `setAllTasksCompleteMode`. `getStats()` is now named `getJobStatistics()`. `getOnTaskFailure()` is now `getTaskFailureMode()`.
  - On `BatchJobSchedule`, `getStats()` is now named `getJobScheduleStatistics()`.
  - On `BatchJobSpecification` and `BatchJobCreateParameters`, `getOnAllTasksComplete()` is now named `getAllTasksCompleteMode()`. `setOnAllTasksComplete` is now `setAllTasksCompleteMode`. `getOnTaskFailure()` is now `getTaskFailureMode()`. `setOnTaskFailure` is now `setTaskFailureMode`.
  - On `BatchJobUpdateParameters`, `getOnAllTasksComplete()` is now named `getAllTasksCompleteMode()`. `setOnAllTasksComplete` is now `setAllTasksCompleteMode`.
  - On `BatchNodeRebootParameters`, `getNodeRebootOption()` is now `getNodeRebootKind()`. `setNodeRebootOption` is now `setNodeRebootKind`.
  - On `BatchNodeRemoveParameters`, `getNodeList()` is now `getNodeIds()`.
  - On `BatchPool`, `getStats()` is now `getPoolStatistics()`.
  - On `BatchPoolStatistics`, `getUsageStats()` is now `getUsageStatistics()` and `getResourceStats()` is now `getResourceStatistics()`.
  - On `BatchTask`, `getStats()` is now `getTaskStatistics()`.
  - On the `BatchCertificate` model, the return type of `getData()` is now `byte[]` instead of `String`.

- Renamed all optional parameter model classes to follow the consistent `{Resource}{Operation}Options` naming pattern.
  - Examples:
    - `GetBatchApplicationOptions` is now `BatchApplicationGetOptions`.
    - `TerminateBatchJobOptions` is now `BatchJobTerminateOptions`.
    - `GetBatchNodeFilePropertiesOptions` is now `BatchNodeFilePropertiesGetOptions`.

This change affects all operation-specific options classes across jobs, pools, certificates, tasks, and nodes.

- The type of `timeOutInSeconds` in many of the optional parameter models has changed from `Integer` to `Duration`. This affects the getter and setter methods on these models. `getTimeOutInSeconds()` now returns `Duration` instead of `Integer`. `setTimeOutInSeconds(Integer timeOutInSeconds)` is now `setTimeOutInSeconds(Duration timeOutInSeconds)`. This change applies to the same set of `{Resource}{Operation}Options` models referenced above.

- Several methods in the SDK have been updated to use the Long-Running Operation (LRO) pattern. LROs are used when an operation may take an extended period to complete. Instead of blocking or returning immediately, LRO methods return a Poller that tracks the operation’s progress and provides access to intermediate and final results.
  - Examples:
    - `deleteJob` has been renamed to `beginDeleteJob`.
      - In the synchronous `BatchClient`, the return type changed from `void` to `SyncPoller<BatchJob, Void>`.
      - In the asynchronous `BatchAsyncClient`, the return type changed from `Mono<Void>` to `PollerFlux<BatchJob, Void>`.
    - `deallocateNode` has been renamed to `beginDeallocateNode`.
      - In the synchronous `BatchClient`, the return type changed from `void` to `SyncPoller<BatchNode, BatchNode>`.
      - In the asynchronous `BatchAsyncClient`, the return type changed from `Mono<Void>` to `PollerFlux<BatchNode, BatchNode>`.
    - `stopPoolResize` has been renamed to `beginStopPoolResize`.
      - In the synchronous `BatchClient`, the return type changed from `void` to `SyncPoller<BatchPool, BatchPool>`.
      - In the asynchronous `BatchAsyncClient`, the return type changed from `Mono<Void>` to `PollerFlux<BatchPool, BatchPool>`.
The same rename pattern (method to beginMethod) and return type change applies to the following old method names: `deleteCertificate`, `disableJob`, `enableJob`, `terminateJob`, `deleteJobSchedule`, `terminateJobSchedule`, `rebootNode`, `reimageNode`, `startNode`, `removeNodes`, `deletePool`, and `resizePool`.

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
