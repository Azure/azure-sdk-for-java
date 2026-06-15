# Release History

## 1.1.0-beta.1 (2026-06-15)

### Breaking Changes

#### `DeviceUpdateClientBuilder` was modified

* `buildClient()` was removed
* `buildAsyncClient()` was removed

### Features Added

* `models.StepType` was added

* `models.DeviceClass` was added

* `models.OperationStatus` was added

* `models.DeviceHealth` was added

* `models.DeviceClassSubgroupDeploymentState` was added

* `models.DeviceUpdateAgentId` was added

* `models.UpdateId` was added

* `models.DeviceClassSubgroupDeploymentStatus` was added

* `models.Step` was added

* `models.ImportType` was added

* `models.LogCollectionOperationDetailedStatus` was added

* `models.UpdateFile` was added

* `models.DeviceDeploymentState` was added

* `models.LogCollection` was added

* `models.Error` was added

* `models.PatchBody` was added

* `models.UpdateOperation` was added

* `models.Group` was added

* `models.UpdateFileDownloadHandler` was added

* `models.InstallResult` was added

* `models.StepResult` was added

* `models.DeploymentStatus` was added

* `models.HealthCheck` was added

* `models.FileImportMetadata` was added

* `models.InnerError` was added

* `models.GroupType` was added

* `models.CloudInitiatedRollbackPolicyFailure` was added

* `models.UpdateCompliance` was added

* `models.Deployment` was added

* `models.ImportUpdateInputItem` was added

* `models.DownloadSecurity` was added

* `models.UpdateFileBase` was added

* `models.Device` was added

* `models.DeviceOperation` was added

* `models.UpdateInfo` was added

* `models.DeploymentDeviceState` was added

* `models.DeviceClassSubgroupUpdatableDevices` was added

* `models.LogCollectionOperationDeviceStatus` was added

* `models.HealthCheckResult` was added

* `models.Update` was added

* `models.DeviceHealthState` was added

* `models.Instructions` was added

* `models.Compatibility` was added

* `models.DeviceClassSubgroup` was added

* `models.ContractModel` was added

* `models.CloudInitiatedRollbackPolicy` was added

* `models.DeviceClassProperties` was added

* `models.DeploymentState` was added

* `models.ImportManifestMetadata` was added

#### `DeviceUpdateClient` was modified

* `listOperationStatuses(java.lang.String,java.lang.Integer)` was added
* `getUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listUpdates()` was added
* `listVersions(java.lang.String,java.lang.String)` was added
* `getUpdate(java.lang.String,java.lang.String,java.lang.String)` was added
* `getOperationStatus(java.lang.String)` was added
* `listOperationStatuses()` was added
* `listVersions(java.lang.String,java.lang.String,java.lang.String)` was added
* `beginImportUpdate(java.util.List)` was added
* `beginDeleteUpdate(java.lang.String,java.lang.String,java.lang.String)` was added
* `listUpdates(java.lang.String,java.lang.String)` was added
* `listProviders()` was added
* `listNames(java.lang.String)` was added
* `listFiles(java.lang.String,java.lang.String,java.lang.String)` was added
* `getFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `getOperationStatus(java.lang.String,java.lang.String)` was added
* `getFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `DeviceManagementAsyncClient` was modified

* `getLogCollection(java.lang.String)` was added
* `getUpdateCompliance()` was added
* `listDeviceClasses(java.lang.String)` was added
* `listDevices(java.lang.String)` was added
* `deleteDeploymentForDeviceClassSubgroup(java.lang.String,java.lang.String,java.lang.String)` was added
* `listDeploymentsForDeviceClassSubgroup(java.lang.String,java.lang.String,java.lang.String)` was added
* `listInstallableUpdatesForDeviceClass(java.lang.String)` was added
* `startLogCollection(java.lang.String,models.LogCollection)` was added
* `deleteDeviceClass(java.lang.String)` was added
* `listDeviceStatesForDeviceClassSubgroupDeployment(java.lang.String,java.lang.String,java.lang.String)` was added
* `getUpdateComplianceForGroup(java.lang.String)` was added
* `getDeploymentStatus(java.lang.String,java.lang.String)` was added
* `getOperationStatus(java.lang.String,java.lang.String)` was added
* `listGroups()` was added
* `listHealthOfDevices(java.lang.String)` was added
* `getDeviceClass(java.lang.String)` was added
* `deleteDeployment(java.lang.String,java.lang.String)` was added
* `listDeviceClassSubgroupsForGroup(java.lang.String,java.lang.String)` was added
* `getOperationStatus(java.lang.String)` was added
* `listDeploymentsForDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `listDeploymentsForGroup(java.lang.String,java.lang.String)` was added
* `getDeployment(java.lang.String,java.lang.String)` was added
* `getDeploymentForDeviceClassSubgroup(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteGroup(java.lang.String)` was added
* `getDeviceClassSubgroupDeploymentStatus(java.lang.String,java.lang.String,java.lang.String)` was added
* `listOperationStatuses()` was added
* `createOrUpdateDeployment(java.lang.String,java.lang.String,models.Deployment)` was added
* `deleteDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `beginImportDevices(models.ImportType)` was added
* `listDeviceStatesForDeviceClassSubgroupDeployment(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listDeviceClassSubgroupsForGroup(java.lang.String)` was added
* `listOperationStatuses(java.lang.String,java.lang.Integer)` was added
* `listDeviceClasses()` was added
* `getGroup(java.lang.String)` was added
* `getLogCollectionDetailedStatus(java.lang.String)` was added
* `listDeploymentsForGroup(java.lang.String)` was added
* `listLogCollections()` was added
* `getDeviceClassSubgroupUpdateCompliance(java.lang.String,java.lang.String)` was added
* `retryDeployment(java.lang.String,java.lang.String,java.lang.String)` was added
* `updateDeviceClass(java.lang.String,models.PatchBody)` was added
* `listGroups(java.lang.String)` was added
* `getBestUpdatesForDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `listBestUpdatesForGroup(java.lang.String)` was added
* `stopDeployment(java.lang.String,java.lang.String,java.lang.String)` was added
* `listDevices()` was added
* `getDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `getDeviceModule(java.lang.String,java.lang.String)` was added
* `getDevice(java.lang.String)` was added

#### `DeviceManagementClient` was modified

* `listDeviceClassSubgroupsForGroup(java.lang.String)` was added
* `listDevices()` was added
* `listOperationStatuses()` was added
* `listDeploymentsForDeviceClassSubgroup(java.lang.String,java.lang.String,java.lang.String)` was added
* `stopDeployment(java.lang.String,java.lang.String,java.lang.String)` was added
* `getDeviceClassSubgroupUpdateCompliance(java.lang.String,java.lang.String)` was added
* `deleteDeploymentForDeviceClassSubgroup(java.lang.String,java.lang.String,java.lang.String)` was added
* `startLogCollection(java.lang.String,models.LogCollection)` was added
* `getDeviceClass(java.lang.String)` was added
* `getDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `listDeviceClasses(java.lang.String)` was added
* `getDeviceClassSubgroupDeploymentStatus(java.lang.String,java.lang.String,java.lang.String)` was added
* `getLogCollectionDetailedStatus(java.lang.String)` was added
* `listBestUpdatesForGroup(java.lang.String)` was added
* `listLogCollections()` was added
* `listDeviceClassSubgroupsForGroup(java.lang.String,java.lang.String)` was added
* `getDeploymentForDeviceClassSubgroup(java.lang.String,java.lang.String,java.lang.String)` was added
* `getGroup(java.lang.String)` was added
* `listGroups()` was added
* `getUpdateComplianceForGroup(java.lang.String)` was added
* `getDeployment(java.lang.String,java.lang.String)` was added
* `beginImportDevices(models.ImportType)` was added
* `listHealthOfDevices(java.lang.String)` was added
* `deleteGroup(java.lang.String)` was added
* `getDevice(java.lang.String)` was added
* `listGroups(java.lang.String)` was added
* `listDevices(java.lang.String)` was added
* `getUpdateCompliance()` was added
* `getDeviceModule(java.lang.String,java.lang.String)` was added
* `listDeviceClasses()` was added
* `listDeploymentsForGroup(java.lang.String,java.lang.String)` was added
* `listDeviceStatesForDeviceClassSubgroupDeployment(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteDeviceClass(java.lang.String)` was added
* `getLogCollection(java.lang.String)` was added
* `deleteDeployment(java.lang.String,java.lang.String)` was added
* `listDeviceStatesForDeviceClassSubgroupDeployment(java.lang.String,java.lang.String,java.lang.String)` was added
* `retryDeployment(java.lang.String,java.lang.String,java.lang.String)` was added
* `getOperationStatus(java.lang.String,java.lang.String)` was added
* `updateDeviceClass(java.lang.String,models.PatchBody)` was added
* `listDeploymentsForGroup(java.lang.String)` was added
* `listInstallableUpdatesForDeviceClass(java.lang.String)` was added
* `getDeploymentStatus(java.lang.String,java.lang.String)` was added
* `deleteDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `listOperationStatuses(java.lang.String,java.lang.Integer)` was added
* `listDeploymentsForDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `getBestUpdatesForDeviceClassSubgroup(java.lang.String,java.lang.String)` was added
* `createOrUpdateDeployment(java.lang.String,java.lang.String,models.Deployment)` was added
* `getOperationStatus(java.lang.String)` was added

#### `DeviceUpdateClientBuilder` was modified

* `buildDeviceUpdateAsyncClient()` was added
* `buildDeviceManagementClient()` was added
* `buildDeviceManagementAsyncClient()` was added
* `buildDeviceUpdateClient()` was added

#### `DeviceUpdateServiceVersion` was modified

* `V2026_06_01` was added

#### `DeviceUpdateAsyncClient` was modified

* `listProviders()` was added
* `beginImportUpdate(java.util.List)` was added
* `listNames(java.lang.String)` was added
* `listVersions(java.lang.String,java.lang.String,java.lang.String)` was added
* `getUpdate(java.lang.String,java.lang.String,java.lang.String)` was added
* `getOperationStatus(java.lang.String)` was added
* `getFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listOperationStatuses(java.lang.String,java.lang.Integer)` was added
* `listUpdates()` was added
* `beginDeleteUpdate(java.lang.String,java.lang.String,java.lang.String)` was added
* `listOperationStatuses()` was added
* `getFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listVersions(java.lang.String,java.lang.String)` was added
* `listFiles(java.lang.String,java.lang.String,java.lang.String)` was added
* `listUpdates(java.lang.String,java.lang.String)` was added
* `getOperationStatus(java.lang.String,java.lang.String)` was added
* `getUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

## 1.0.33 (2026-05-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.57.1` to version `1.58.0`.
- Upgraded `azure-core-http-netty` from `1.16.3` to version `1.16.4`.

## 1.0.32 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.16.2` to version `1.16.3`.
- Upgraded `azure-core` from `1.57.0` to version `1.57.1`.

## 1.0.31 (2025-10-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.56.1` to version `1.57.0`.
- Upgraded `azure-core-http-netty` from `1.16.1` to version `1.16.2`.

## 1.0.30 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.16.0` to version `1.16.1`.
- Upgraded `azure-core` from `1.56.0` to version `1.56.1`.

## 1.0.29 (2025-08-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.5` to version `1.56.0`.
- Upgraded `azure-core-http-netty` from `1.15.13` to version `1.16.0`.

## 1.0.28 (2025-07-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.4` to version `1.55.5`.
- Upgraded `azure-core-http-netty` from `1.15.12` to version `1.15.13`.

## 1.0.27 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.
- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.

## 1.0.26 (2025-03-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to version `1.55.3`.
- Upgraded `azure-core-http-netty` from `1.15.10` to version `1.15.11`.

## 1.0.25 (2025-02-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.10`.
- Upgraded `azure-core` from `1.54.1` to version `1.55.2`.

## 1.0.24 (2024-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.53.0` to version `1.54.1`.
- Upgraded `azure-core-http-netty` from `1.15.5` to version `1.15.7`.

## 1.0.23 (2024-10-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.52.0` to version `1.53.0`.
- Upgraded `azure-core-http-netty` from `1.15.4` to version `1.15.5`.

## 1.0.22 (2024-09-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.

## 1.0.21 (2024-08-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.

## 1.0.20 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.

## 1.0.19 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.

## 1.0.18 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.

## 1.0.17 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.

## 1.0.16 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.

## 1.0.15 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.

## 1.0.14 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.

## 1.0.13 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.

## 1.0.12 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.

## 1.0.11 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 1.0.10 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 1.0.9 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.

## 1.0.8 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 1.0.7 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.0.6 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 1.0.5 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 1.0.4 (2023-02-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.

## 1.0.3 (2023-01-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.

## 1.0.2 (2022-11-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.

## 1.0.1 (2022-10-24)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 1.0.0 (2022-09-09)
This is the first stable release for the Device Update for IoT Hub client library.

### Changes since 1.0.0-beta.3

#### Features Added
- Added filter to `listDeviceClasses` device management method.
- Updated description for some methods to be more descriptive and less ambiguous.

#### Breaking Changes
- Removed filter from `listBestUpdatesForGroup` device management method.

#### Other Changes

##### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.5`.

## 1.0.0-beta.3 (2022-07-12)

### Features Added

- Added `relatedFiles` and `downloadHandler` to `Update`
- Updated various model that reference update to include not only `updateId` but also update `description` and `friendlyName`
- Removed device tag concept
- Allow to filter by deployment status in the `listDevices` method
- Added ability to update device class friendly name
- Added ability to delete device class
- Added device class subgroups to groups
- Added new method to retrieve devices health information

### Breaking Changes

- Added `DeviceManagementClientBuilder` to create `DeviceManagementClient` instance via `DeviceManagementClientBuilder.buildClient()`.
- Modified `DeviceUpdateClientBuilder` to create `DeviceUpdateClient` instance via `DeviceUpdateClientBuilder.buildClient()`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-core-http-netty` from `1.12.2` to version `1.12.3`.

## 1.0.0-beta.2 (2022-01-19)

### Breaking Changes

This is a new version of client SDK. Changes are:

- Remove models, use `BinaryData` as request and response payload.
- A sync client and an async client for each operation group.
- Add `RequestOptions` to client method parameters.
- Return type of client method is always `Response` except paging and long-running operations.
- Allow users to set `ServiceVersion` in client builder.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.24.1`.
- Upgraded `azure-core-http-netty` to `1.11.6`.

## 1.0.0-beta.1 (2021-03-02)
This is the initial release of Azure Device Update for IoT Hub library. For more information, please see the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/deviceupdate/azure-iot-deviceupdate/README.md) 
and [samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/deviceupdate/azure-iot-deviceupdate/src/samples/README.md).

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).
