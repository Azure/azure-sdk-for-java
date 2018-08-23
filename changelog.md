# Azure Batch SDK for Java release notes

## Changes in 4.0.0
### Features
 - Added the functionality to get which version of the Azure Batch Node Agent(https://github.com/Azure/Batch/blob/master/changelogs/nodeagent/CHANGELOG.md) is running on nodes via the NodeAgentInformation property of ComputeNode.
 - Added additional error handling to task add operations to prevent deadlock scenarios.
 - Removed validation status from count operations.
 - Updated adal, azure-client-runtime, and commons-codec dependencies.
 - Update 'List' methods to return the lower layer PagedList object instead of the standard List.

### REST API version
This version of the Batch Java client library targets version 2018-08-01.7.1 of the Azure Batch REST API. See this [document](https://docs.microsoft.com/en-us/rest/api/batchservice/batch-service-rest-api-versioning#latest-version-2018-08-0171) for detail.

## Changes in 3.3.0
### Features
 - `createTasks` rethrow `RuntimeException` catched by internal threads. 
 - `createTasks` handle `RequestSizeTooLarge` chunking errors for well behaved tasks.

## Changes in 3.2.0
### Features
 - Update comments for some classes and properties.
 - Added the `leavingPool` property to `NodeCounts` class.

## Changes in 3.1.0
### Features
 - Added the ability to query pool node counts by state, via the new `listPoolNodeCounts` method on `PoolOperations`.
 - Added the ability to upload Azure Batch node agent logs from a particular node, via the `uploadComputeNodeBatchServiceLogs` method on `ComputeNodeOperations`.
   - This is intended for use in debugging by Microsoft support when there are problems on a node.

### REST API version
This version of the Batch Java client library targets version 2018-03-01.6.1 of the Azure Batch REST API. See this [document](https://docs.microsoft.com/en-us/rest/api/batchservice/batch-service-rest-api-versioning#latest-version-2018-03-0161) for detail.


## Changes in 3.0.0
### Features
- Added the ability to get a discount on Windows VM pricing if you have on-premises licenses for the OS SKUs you are deploying, via `licenseType` on `VirtualMachineConfiguration`.
- Added support for attaching empty data drives to `VirtualMachineConfiguration` based pools, via the new `dataDisks` property on `VirtualMachineConfiguration`.
- **[Breaking]** Custom images must now be deployed using a reference to an ARM Image, instead of pointing to .vhd files in blobs directly.
  - The new `virtualMachineImageId` property on `ImageReference` contains the reference to the ARM Image, and `OSDisk.imageUris` no longer exists.
  - Because of this, `imageReference` is now a required property of `VirtualMachineConfiguration`.
- **[Breaking]** Multi-instance tasks (created using `MultiInstanceSettings`) must now specify a `coordinationCommandLine`, and `numberOfInstances` is now optional and defaults to 1.
- Added support for tasks run using Docker containers. To run a task using a Docker container you must specify a `containerConfiguration` on the `VirtualMachineConfiguration` for a pool, and then add `taskContainerSettings` on the Task.

### REST API version
This version of the Batch Java client library targets version 2017-09-01.6.0 of the Azure Batch REST API. See this [document](https://docs.microsoft.com/en-us/rest/api/batchservice/batch-service-rest-api-versioning#latest-version-2017-09-0160) for detail.
