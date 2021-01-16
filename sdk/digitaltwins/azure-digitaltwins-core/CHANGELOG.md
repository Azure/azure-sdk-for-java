# Release History

## 1.1.0-beta.1 (Unreleased)

- Added diagnostic contexts to async APIs including service namespace.

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
- Removed `UpdateOperationsUtility` and replace it with a direct dependency on [JsonPatchDocument](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/models/JsonPatchDocument.java) from azure-core.
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
- [Azure Digital Twins Public Repo](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core)
- [Azure Digital Twins Samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core/src/samples)

### Breaking changes

- N/A

### Added

- N/A

### Fixes and improvements

- N/A
