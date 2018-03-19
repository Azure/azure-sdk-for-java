# Azure Batch SDK for Java release notes

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
