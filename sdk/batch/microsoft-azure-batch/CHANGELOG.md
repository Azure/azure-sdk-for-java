# Release History

## 10.0.0 (2021-07-30)

### Features

- Adds two properties on accounts which enable auto-storage to use a managed identity for authentication rather than a shared key:
   - Setting `autoStorageAuthenticationMode` to "BatchAccountManagedIdentity" will use the identity on the account for storage management operations such as blob container creation/deletion.
   - Setting `identityReference` will specify the identity which can be used on compute nodes to access auto-storage. Note that this identity *must* be assigned to each pool individually.
- Adds an `identityReference` property to the following models to support accessing resources via managed identity:
  - `AzureBlobFileSystemConfiguration`
  - `OutputFileBlobContainerDestination`
  - `ContainerRegistry`
  - `ResourceFile`
  - `UploadBatchServiceLogsConfiguration`
- Adds an `allowedAuthenticationModes` property on `BatchAccount` to list the allowed authentication modes for a given account that can be used to authenticate with the data plane. This does not affect authentication with the control plane.
- Adds a `computeNodeExtension` operation to `BatchServiceClient` for getting and listing VM extensions on a node.
- Adds an `extensions` property to `VirtualMachineConfiguration` on `CloudPool` to specify virtual machine extensions for nodes.
- Adds the ability to specify availability zones using a new property `nodePlacementConfiguration` on `VirtualMachineConfiguration`
- Adds an `osDisk` property to `VirtualMachineConfiguration`, which contains settings for the operating system disk of the virtual machine.
  - The `placement` property on `DiffDiskSettings` specifies the ephemeral disk placement for operating system disks for all VMs in the pool. Setting it to "CacheDisk" will store the ephemeral OS disk on the VM cache.
- Adds a `maxParallelTasks` property on `CloudJob` to control the maximum allowed tasks per job (defaults to `-1`, meaning unlimited).
- Adds a `virtualMachineInfo` property on `ComputeNode` which contains information about the current state of the virtual machine, including the exact version of the marketplace image the VM is using.
- Adds a `recurrenceInterval` property to `Schedule` to control the interval between the start times of two successive job under a job schedule.
 - Adds a `listSupportedVirtualMachineSkus` operation, which gets the list of Batch-supported Virtual Machine VM sizes available at a given location.
 - Adds a `listOutboundNetworkDependenciesEndpoints` operation, which lists the endpoints that a Batch Compute Node under a Batch Account may call as part of Batch service administration.
    - [More information about creating a pool inside of a virtual network.](https://docs.microsoft.com/azure/batch/batch-virtual-network)

## 9.0.0 (2021-01-08)

### Features

- Adds support for task slots
  - `JobOperations.getTaskSlotCounts()` returns task slot counts
  - `JobOperations.getTaskCountsResult()` returns a `TaskCountsResult` object containing both task and slot counts
- Adds property `requiredSlots` to `CloudTask`, allowing the user to specify how many slots on a node they should take up
- Exposes a `BatchClient` factory method

### Breaking Changes

- Property `maxTasksPerNode` is replaced with `taskSlotsPerNode`, which allows nodes to consume a dynamic amount of slots for more fine-grained control over resource consumption
  - `CloudPool.maxTasksPerNode` &rarr; `CloudPool.taskSlotsPerNode`
  - `PoolAddParameter.maxTasksPerNode` &rarr; `PoolAddParameter.taskSlotsPerNode`
  - `PoolSpecification.maxTasksPerNode` &rarr; `PoolSpecification.taskSlotsPerNode`

## 8.0.0 (2020-04-27)
### Features
- Added ability to encrypt `ComputeNode` disk drives using the new `diskEncryptionConfiguration` property of `VirtualMachineConfiguration`.
- The `createCertificate` functions on `CertificateOperations` had their parameters updated to more clearly reflect that `password` is optional for PFX formatted certificates.
- **[Breaking]** The `virtualMachineImageId` property of `ImageReference` can now only refer to a Shared Image Gallery image.
- **[Breaking]** Pools can now be provisioned without a public IP using the new `PublicIPAddressConfiguration` property of `NetworkConfiguration`.
    - The `PublicIPs` property of `NetworkConfiguration` has moved in to `PublicIPAddressConfiguration` as well. This property can only be specified if `IPAddressProvisioningType` is `UserManaged`.
      
## 7.0.0
### Features
    - Added ability to specify a collection of public IPs on CloudPool via the new PublicIPs property. This guarantees nodes in the Pool will have an IP from the list user provided IPs.
    - Added ability to mount remote file-systems on each node of a pool via the MountConfiguration property on CloudPool.
    - Shared Image Gallery images can now be specified on the VirtualMachineImageId property of ImageReference by referencing the image via its ARM ID.
    - [Breaking] When not specified, the default value for WaitForSuccess on StartTask is now true (was false).
    - [Breaking] When not specified, the default value for Scope on AutoUserSpecification is now always Pool (was Task on Windows nodes, Pool on Linux nodes).
### REST API version
    This version of the Batch .NET client library targets version 2019-08-01.10.0 of the Azure Batch REST API.

## 6.0.0
### Features
- **[Breaking]** Replaced PoolOperations.ListNodeAgentSkus with PoolOperations.ListSupportedImages. ListSupportedImages contains all of the same information originally available in ListNodeAgentSKUs but in a clearer format. New non-verified images are also now returned. Additional information about Capabilities and BatchSupportEndOfLife is accessible on the ImageInformation object returned by ListSupportedImages.
- Now support network security rules blocking network access to a CloudPool based on the source port of the traffic. This is done via the SourcePortRanges property on NetworkSecurityGroupRule.
- When running a container, Batch now supports executing the task in the container working directory or in the Batch task working directory. This is controlled by the WorkingDirectory property on TaskContainerSettings.

### REST API version
This version of the Batch .NET client library targets version 2019-06-01.9.0 of the Azure Batch REST API.

## 5.0.1
### Features
### Fixes
  - Fix corrupt signing of 5.0.0 Java SDK binary

## 5.0.0
### Features
- **[Breaking]** Removed support for the `ChangeOSVersion` API on `CloudServiceConfiguration` pools. 
  - Removed `PoolOperations.ChangeOSVersion`.
  - Renamed `TargetOSVersion` to `OSVersion` and removed `CurrentOSVersion` on `CloudPool`.
  - Removed `PoolState.Upgrading` enum.
- **[Breaking]** Removed `DataEgressGiB` and `DataIngressGiB` from `PoolUsageMetrics`. These properties are no longer supported.
- **[Breaking]** ResourceFile improvements
  - Added the ability specify an entire Azure Storage container in `ResourceFile`. There are now three supported modes for `ResourceFile`:
    - `ResourceFile.withUrl` creates a `ResourceFile` pointing to a single HTTP URL.
    - `ResourceFile.withStorageContainerUrl` creates a `ResourceFile` pointing to an Azure Blob Storage container.
    - `ResourceFile.withAutoStorageContainer` creates a `ResourceFile` pointing to an Azure Blob Storage container in the Batch registered auto-storage account.
      - The `BlobPrefix` property can be used to filter downloads from a storage container to only those matching the prefix.
  - URLs provided to `ResourceFile` via the `ResourceFile.withUrl` method can now be any HTTP URL. Previously, these had to be an Azure Blob Storage URL.
- **[Breaking]** Removed `OSDisk` property from `VirtualMachineConfiguration`. This property is no longer supported.
- Pools which set the `DynamicVNetAssignmentScope` on `NetworkConfiguration` to be `DynamicVNetAssignmentScope.Job` can 
  now dynamically assign a Virtual Network to each node the job's tasks run on. The specific Virtual Network to join the nodes to is specified in 
  the new `JobNetworkConfiguration` property on `CloudJob` and `JobSpecification`. 
  - Note: This feature is in public preview. It is disabled for all Batch accounts except for those which have contacted us and requested to be in the pilot.
- The maximum lifetime of a task is now 180 days (previously it was 7).
- Added support on Windows pools for creating users with a specific login mode (either `Batch` or `Interactive`) via `WindowsUserConfiguration.LoginMode`.
- The default task retention time for all tasks is now 7 days, previously it was infinite.

### REST API version
This version of the Batch .NET client library targets version 2018-12-01.8.0 of the Azure Batch REST API.

## 4.0.1
### Fixes
  - Fix missing signing of 4.0.0 Java SDK binary

## 4.0.0
### Features
 - Added the functionality to get which version of the Azure Batch Node Agent(https://github.com/Azure/Batch/blob/master/changelogs/nodeagent/CHANGELOG.md) is running on nodes via the NodeAgentInformation property of ComputeNode.
 - Added additional error handling to task add operations to prevent deadlock scenarios.
 - Removed validation status from count operations.
 - Updated adal, azure-client-runtime, and commons-codec dependencies.
 - Update 'List' methods to return the lower layer PagedList object instead of the standard List.

### REST API version
This version of the Batch Java client library targets version 2018-08-01.7.1 of the Azure Batch REST API. See this [document](https://docs.microsoft.com/rest/api/batchservice/batch-service-rest-api-versioning#latest-version-2018-08-0171) for detail.

## 3.3.0
### Features
 - `createTasks` rethrow `RuntimeException` catched by internal threads. 
 - `createTasks` handle `RequestSizeTooLarge` chunking errors for well behaved tasks.

## 3.2.0
### Features
 - Update comments for some classes and properties.
 - Added the `leavingPool` property to `NodeCounts` class.

## 3.1.0
### Features
 - Added the ability to query pool node counts by state, via the new `listPoolNodeCounts` method on `PoolOperations`.
 - Added the ability to upload Azure Batch node agent logs from a particular node, via the `uploadComputeNodeBatchServiceLogs` method on `ComputeNodeOperations`.
   - This is intended for use in debugging by Microsoft support when there are problems on a node.

### REST API version
This version of the Batch Java client library targets version 2018-03-01.6.1 of the Azure Batch REST API. See this [document](https://docs.microsoft.com/rest/api/batchservice/batch-service-rest-api-versioning#latest-version-2018-03-0161) for detail.


## 3.0.0
### Features
- Added the ability to get a discount on Windows VM pricing if you have on-premises licenses for the OS SKUs you are deploying, via `licenseType` on `VirtualMachineConfiguration`.
- Added support for attaching empty data drives to `VirtualMachineConfiguration` based pools, via the new `dataDisks` property on `VirtualMachineConfiguration`.
- **[Breaking]** Custom images must now be deployed using a reference to an ARM Image, instead of pointing to .vhd files in blobs directly.
  - The new `virtualMachineImageId` property on `ImageReference` contains the reference to the ARM Image, and `OSDisk.imageUris` no longer exists.
  - Because of this, `imageReference` is now a required property of `VirtualMachineConfiguration`.
- **[Breaking]** Multi-instance tasks (created using `MultiInstanceSettings`) must now specify a `coordinationCommandLine`, and `numberOfInstances` is now optional and defaults to 1.
- Added support for tasks run using Docker containers. To run a task using a Docker container you must specify a `containerConfiguration` on the `VirtualMachineConfiguration` for a pool, and then add `taskContainerSettings` on the Task.

### REST API version
This version of the Batch Java client library targets version 2017-09-01.6.0 of the Azure Batch REST API. See this [document](https://docs.microsoft.com/rest/api/batchservice/batch-service-rest-api-versioning#latest-version-2017-09-0160) for detail.
