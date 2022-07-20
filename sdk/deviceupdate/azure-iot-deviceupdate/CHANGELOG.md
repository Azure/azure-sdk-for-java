# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
