# Release History

## 1.1.2 (2021-09-10)

### Bugs Fixed
- Fixed the lack of separation between "per-call" and "per-retry" HttpPipeline policies (#22932).

### Other Changes
#### Dependency updates
- Upgraded `azure-core` dependency from `1.16.0` to `1.20.0`.
  - [azure-core changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/CHANGELOG.md#1200-2021-09-07)
- Upgraded `azure-core-http-netty` dependency from `1.9.2` to `1.11.0`.
  - [azure-core-http-netty changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty/CHANGELOG.md#1110-2021-09-07)
- Upgraded `azure-core-serializer-json-jackson` dependency from `1.2.3` to `1.2.7`.
  - [azure-core-serializer-json-jackson changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-serializer-json-jackson/CHANGELOG.md#127-2021-09-07)
- Upgraded `jackson-annotations` dependency from `2.12.2` to `2.12.4`.

## 1.1.1 (2021-05-19)

### Fixes and improvements

- Address breaking behavior change where previously `null` could be passed down for `context` parameter to synchronous APIs prior to version `1.0.2` but after `1.0.2` that behavior was changed. `null` can again be passed for the `context` parameter and the SDK will behave as was expected before. In these scenarios `Context.NONE` will be used as the default value.

## 1.1.0 (2021-05-17)

### Fixes and improvements

- Fixed a bug where string tokens that start with a numeric values would transform into number type after payload serialization.
- Upgraded `reactor-core` dependency from `3.3.12.RELEASE` to `3.4.3`
- Upgraded `jackson-annotations` dependency from `2.12.1` to `2.12.2`
- Upgraded `azure-core` dependency from `1.13.0` to `1.16.0`
  - [azure-core changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/CHANGELOG.md#1160-2021-05-07)
- Upgraded `azure-core-serializer-json-jackson` dependency from `1.1.2` to `1.2.3`
  - [azure-core-serializer-json-jackson changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-serializer-json-jackson/CHANGELOG.md#123-2021-05-07)

## 1.0.3 (2021-02-24)

### Dependency Updates

- Upgraded `jackson-annotations` dependency from `2.11.3` to `2.12.1`
- Upgraded `azure-identity` dependency from `1.2.2` to `1.2.3`
  - [azure-identity changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/CHANGELOG.md#123-2021-02-09)
- Upgraded `azure-core` dependency from `1.12.0` to `1.13.0`
  - [azure-core changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/CHANGELOG.md#1130-2021-02-05)
- Upgraded `azure-core-http-netty` dependency from `1.7.1` to `1.8.0`
  - [azure-core-http-netty changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty/CHANGELOG.md#180-2021-02-05)
- Upgraded `azure-core-serializer-json-jackson` dependency from `1.1.1` to `1.1.2`
  - [azure-core-serializer-json-jackson changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-serializer-json-jackson/CHANGELOG.md#112-2021-02-05)

## 1.0.2 (2021-01-21)

### Dependency Updates

- Added diagnostic contexts to async APIs including service namespace.
- Upgraded `azure-core` dependency from `1.11.0` to `1.12.0`
  - [azure-core changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/CHANGELOG.md#1120-2021-01-11)
- Upgraded `azure-identity` dependency from `1.2.1` to `1.2.2`
  - [azure-identity changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/CHANGELOG.md#122-2021-01-12)
- Upgraded `azure-core-http-netty` dependency from `1.7.0` to `1.7.1`
  - [azure-core-http-netty changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty/CHANGELOG.md#171-2021-01-11)
- Upgraded `azure-core-http-okhttp` dependency to `1.4.0` to `1.4.1`
  - [azure-core-http-okhttp changelog](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-okhttp/CHANGELOG.md#141-2021-01-11)

## 1.0.1 (2020-11-05)

### Fixes and improvements

- Removed logic to determine authorization scope based on digital twins instance URI.

## 1.0.0 (2020-10-30)

### New Features

- Regenerated protocol layer from service API version 2020-10-31.
- Updated service API version to use service API version 2020-10-31 by default.

### Breaking changes

Note that these breaking changes are only breaking changes from the preview version of this library.

- Replaced all `Response<string>` and `Pageable<string>` APIs with `Response<T>` and `Pageable<T>` respectively.
- Renamed `CreateDigitalTwin`, `CreateRelationship` and `CreateEventRoute` APIs to `CreateOrReplaceDigitalTwin`, `CreateOrReplaceRelationship` and `CreateOrReplaceEventRoute` respectively.
- Renamed model type `ModelData` to `DigitalTwinsModelData` to make type less generic, and less likely to conflict with other libraries.
- Renamed model type `EventRoute` to `DigitalTwinsEventRoute` to make type less generic, and less likely to conflict with other libraries.
- `EventRoute` (now `DigitalTwinsEventRoute`) object ctor now requires filter.
- Removed `UpdateOperationsUtility` and replace it with a direct dependency on [JsonPatchDocument](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/models/JsonPatchDocument.java) from azure-core.
- Removed `WritableProperty` since service no longer returns that type.
- Removed `MaxItemCount` parameter as an option for GetEventRoutes APIs since users are expected to provide page size in pageable type's .AsPages() method instead.
- Renamed `DigitalTwinsModelData` field `DisplayName` to `DisplayNameLanguageMap` for clarity.
- Renamed `DigitalTwinsModelData` field `Description` to `DescriptionLanguageMap` for clarity.
- Renamed `DigitalTwinsModelData` field `model` to `dtdlModel`.
- Flattened `DigitalTwinsRequestOptions` so that each API takes in ifMatch and ifNoneMatch header directly.
- Reworked `BasicDigitalTwin` and other helper classes to better match the service definitions. This includes renaming `CustomProperties` to `Contents`.
- Added `messageId` as mandatory parameter for telemetry APIs. Service API version 2020-10-31 requires this parameter.
- Renamed CreateModels API parameter `models` to `dtdtlModels` for clarity.

### Fixes and improvements

- Fixed bug where `CreateDigitalTwin` and `CreateRelationship` APIs always sent ifNoneMatch header with value "*" making it impossible to replace an existing entity.

## 1.0.0-beta.3 (2020-10-01)

- Fixed issue with pagination APIs that support max-item-count where the item count was not respected from the second page forward.

## 1.0.0-beta.2 (2020-09-24)

### Fixes and improvements

- Paging functionality for list operations has been fixed.
- `listModelOptions` is renamed to `modelsListOptions` for naming consistency.

## 1.0.0-beta.1 (2020-09-22)

### New features

- Official public preview of azure-digitalTwins-core SDK
- [Azure Digital Twins Public Repo](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/digitaltwins/azure-digitaltwins-core)
- [Azure Digital Twins Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/digitaltwins/azure-digitaltwins-core/src/samples)

### Breaking changes

- N/A

### Added

- N/A

### Fixes and improvements

- N/A
