# Release History

## 1.0.0 (2025-03-25)

This release is the first stable release of the Azure Compute Batch client library.

### Breaking Changes

- Changed capitalization of the methods in some models
  - In BatchJobNetworkConfiguration, isSkipWithdrawFromVNet() is now isSkipWithdrawFromVnet().
  - In BatchJobScheduleStatistics, getReadIOps() is now getReadIops() and getWriteIOps() is now getWriteIops().
  - In BatchJobStatistics, getReadIOps() is now getReadIops() and getWriteIOps() is now getWriteIops().
  - In BatchPoolResourceStatistics, getDiskReadIOps() is now getDiskReadIops() and getDiskWriteIOps() is now getDiskWriteIops().
  - In BatchTaskStatistics, getReadIOps() is now getReadIops() and getWriteIOps() is now getWriteIops().
  - In NetworkConfiguration, getDynamicVNetAssignmentScope() is now getDynamicVnetAssignmentScope() and setDynamicVNetAssignmentScope(DynamicVNetAssignmentScope dynamicVNetAssignmentScope) is now setDynamicVnetAssignmentScope(DynamicVNetAssignmentScope dynamicVnetAssignmentScope).

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
