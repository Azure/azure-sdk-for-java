# Release History

## 1.0.15 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.


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
